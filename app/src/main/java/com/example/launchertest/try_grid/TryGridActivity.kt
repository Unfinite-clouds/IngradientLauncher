package com.example.launchertest.try_grid

import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.iterator
import androidx.core.view.setMargins
import com.example.launchertest.AppInfo
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.activity_try_grid.*


class TryGridActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener {
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        println(item?.intent)
        return true
    }

    lateinit var allApps: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_grid)

        try_grid.columnCount = 5
        try_grid.rowCount = 7

        allApps = getAllAppsList(this)

        val grid = findViewById<GridLayout>(R.id.try_grid)


        for (i in 8..20) {
            val lpars = GridLayout.LayoutParams()
            lpars.width = 144
            lpars.height = 144
            lpars.setMargins(20)
            lpars.setGravity(11)

            val img = ImageView(this)
            img.setOnLongClickListener {
                createPopupMenu(it)
                return@setOnLongClickListener true
            }
            img.layoutParams = lpars
            img.setBackgroundDrawable(allApps[i].icon)
//            img.setBackgroundResource(android.R.color.holo_green_dark)
            grid.addView(img)
        }
    }

    fun createPopupMenu(view: View) {
        val builder = MenuBuilder(view.context)
        val inflater = MenuInflater(view.context)
        inflater.inflate(R.menu.shortcut_popup_menu, builder)
        for (item in builder.iterator()) {
            item.setOnMenuItemClickListener(this)
        }
        //                val menu = PopupMenu(this, it)
        //                menu.inflate(R.menu.shortcut_popup_menu)
        //                menu.menuInflater.inflate(R.menu.shortcut_popup_menu, menu.menu)
        //                menu.setOnMenuItemClickListener(this)
        val menuHelper = MenuPopupHelper(view.context, builder, view)
        menuHelper.setForceShowIcon(true)
        menuHelper.show()
    }

/*
    override fun onContentChanged() {
        super.onContentChanged()
        println("onContentChanged")
    }

    override fun onStart() {
        super.onStart()
        println("onStart")
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        println("onAttachedToWindow")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println("onSaveInstanceState")
    }
*/
}
