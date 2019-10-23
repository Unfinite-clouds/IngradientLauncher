package com.secretingradient.ingradientlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import com.secretingradient.ingradientlauncher.element.AppInfo
import com.secretingradient.ingradientlauncher.element.ElementInfo
import com.secretingradient.ingradientlauncher.element.FolderInfo
import com.secretingradient.ingradientlauncher.element.WidgetInfo
import java.io.*

typealias AppPoolT = MutableMap<String, AppInfo>
typealias MainStageAppsT = MutableList<String>
typealias UserStageAppsT = MutableMap<Int, String>
typealias UserStageFoldersT = MutableMap<Int, MutableList<String>>
typealias UserStageWidgetsT = MutableMap<Int, WidgetInfo>
fun AppPoolT(): AppPoolT {
    return mutableMapOf()
}
fun MainStageAppsT(): MainStageAppsT {
    return mutableListOf()
}
fun UserStageAppsT(): UserStageAppsT {
    return mutableMapOf()
}
fun UserStageFoldersT(): UserStageFoldersT {
    return mutableMapOf()
}

/**
 DataKeeper is responsible for load/dump data from/to files
 Allocates new objects. Only DataKeeper should create new objects
 Contains appsPool - AppInfo prototypes
 */
object DataKeeper {
    data class FileInfo<T>(val fileName: String)

    //    val TEST = FileInfo2<MutableMap<String, AppInfo>>("test")
    val ALL_APPS = FileInfo<AppPoolT>("ALL_APPS")
    val MAIN_STAGE_APPS = FileInfo<MainStageAppsT>("MAIN_STAGE_APPS")
    val USER_STAGE_APPS = FileInfo<UserStageAppsT>("USER_STAGE_APPS")
    val USER_STAGE_FOLDERS = FileInfo<UserStageFoldersT>("USER_STAGE_FOLDERS")

    private lateinit var appsPool: AppPoolT
    val allApps = mutableMapOf<Int, String>()
    lateinit var mainStageAppsData: MainStageAppsT
    lateinit var userStageAppsData: UserStageAppsT
    lateinit var userStageFoldersData: UserStageFoldersT

    fun init(context: Context) {
        appsPool = loadFile(context, ALL_APPS) ?: AppPoolT()
        mainStageAppsData = loadFile(context, MAIN_STAGE_APPS)?: MainStageAppsT()
        userStageAppsData = loadFile(context, USER_STAGE_APPS)?: UserStageAppsT()
        userStageFoldersData = loadFile(context, USER_STAGE_FOLDERS)?: UserStageFoldersT()
        // todo widgets

        fetchUpdates(context)
        appsPool.keys.toList().forEachIndexed {i, id ->
            allApps[i] = id
        }
        appsPool.forEach { it.value.loadIconFromDump(context) }
    }

    private fun getLaunchableApps(context: Context): List<ResolveInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(launcherIntent, 0)
    }

    fun fetchUpdates(context: Context, size: Int? = null) {
        println("fetching updates...")
        val realAppInfo = getLaunchableApps(context)
        val realAppMap = mutableMapOf<String, ResolveInfo>()
        realAppInfo.forEach {realAppMap[AppInfo.getIdFromResolveInfo(it)] = it}
        val realApps = realAppMap.keys

        val cachedAppInfo = appsPool
        val cachedApps = cachedAppInfo.keys as Set<String>

        if (realApps != cachedApps) {
            val newApps = realApps - cachedApps
            val oldApps = cachedApps - realApps

            oldApps.forEach {
                cachedAppInfo.remove(it)
                mainStageAppsData.remove(it)
            }
            userStageAppsData.filterValues { it !in oldApps }

            newApps.forEach {
                val resolveInfo = realAppMap[it]
                cachedAppInfo[it] = AppInfo.createFromResolveInfo(context, resolveInfo!!).apply {
                    prepareIconToDump(size)
                }
            }

            println("newApps: ${newApps.size}, oldApps: ${oldApps.size}, now: ${cachedAppInfo.size} apps")

            appsPool = cachedAppInfo // is it already the same?

            dumpFile(context, appsPool, ALL_APPS)
            if (oldApps.isNotEmpty()) {
                dumpFile(context, userStageAppsData, USER_STAGE_APPS)
                dumpFile(context, mainStageAppsData, MAIN_STAGE_APPS)
            }
        }

//        allAppsSorted = allApps.values.sortedBy { it.label }.map { it.id }
    }

    fun getAppInfoById(id: String): AppInfo? {
        return appsPool[id]
    }

    fun getMainStageApps(): MutableList<AppInfo> {
        return MutableList(mainStageAppsData.size) { i ->
            appsPool[mainStageAppsData[i]] ?:
            throw LauncherException("attempt to get nonexistent AppInfo. app ${mainStageAppsData[i]} is absent in appsPool (appsPool.size=${appsPool.size})")
        }
    }

    fun createUserStageElements(): MutableMap<Int, ElementInfo> {
        val map = mutableMapOf<Int, ElementInfo>()
        userStageAppsData.forEach {
            map[it.key] = appsPool[it.value] ?:
                    throw LauncherException("attempt to get nonexistent AppInfo. app ${userStageAppsData[it.key]} is absent in appsPool (appsPool.size=${appsPool.size})")
        }
        userStageFoldersData.forEach {
            if (map[it.key] != null)
                throw LauncherException("attempt to rewrite element. place ${it.key} is already busy for ${map[it.key]}")

            map[it.key] = createFolderInfo(it.value)
        }
        // todo widgets
        return map
    }

    fun createAllWidgets() {
        TODO("not impl")
    }

    fun dumpUserStageApps(context: Context, elements: MutableMap<Int, ElementInfo>) {
        userStageAppsData = UserStageAppsT()
        elements.forEach {
            if (it.value is AppInfo) {
                userStageAppsData[it.key] = (it.value as AppInfo).id
            }
        }
        dumpFile(context, userStageAppsData, USER_STAGE_APPS)
    }

    fun dumpUserStageApps(context: Context) {
        dumpFile(context, userStageAppsData, USER_STAGE_APPS)
    }

    fun dumpMainStageApps(context: Context) {
        dumpFile(context, mainStageAppsData, MAIN_STAGE_APPS)
    }

    fun dumpUserStageFolders(context: Context, elements: MutableMap<Int, ElementInfo>) {
        userStageFoldersData = UserStageFoldersT()
        elements.forEach {
            if (it.value is FolderInfo) {
                userStageFoldersData[it.key] = createFolderData((it.value as FolderInfo))
            }
        }
        dumpFile(context, userStageFoldersData, USER_STAGE_FOLDERS)
    }

    fun dumpUserStageWidgets(context: Context, elements: MutableMap<Int, ElementInfo>) {
        TODO("not impl")
    }

    fun dumpUserStageElementsAll(context: Context, elements: MutableMap<Int, ElementInfo>) {
        userStageAppsData = UserStageAppsT()
        userStageFoldersData = UserStageFoldersT()
        // todo widgets

        elements.forEach {
            when (val element = it.value) {
                is AppInfo -> {
                    userStageAppsData[it.key] = element.id
                }
                is FolderInfo -> {
                    userStageFoldersData[it.key] = createFolderData(element)
                }
                is WidgetInfo -> {
                    TODO("not impl")
                }
            }
        }

        dumpFile(context, userStageAppsData, USER_STAGE_APPS)
        dumpFile(context, userStageFoldersData, USER_STAGE_FOLDERS)
        // todo widgets
    }

    fun createFolderInfo(appsData: List<String>): FolderInfo {
        return FolderInfo (
            MutableList<AppInfo>(appsData.size) {i ->
                appsPool[appsData[i]] ?:
                throw LauncherException("attempt to get nonexistent AppInfo. app ${appsData[i]} is absent in appsPool (appsPool.size=${appsPool.size})")
            }
        )
    }

    private fun createFolderData(folderInfo: FolderInfo): MutableList<String> {
        return MutableList(folderInfo.apps.size) {i ->
            folderInfo.apps[i].id
        }
    }

    private fun loadData(inputStream: FileInputStream): Any? {
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
            a = loadData(inputStream) as T?
        } catch (e: FileNotFoundException) {
            println(e.message)
            return null
        } finally {
            inputStream?.close()
        }
        return a
    }

    private fun <T> dumpFile(context: Context, obj: T, fileInfo: FileInfo<T>) {
        ObjectOutputStream(context.openFileOutput(fileInfo.fileName, Context.MODE_PRIVATE)).use { it.writeObject(obj) }
    }

    fun deleteFile(context: Context, file: FileInfo<*>) {
        context.deleteFile(file.fileName)
    }
}