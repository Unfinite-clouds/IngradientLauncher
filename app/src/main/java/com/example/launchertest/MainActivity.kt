package com.example.launchertest

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var iconContainer: LinearLayout
    private lateinit var hScrollView: HorizontalScrollView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_scroll)
        iconContainer = findViewById<LinearLayout>(R.id.iconContainer)
        hScrollView = findViewById<HorizontalScrollView>(R.id.scrollView)

        getAllAppsList(applicationContext)

        for (i in 1..8) {
            iconContainer.addView(IconFactory(applicationContext, 6).createIcon(allApps[i]))
        }
    }

    override fun onBackPressed() {

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        return super.onTouchEvent(event)
    }
}

data class AppInfo (var label: CharSequence, var packageName: String, var icon: Drawable)

private var allApps = ArrayList<AppInfo>()
var invalid = true

fun getAllAppsList(context: Context) : ArrayList<AppInfo> {
    if (!invalid) return allApps

    println("fetching All Apps...")
    val pm = context.packageManager

    val i = Intent(Intent.ACTION_MAIN, null)
    i.addCategory(Intent.CATEGORY_LAUNCHER)
    val riList = pm.queryIntentActivities(i, 0)

    for (ri in riList) {
        val app = AppInfo(ri.loadLabel(pm), ri.activityInfo.packageName, ri.activityInfo.loadIcon(pm))
        allApps.add(app)
    }
    invalid = false


    // way 2 (may be more efficient)
/*    val launcherIntent = Intent().apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    pm.getInstalledApplications(0).forEach { appInfo ->
        launcherIntent.`package` = appInfo.packageName
        // only show launch-able apps
        if (pm.queryIntentActivities(launcherIntent, 0).size > 0) {
            allApps.add(AppInfo(appInfo.loadLabel(pm), appInfo.packageName, appInfo.loadIcon(pm)))
        }
    }*/

    return allApps
}


