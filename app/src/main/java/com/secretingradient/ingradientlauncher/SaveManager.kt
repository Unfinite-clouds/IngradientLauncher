package com.secretingradient.ingradientlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.util.SparseArray
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.FolderInfo
import com.secretingradient.ingradientlauncher.element.WidgetInfo
import java.io.*

typealias AllAppsT = MutableMap<String, AppInfo>
typealias MainStageAppsT = MutableList<String>
typealias UserStageAppsT = SparseArray<String>
typealias UserStageFoldersT = SparseArray<FolderInfo>
typealias UserStageWidgetsT = SparseArray<WidgetInfo>
fun AllAppsT(): AllAppsT {
    return mutableMapOf()
}
fun MainStageAppsT(): MainStageAppsT {
    return mutableListOf()
}

object SaveManager {
    data class FileInfo<T>(val fileName: String)

    //    val TEST = FileInfo2<MutableMap<String, AppInfo>>("test")
    val ALL_APPS = FileInfo<AllAppsT>("ALL_APPS")
    val MAIN_STAGE_APPS = FileInfo<MainStageAppsT>("MAIN_STAGE_APPS")
    val USER_STAGE_APPS = FileInfo<UserStageAppsT>("USER_STAGE_APPS")

    private fun load(inputStream: FileInputStream): Any? {
        val loaded: Any?
        var objIn: ObjectInputStream? = null
        try {
            objIn = ObjectInputStream(inputStream)
            loaded = objIn.readObject()
        } finally {
            objIn?.close()
        }
        return loaded
    }

    private fun <T> loadFile(context: Context, fileInfo: FileInfo<T>): T? {
        val a: T?
        var inputStream: InputStream? = null
        try {
            inputStream = context.openFileInput(fileInfo.fileName)
            a = load(inputStream) as T?
        } catch (e: FileNotFoundException) {
            println(e.message)
            return null
        } finally {
            inputStream?.close()
        }
        return a
    }

    private fun dumpFile(context: Context, obj: Any, fileInfo: FileInfo<*>) {
        ObjectOutputStream(context.openFileOutput(fileInfo.fileName, Context.MODE_PRIVATE)).use { it.writeObject(obj) }
    }

    fun deleteFile(context: Context, file: FileInfo<*>) {
        context.deleteFile(file.fileName)
    }

    lateinit var allApps: AllAppsT
    lateinit var allAppsSorted: List<String>

    lateinit var mainStageApps: MainStageAppsT

    lateinit var userStageApps: UserStageAppsT

    fun init(context: Context) {
        println("loading All Apps...")
        allApps = loadFile(context, ALL_APPS) ?: AllAppsT()
        mainStageApps = loadFile(context, MAIN_STAGE_APPS) ?: MainStageAppsT()
        userStageApps = loadFile(context, USER_STAGE_APPS) ?: UserStageAppsT()

        checkUpdateAllApps(context)
        allApps.forEach { it.value.loadIconFromDump(context)}
    }

/*    fun dumpUserStageApps(context: Context, pos: Int, app: String) {
        if (app == "")
            userStageApps.remove(pos)
        else
            userStageApps[pos] = app
        dumpFile(context, userStageApps, USER_STAGE_APPS)
    }*/

    fun dumpUserStageApps(context: Context) {
/*        apps.forEach {
            if (it.value == null)
                userStageApps.remove(it.key)
            else
                userStageApps[it.key] = it.value!!
        }*/
        dumpFile(context, userStageApps, USER_STAGE_APPS)
    }

    fun dumpMainStageApps(context: Context) {
        dumpFile(context, mainStageApps, MAIN_STAGE_APPS)
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
            // TODO: also update userStageApps and mainStageApps + resave files

            newApps.forEach {
                val resolveInfo = realAppMap[it]
                cachedAppInfo[it] = AppInfo.createFromResolveInfo(context, resolveInfo!!).apply {
                    prepareIconToDump(size)
                }
            }

            println("newApps: ${newApps.size}, oldApps: ${oldApps.size}, now: ${cachedAppInfo.size} apps")
            dumpFile(context, cachedAppInfo, ALL_APPS)
        }

        allApps = cachedAppInfo
        allAppsSorted = allApps.values.sortedBy { it.label }.map { it.id }
    }

    fun getApp(id: String): AppInfo? {
        return allApps[id]
    }
}