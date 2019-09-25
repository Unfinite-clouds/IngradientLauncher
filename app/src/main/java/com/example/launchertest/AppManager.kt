package com.example.launchertest

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import com.example.launchertest.launcher_skeleton.AppInfo
import com.example.launchertest.launcher_skeleton.Storable

object AppManager {
    private var allApps: MutableMap<String, AppInfo>? = null

    fun getAllApps(context: Context): MutableMap<String, AppInfo> {
        return allApps ?: loadAllApps(context)
    }

    private fun loadAllApps(context: Context) : MutableMap<String, AppInfo> {
        println("loading All Apps...")
        return Storable.loadAuto(context, Storable.SORTED_ALL_APPS) as? MutableMap<String, AppInfo> ?: mutableMapOf<String, AppInfo>()
    }

    fun getLaunchableApps(context: Context): List<ResolveInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(launcherIntent, 0)
    }

    fun updateCachedApps(context: Context): MutableMap<String, AppInfo> {
        val realAppInfo = getLaunchableApps(context)
        val realAppMap = mutableMapOf<String, ResolveInfo>()
        realAppInfo.forEach {realAppMap[AppInfo.getIdFromResolveInfo(it)] = it}
        val realApps = realAppMap.keys

        val cachedAppInfo = getAllApps(context)
        val cachedApps = cachedAppInfo.keys as Set<String>

        if (realApps != cachedApps) {
            val newApps = realApps - cachedApps
            val oldApps = cachedApps - realApps

            cachedAppInfo.filterKeys { it !in oldApps }

            newApps.forEach {
                val resolveInfo = realAppMap[it]
                cachedAppInfo[it] = AppInfo.createFromResolveInfo(context, resolveInfo!!).apply {
                    prepareIconToDump(100)
                }
            }

            Storable.dumpAuto(context, cachedAppInfo, Storable.SORTED_ALL_APPS)
        }

        cachedAppInfo.forEach {it.value.loadIconFromDump(context)}

        return cachedAppInfo
    }

    fun getSortedApps(context: Context): List<String> {
        return getAllApps(context).values.sortedBy { it.label }.map { it.id }
    }

}