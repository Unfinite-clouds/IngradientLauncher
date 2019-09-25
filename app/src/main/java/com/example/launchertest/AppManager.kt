package com.example.launchertest

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import com.example.launchertest.launcher_skeleton.AppInfo
import com.example.launchertest.launcher_skeleton.Storable

object AppManager {
    private lateinit var sortedApps: List<String>
    private var isSorted: Boolean = false
    private var isLoaded: Boolean = false
    var allApps: MutableMap<String, AppInfo> = mutableMapOf()
        private set(value) {
            isSorted = false
            field = value
        }
        get() { return if (isLoaded) field else throw LauncherException("allApps is not laded") }

    var customGridApps: MutableMap<String, Int> = mutableMapOf()

    fun loadAllApps(context: Context) {
        println("loading All Apps...")
        allApps = Storable.loadAuto(context, Storable.ALL_APPS) as? MutableMap<String, AppInfo> ?: mutableMapOf()
        isLoaded = true
        if (allApps.isEmpty()) {
            updateAllApps(context)
        }
        allApps.forEach { it.value.loadIconFromDump(context)}
        customGridApps = Storable.loadAuto(context, Storable.CUSTOM_GRID_APPS) as? MutableMap<String, Int> ?: mutableMapOf()
    }

    fun getLaunchableApps(context: Context): List<ResolveInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(launcherIntent, 0)
    }

    fun updateAllApps(context: Context, size: Int? = null) {
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