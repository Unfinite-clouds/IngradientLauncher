package com.secretingradient.ingradientlauncher.data

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.secretingradient.ingradientlauncher.*
import java.io.Serializable

class DataKeeper(val context: Context, val widgetHost: WidgetHost) {

    // this is pool for Bitmaps and data needed to create AppInfo
    private val allAppsCacheData: FileDataset<String, AppCache>  // deprecated
    private val iconCache: FileDataset<String, DrawableCache>
    private val widgetPreviewCache: FileDataset<String, DrawableCache>
    val iconDrawables = mutableMapOf<String, Drawable?>()
    val widgetPreviewDrawables = mutableMapOf<String, Drawable?>()
    val mainStageDataset: DatasetList<AppData, AppInfo>
    val userStageDataset: Dataset<Data, Info>
    val allAppsList: List<AppInfo>

    val allAppsIds: MutableList<String>

    init {
//        resetAllCache()

        allAppsCacheData = FileDataset(context,"all_apps_cache_data")  // deprecated
        iconCache = FileDataset(context, "icon_cache")
        widgetPreviewCache = FileDataset(context, "widget_preview_cache")
        fetchUpdates()
        fetchIcons(false)
        fetchWidgetPreviews(false)
        loadIconsFromCache()
        loadWidgetPreviewsFromCache()
        allAppsCacheData.rawDataset.forEach { it.value.loadIcon(context) }  // deprecated
        allAppsIds = allAppsCacheData.rawDataset.keys.toMutableList()  // deprecated

        mainStageDataset = DatasetList(this, "main_stage_data")
        userStageDataset = Dataset(this, "user_stage_data")
        allAppsList = List(allAppsCacheData.rawDataset.size) {  // deprecated
            createAppInfo(allAppsIds[it])
        }.sortedBy { it.label }
    }

    fun fetchUpdates(size: Int? = null) {
        println("fetching apps updates...")
        val realAppInfo = getLaunchableApps()
        val realAppMap = mutableMapOf<String, ResolveInfo>()
        realAppInfo.forEach {realAppMap[AppCache.getIdFromResolveInfo(it)] = it}
        val realApps = realAppMap.keys
        val cachedApps = allAppsCacheData.rawDataset.keys as Set<String>
        if (realApps != cachedApps) {
            val newApps = realApps - cachedApps
            val oldApps = cachedApps - realApps
            oldApps.forEach {
                allAppsCacheData.rawDataset.remove(it)
            }
            newApps.forEach {
                val resolveInfo = realAppMap[it]
                allAppsCacheData.rawDataset[it] = AppCache(context, resolveInfo!!)
            }
            println("newApps: ${newApps.size}, oldApps: ${oldApps.size}, now: ${allAppsCacheData.rawDataset.size} apps")
            allAppsCacheData.dumpData()
            if (oldApps.isNotEmpty()) {
//                mainStageDataset.dumpData()
            }
        }
    }

    fun fetchIcons(forceReset: Boolean = false) {
        println("fetching icons...")
        // currently rewrites all data
        val installedApps = getLaunchableApps()
        val installedAppsKeys = installedApps.map { getAppId(it) }.toSet()

        if (installedAppsKeys != iconCache.rawDataset.keys || forceReset) {
            println("reset...")
            iconDrawables.clear()
            iconCache.clearData()
            var id: String
            var drawable: Drawable?
            for (it in installedApps) {
                id = getAppId(it)
                drawable = it.loadIcon(context.packageManager)
                if (drawable == null) {
                    println("WARNING: failed to load icon for app $id. skipping")
                    widgetPreviewCache.rawDataset[id] = DrawableCache()
                } else
                    iconCache.rawDataset[id] = DrawableCache(drawable)
                iconDrawables[id] = drawable
            }
            iconCache.dumpData()
        }
    }

    fun fetchWidgetPreviews(forceReset: Boolean = false) {
        println("fetching widgets...")
        // currently rewrites all data
        // need to check for icons
        val installedProviders = AppWidgetManager.getInstance(context).installedProviders
        val installedProvidersKeys = installedProviders.map { getWidgetId(it) }.toSet()

        if (installedProvidersKeys != widgetPreviewCache.rawDataset.keys || forceReset) {
            println("reset...")
            widgetPreviewDrawables.clear()
            widgetPreviewCache.clearData()
            val density = getDensity(context)
            var id: String
            var drawable: Drawable?
            for (it in installedProviders) {
                id = getWidgetId(it)
                drawable = loadWidgetPreview(context, density, it)
                if (drawable == null) {
                    println("WARNING: failed to load preview for widget $id. skipping")
                    widgetPreviewCache.rawDataset[id] = DrawableCache()
                } else
                    widgetPreviewCache.rawDataset[id] = DrawableCache(drawable)
                widgetPreviewDrawables[id] = drawable
            }
            widgetPreviewCache.dumpData()
        }
    }

    fun getAppId(resolveInfo: ResolveInfo) = resolveInfo.activityInfo.packageName + "_" + resolveInfo.activityInfo.name

    fun getWidgetId(providerInfo: AppWidgetProviderInfo) = providerInfo.provider.flattenToString()

    fun loadWidgetPreview(context: Context, density: Int, providerInfo: AppWidgetProviderInfo): Drawable? {
        val resources: Resources = context.packageManager.getResourcesForApplication(providerInfo.provider.packageName)
        try {
            if (providerInfo.previewImage != 0)
                return ResourcesCompat.getDrawableForDensity(resources, providerInfo.previewImage, density, null)
        } catch (e: Resources.NotFoundException) { }
        return null
    }

    fun loadWidgetIcon(density: Int, providerInfo: AppWidgetProviderInfo): Drawable? {
        val resources: Resources = context.packageManager.getResourcesForApplication(providerInfo.provider.packageName)
        try {
            return ResourcesCompat.getDrawableForDensity(resources, providerInfo.icon, density, null)
        } catch (e: Resources.NotFoundException) { }
        return null
    }

    fun loadIconsFromCache() {
        iconCache.rawDataset.forEach {
            iconDrawables[it.key] = it.value.loadDrawable(context)
        }
    }

    fun loadWidgetPreviewsFromCache() {
        widgetPreviewCache.rawDataset.forEach {
            widgetPreviewDrawables[it.key] = it.value.loadDrawable(context)
        }
    }

    private fun getLaunchableApps(): List<ResolveInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(launcherIntent, 0)
    }

    private fun getAppCache(id: String): AppCache {
        return allAppsCacheData.rawDataset[id] ?: throw LauncherException("not found id $id in allAppsCacheData")
    }

    fun createAppInfo(id: String): AppInfo {
        return getAppCache(id).createAppInfo()
    }

    fun createAppInfo(i: Int): AppInfo {
        return getAppCache(allAppsIds[i]).createAppInfo()
    }

    fun getAppIcon(id: String): Drawable {
        return getAppCache(id).icon ?: throw LauncherException("icon has not decoded")
    }

    fun getWidgetProviderInfo(providerId: String): AppWidgetProviderInfo {
        val widgetInfo = AppWidgetManager.getInstance(context).installedProviders.find { it.provider.flattenToString() == providerId }
            ?: throw LauncherException("not found widget for providerId $providerId. Was it uninstalled?")
        return widgetInfo
    }

    fun resetAllCache() {
        context.deleteFile("all_apps_cache_data")
        context.deleteFile("icon_cache")
        context.deleteFile("widget_preview_cache")
//        allAppsCacheData.deleteFile()
//        iconCache.deleteFile()
//        widgetPreviewCache.deleteFile()
    }

    class AppCache(context: Context, resolveInfo: ResolveInfo) : Serializable {
        private val packageName: String
        private val name: String
        private val label: String
        private var iconByteArray: ByteArray?
        var icon: Drawable? = null

        init {
            // +prepare for dump
            val pm = context.packageManager
            val activity = resolveInfo.activityInfo
            packageName = activity.packageName
            name = activity.name
            label = activity.loadLabel(pm).toString()
            iconByteArray = encodeIcon(activity.loadIcon(pm))
        }

        fun loadIcon(context: Context) {
            icon = decodeIcon(context)
            iconByteArray = null
        }

        fun createAppInfo(): AppInfo {
            return AppInfo(packageName, name, label, icon!!)
        }

        private fun encodeIcon(bitmap: Drawable, size: Int? = null): ByteArray {
            return encodeBitmap(if (size == null) bitmap.toBitmap() else bitmap.toBitmap(size, size))
        }

        private fun decodeIcon(context: Context): BitmapDrawable {
            return BitmapDrawable(context.resources, decodeBitmap(iconByteArray!!))
        }

        companion object {
            private const val serialVersionUID = 44000L

            fun getIdFromResolveInfo(resolveInfo: ResolveInfo): String {
                return "${resolveInfo.activityInfo.packageName}_${resolveInfo.activityInfo.name}"
            }
        }
    }

    class DrawableCache(val iconByteArray: ByteArray = ByteArray(0)) : Serializable {
        constructor(drawable: Drawable, width: Int = drawable.intrinsicWidth, height: Int = drawable.intrinsicHeight) : this(encodeBitmap(drawable.toBitmap(width, height)))
        fun loadDrawable(context: Context): BitmapDrawable? {
            if (iconByteArray.isEmpty())
                return null
            return BitmapDrawable(context.resources, decodeBitmap(iconByteArray))
        }
    }


}