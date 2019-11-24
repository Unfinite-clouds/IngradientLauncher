package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.stage.UserStage

class LauncherActivity : AppCompatActivity() {
    val REQUSET_BIND_WIDGET = 0
    val REQUSET_CONFIGURE_WIDGET = 1

    lateinit var context: Context
    lateinit var launcherRootLayout: LauncherRootLayout
    lateinit var dragLayer: DragLayer
    lateinit var dataKeeper: DataKeeper
    lateinit var widgetHost: WidgetHost
    lateinit var widgetManager: AppWidgetManager
    lateinit var gestureHelper: GestureHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_root)

        context = this.applicationContext
        widgetHost = WidgetHost(this)
        dataKeeper = DataKeeper(this, widgetHost)
        widgetManager = AppWidgetManager.getInstance(context)
        gestureHelper = GestureHelper(this)

        fillData(dataKeeper)
        val a = widgetManager.installedProviders[1].provider.packageName
        val prov = widgetManager.installedProviders.find { it.provider.packageName == a }
        println(prov?.provider?.className)
        println(widgetHost.appWidgetIds?.contentToString())
        println(prov?.provider?.className)
//        widgetHost.appWidgetIds.forEach {
//            widgetHost.deleteAppWidgetId(it)
//        }


        getPrefs(this).edit { putBoolean(Preferences.FIRST_LOAD, true) }
        if (getPrefs(this).getBoolean(Preferences.FIRST_LOAD, true))
            Preferences.loadDefaultPreferences(this)

        launcherRootLayout = findViewById(R.id.launcher_root)
        dragLayer = findViewById(R.id.drag_layer)
        launcherRootLayout.initViewPager(findViewById(R.id.root_viewpager), dataKeeper)

    }

    fun requestCreateWidget(widgetInfo: AppWidgetProviderInfo) {
        createWidget(widgetInfo)
    }

    private fun fillData(dk: DataKeeper) {
        dk.mainStageDataset.deleteFile()
        for (i in 0 until 12) {
            dk.mainStageDataset.add(i, dk.createAppInfo(i))
        }

//        dk.userStageDataset.deleteFile()
//        for (i in 0 until 24) {
//            val v = i / 2 * 2 + i/14*14
//            dk.userStageDataset.put(v, dk.createAppInfo(i), true)
//        }
    }

    override fun onBackPressed() {
        super.onBackPressed()  // do back stack
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        printlnClass("onResult")
        super.onActivityResult(requestCode, resultCode, data)
        val widgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: return
        val widgetInfo: AppWidgetProviderInfo = widgetManager.getAppWidgetInfo(widgetId) ?: return

        when (requestCode) {
            REQUSET_BIND_WIDGET -> configureWidgetIfNeeded(widgetId, widgetInfo)
            REQUSET_CONFIGURE_WIDGET -> addWidget(widgetId, widgetInfo)
        }
    }

    private fun createWidget(widgetInfo: AppWidgetProviderInfo) {
        val wid = widgetHost.allocateAppWidgetId()
        bindWidget(wid, widgetInfo)
    }

    private fun bindWidget(widgetId: Int, widgetInfo: AppWidgetProviderInfo) {
        val isBindAllowed = widgetManager.bindAppWidgetIdIfAllowed(widgetId, widgetInfo.provider)
        println("bind is allowed: $isBindAllowed")

        if (!isBindAllowed) {
            println("asking for permission...")
            val intentBind = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
            intentBind.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intentBind.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, widgetInfo.provider)
            startActivityForResult(intentBind, REQUSET_BIND_WIDGET)
        } else {
            configureWidgetIfNeeded(widgetId, widgetInfo)
        }
    }

    private fun configureWidgetIfNeeded(widgetId: Int, wInfo: AppWidgetProviderInfo) {
        if (wInfo.configure != null) {
            println("starting configure activity...")
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = wInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            startActivityForResult(intent, REQUSET_CONFIGURE_WIDGET)
        } else {
            addWidget(widgetId, wInfo)
        }
    }

    private fun addWidget(widgetId: Int, widgetInfo: AppWidgetProviderInfo){
//        val hostView = widgetHost.createView(context, widgetId, widgetInfo)
        (launcherRootLayout.stages[1] as UserStage).onAddWidget(widgetInfo, widgetId)
    }

    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        widgetHost.stopListening()
    }

    class PageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
//        println("Transfroming: $relativePosition")
        }
    }
}

