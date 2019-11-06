package com.secretingradient.ingradientlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.secretingradient.ingradientlauncher.element.*
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class DataKeeper2(val context: Context) {
    interface OnDataChangedListener <K, V>{
        fun onInserted(index: K, item: V)
        fun onRemoved(index: K)
        fun onMoved(from: K, to: K)
        fun onChanged(newData: MutableMap<K, V>)
    }

    class DataHolder <K, V> (val context: Context, val fileName: String): OnDataChangedListener<K, V> {
        lateinit var data: MutableMap<K, V>

        override fun onInserted(index: K, item: V) {
            if (data.containsKey(index))
                throw LauncherException("attempt to rewrite element ${data[index]} at index $index")
            data[index] = item
            dumpData()
        }

        override fun onRemoved(index: K) {
            data.remove(index)
            dumpData()
        }

        override fun onMoved(from: K, to: K) {
            if (data.containsKey(to))
                throw LauncherException("attempt to rewrite element ${data[to]} at index $to")
            if (!data.containsKey(from))
                throw LauncherException("attempt to move null element at index $from")
            data[to] = data[from]!!
            dumpData()
        }

        override fun onChanged(newData: MutableMap<K, V>) {
            data.clear()
            data.putAll(newData)
        }

        fun dumpData() {
            ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)).use { it.writeObject(data) }
        }

        fun loadData() {
            var stream: ObjectInputStream? = null
            try {
                stream = ObjectInputStream(context.openFileInput(fileName))
                val loaded = stream.readObject() as? MutableMap<K, V>
                if (loaded != null)
                    data = loaded
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            stream?.close()
        }

        fun deleteFile() {
            context.deleteFile(fileName)
        }
    }

    private class AppCache(packageName: String? = null, name: String? = null, label: String? = null, icon: Drawable? = null) : Serializable {
        lateinit var packageName: String
        lateinit var name: String
        lateinit var label: String
        @Transient var icon: Drawable? = null
        private var iconByteArray: ByteArray? = null

        init {
            if (packageName != null) this.packageName = packageName
            if (name != null) this.name = name
            if (label != null) this.label = label
            if (icon != null) this.icon = icon
        }

        val id: String
            get() = "${packageName}_$name" // .split('_')

        fun set(appInfo: AppInfo) {
            this.packageName = appInfo.packageName
            this.name = appInfo.name
            this.label = appInfo.label
            this.icon = appInfo.icon
        }

        fun encodeIcon(size: Int?) {
            iconByteArray = encodeBitmap(if (size == null) icon!!.toBitmap() else icon!!.toBitmap(size, size))
        }

        fun decodeIcon(context: Context) {
            icon = BitmapDrawable(context.resources, decodeBitmap(iconByteArray!!))
        }

        companion object {
            private const val serialVersionUID = 44010L

            fun createFromResolveInfo(context: Context, resolveInfo: ResolveInfo): AppCache {
                val pm = context.packageManager
                val ai = resolveInfo.activityInfo
                return AppCache(
                    ai.packageName,
                    ai.name,
                    ai.loadLabel(pm).toString(),
                    ai.loadIcon(pm)
                )
            }

            fun getIdFromResolveInfo(resolveInfo: ResolveInfo): String {
                return "${resolveInfo.activityInfo.packageName}_${resolveInfo.activityInfo.name}"
            }
        }
    }

    private var allAppsCacheData = DataHolder<String, AppCache>(context, "all_apps_cache_data")
    var userStageData = DataHolder<Int, ElementData>(context, "user_stage_data")
    var allAppsIds: MutableList<String>

    init {
        allAppsCacheData.loadData()
        // mainScreen
        userStageData.loadData()
        // widgets

        fetchUpdates()

        allAppsCacheData.data.forEach { it.value.decodeIcon(context) }
        allAppsIds = allAppsCacheData.data.keys.toMutableList()
    }

    private fun getLaunchableApps(): List<ResolveInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(launcherIntent, 0)
    }

    fun fetchUpdates(size: Int? = null) {
        println("fetching updates...")
        val realAppInfo = getLaunchableApps()
        val realAppMap = mutableMapOf<String, ResolveInfo>()
        realAppInfo.forEach {realAppMap[AppInfo.getIdFromResolveInfo(it)] = it}
        val realApps = realAppMap.keys

        val cachedApps = allAppsCacheData.data.keys as Set<String>

        if (realApps != cachedApps) {
            val newApps = realApps - cachedApps
            val oldApps = cachedApps - realApps

            oldApps.forEach {
                allAppsCacheData.data.remove(it)
//                mainStageAppsData.remove(it)
            }
//            userStageData.filterValues { it !in oldApps }

            newApps.forEach {
                val resolveInfo = realAppMap[it]
                allAppsCacheData.data[it] = AppCache.createFromResolveInfo(context, resolveInfo!!).apply {encodeIcon(size)}
            }

            println("newApps: ${newApps.size}, oldApps: ${oldApps.size}, now: ${allAppsCacheData.data.size} apps")

            allAppsCacheData.dumpData()

            if (oldApps.isNotEmpty()) {
                userStageData.dumpData()
//                dumpFile(context, mainStageAppsData, MAIN_STAGE_APPS)
            }
        }

    }

    fun createViews(): List<View> {
        return List(userStageData.data.size) { i ->
            val elementData = userStageData.data[i]!!
            when (elementData) {
                is AppData -> AppView(context, createAppInfo(elementData.id)).apply { layoutParams = SnapLayout.SnapLayoutParams(i, 2, 2) }
                is FolderData -> FolderView(context).apply { addApps(List(elementData.ids.size) { createAppInfo(elementData.ids[it]) })
                    layoutParams = SnapLayout.SnapLayoutParams(i, 2, 2) }
                is WidgetData -> TODO()
            }
        }
    }

    fun createAppInfo(id: String): AppInfo {
        val app = allAppsCacheData.data[id] ?: throw LauncherException("id $id is absent in cached allApps")
        return AppInfo(app.packageName, app.name, app.label, app.icon)
    }
}