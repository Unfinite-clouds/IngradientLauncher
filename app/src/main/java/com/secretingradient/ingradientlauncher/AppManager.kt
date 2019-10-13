package com.secretingradient.ingradientlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import com.secretingradient.ingradientlauncher.element.AppInfo

object AppManager {
    private lateinit var sortedApps: List<String>
    private var isSorted: Boolean = false
    private var isLoaded: Boolean = false

    var allApps: MutableMap<String, AppInfo> = mutableMapOf()
        private set(value) {
            isSorted = false
            field = value
        }
        get() { return if (isLoaded) field else throw LauncherException("allApps is not loaded") }

    var customGridApps: MutableMap<Int, String> = mutableMapOf()
        private set
        get() { return if (isLoaded) field else throw LauncherException("customGridApps is not loaded") }

    var mainScreenApps: MutableList<String> = mutableListOf()
        private set
        get() { return if (isLoaded) field else throw LauncherException("mainScreenApps is not loaded") }

    fun loadAllApps(context: Context) {
        println("loading All Apps...")
        allApps = Storable.loadAuto(context, Storable.ALL_APPS) as? MutableMap<String, AppInfo> ?: mutableMapOf()
        customGridApps = Storable.loadAuto(context, Storable.CUSTOM_GRID_APPS) as? MutableMap<Int, String> ?: mutableMapOf()
        mainScreenApps = Storable.loadAuto(context, Storable.MAIN_SCREEN_APPS) as? MutableList<String> ?: mutableListOf()

        isLoaded = true
        checkUpdateAllApps(context)
        allApps.forEach { it.value.loadIconFromDump(context)}
    }

    fun applyCustomGridChanges(context: Context, pos: Int, app: String) {
        if (app == "")
            customGridApps.remove(pos)
        else
            customGridApps[pos] = app
        Storable.dumpAuto(context, customGridApps, Storable.CUSTOM_GRID_APPS)
    }

    fun applyCustomGridChanges(context: Context, apps: Map<Int, String?>) {
        apps.forEach {
            if (it.value == null)
                customGridApps.remove(it.key)
            else
                customGridApps[it.key] = it.value!!
        }
        Storable.dumpAuto(context, customGridApps, Storable.CUSTOM_GRID_APPS)
    }

    fun applyMainScreenChanges(context: Context, apps: MutableList<String>) {
        mainScreenApps = apps
        Storable.dumpAuto(context, mainScreenApps, Storable.MAIN_SCREEN_APPS)
    }

    private fun getLaunchableApps(context: Context): List<ResolveInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(launcherIntent, 0)
    }

    private fun checkUpdateAllApps(context: Context, size: Int? = null) {
        val realAppInfo = getLaunchableApps(context)
        val realAppMap = mutableMapOf<String, ResolveInfo>()
        realAppInfo.forEach {realAppMap[AppInfo.getIdFromResolveInfo(it)] = it}
        val realApps = realAppMap.keys

        val cachedAppInfo = allApps
        val cachedApps = cachedAppInfo.keys as Set<String>

        if (realApps != cachedApps) {
            val newApps = realApps - cachedApps
            val oldApps = cachedApps - realApps

            cachedAppInfo.filterKeys { it !in oldApps }

            newApps.forEach {
                val resolveInfo = realAppMap[it]
                cachedAppInfo[it] = AppInfo.createFromResolveInfo(context, resolveInfo!!).apply {
                    prepareIconToDump(size)
                }
            }

            println("newApps: ${newApps.size}, oldApps: ${oldApps.size}, now: ${cachedAppInfo.size} apps")
            Storable.dumpAuto(context, cachedAppInfo, Storable.ALL_APPS)
        }

        allApps = cachedAppInfo
    }

    fun getSortedApps(): List<String> {
        if (!isSorted)
            sortedApps = allApps.values.sortedBy { it.label }.map { it.id }
        return sortedApps
    }

    fun getApp(id: String): AppInfo? {
        return allApps[id]
    }



}