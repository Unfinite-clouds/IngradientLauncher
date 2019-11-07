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
    var autoDumpEnabled = true

    // this is pool for Bitmaps and data needed to create AppInfo
    /*private*/ val allAppsCacheData = DataHolder<String, AppCache>(context, "all_apps_cache_data", autoDumpEnabled)

    val mainStageData = DataHolder<Int, String>(context, "main_stage_data", autoDumpEnabled)
    val userStageData = DataHolder<Int, ElementData>(context, "user_stage_data", autoDumpEnabled)

    val allAppsIds: MutableList<String>

    init {
        allAppsCacheData.loadData()
        mainStageData.loadData()
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
                allAppsCacheData.data[it] = AppCache(context, resolveInfo!!)
            }

            println("newApps: ${newApps.size}, oldApps: ${oldApps.size}, now: ${allAppsCacheData.data.size} apps")

            allAppsCacheData.dumpData()

            if (oldApps.isNotEmpty()) {
                userStageData.dumpData()
//                dumpFile(context, mainStageAppsData, MAIN_STAGE_APPS)
            }
        }

    }

    private fun getAppCache(id: String): AppCache {
        if (allAppsCacheData.data[id] == null) throw LauncherException("id $id for App not found in allAppsCacheData")
        return allAppsCacheData.data[id]!!
    }

    fun createViews(): List<View> {
        val iter = userStageData.data.iterator()
        return List(userStageData.data.size) { _ ->
            val pair = iter.next()
            val position = pair.key
            val elementData = pair.value
            when (elementData) {
                is AppData -> AppView(context, getAppCache(elementData.id).createAppInfo(context))
                    .apply { layoutParams = SnapLayout.SnapLayoutParams(position, 2, 2) }
                is FolderData -> FolderView(context).apply { addApps(List(elementData.ids.size) { getAppCache(elementData.ids[it]).createAppInfo(context) })
                    layoutParams = SnapLayout.SnapLayoutParams(position, 2, 2) }
                is WidgetData -> TODO()
            }
        }
    }

    interface OnDataChangedListener <K, V>{
        fun onInserted(index: K, item: V, replace: Boolean = false)
        fun onRemoved(index: K)
        fun onMoved(from: K, to: K)
        fun onChanged(newData: MutableMap<K, V>)
    }

    fun createAppInfo(id: String): AppInfo {
        return getAppCache(id).createAppInfo(context)
    }

    class DataHolder <K, V> (val context: Context, val fileName: String, var autoDumpEnabled: Boolean = true): OnDataChangedListener<K, V> {
        lateinit var data: MutableMap<K, V>

        override fun onInserted(index: K, item: V, replace: Boolean) {
            if (!replace && data.containsKey(index))
                throw LauncherException("attempt to rewrite element ${data[index]} at index $index")

            data[index] = item

            if (autoDumpEnabled)
                dumpData()
        }

        override fun onRemoved(index: K) {
            data.remove(index)

            if (autoDumpEnabled)
                dumpData()
        }

        override fun onMoved(from: K, to: K) {
            if (from == to)
                return
            if (data.containsKey(to))
                throw LauncherException("attempt to rewrite element ${data[to]} at index $to")
            if (!data.containsKey(from))
                throw LauncherException("attempt to move null element at index $from")

            data[to] = data[from]!!
            data.remove(from)

            if (autoDumpEnabled)
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
            var loaded: MutableMap<K, V>? = null
            try {
                stream = ObjectInputStream(context.openFileInput(fileName))
                loaded = stream.readObject() as? MutableMap<K, V>
            } catch (e: FileNotFoundException) {
                if (BuildConfig.DEBUG) println("file not found ${context.filesDir.absolutePath}/$fileName")
            }
            data = loaded ?: mutableMapOf()

            stream?.close()
        }

        fun deleteFile() {
            context.deleteFile(fileName)
        }
    }

    class AppCache(context: Context, resolveInfo: ResolveInfo) : Serializable {
        companion object {private const val serialVersionUID = 44010L}

         val id: String
         val packageName: String
         val name: String
         val label: String
         var iconByteArray: ByteArray

        init {
            val pm = context.packageManager
            val activity = resolveInfo.activityInfo

            packageName = activity.packageName
            name = activity.name
            id = "${packageName}_$name" // .split('_')
            label = activity.loadLabel(pm).toString()
            iconByteArray = encodeIcon(activity.loadIcon(pm))
        }

        private fun encodeIcon(bitmap: Drawable, size: Int? = null): ByteArray {
            return encodeBitmap(if (size == null) bitmap.toBitmap() else bitmap.toBitmap(size, size))
        }

        fun decodeIcon(context: Context): BitmapDrawable {
            return BitmapDrawable(context.resources, decodeBitmap(iconByteArray))
        }

        fun createAppInfo(context: Context): AppInfo {
            return AppInfo(packageName, name, label, decodeIcon(context))
        }

/*        fun getIdFromResolveInfo(resolveInfo: ResolveInfo): String {
            return "${resolveInfo.activityInfo.packageName}_${resolveInfo.activityInfo.name}"
        }*/
    }



}