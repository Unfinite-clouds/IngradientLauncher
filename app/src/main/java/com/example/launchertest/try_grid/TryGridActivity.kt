package com.example.launchertest.try_grid

import android.content.ClipData
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.iterator
import androidx.core.view.setMargins
import com.example.launchertest.AppInfo
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.activity_try_grid.*


class TryGridActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener, View.OnDragListener, View.OnLongClickListener {
    lateinit var enterShape: Drawable
    lateinit var normalShape: Drawable

    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
            }
            DragEvent.ACTION_DRAG_ENTERED -> view?.setBackgroundDrawable(enterShape)
            DragEvent.ACTION_DRAG_EXITED -> view?.setBackgroundDrawable(normalShape)
            DragEvent.ACTION_DROP -> {
                // Dropped, reassign View to ViewGroup
                val view2 = event.localState as View
                val owner = view2.parent as ViewGroup
                owner.removeView(view2)
                val container = try_grid
                container.addView(view2)
                view2.visibility = View.VISIBLE
            }
            DragEvent.ACTION_DRAG_ENDED -> view?.setBackgroundDrawable(normalShape)
            else -> {
                // do nothing
            }
        }
        return true
    }

    override fun onLongClick(view: View?): Boolean {
        val data = ClipData.newPlainText("asdsad", "testtsetcse")
        val shadowBuilder = View.DragShadowBuilder(view)
        view?.startDrag(data, shadowBuilder, view, 0)
        view?.visibility = View.INVISIBLE
        return true
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        println(item?.intent)
        return true
    }

    lateinit var allApps: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_grid)

        enterShape = resources.getDrawable(R.drawable.ic_info)
        normalShape = resources.getDrawable(R.drawable.ic_delete)

        try_grid.setOnDragListener(this)

        try_grid.columnCount = 5
        try_grid.rowCount = 7

        allApps = getAllAppsList(this)

        val grid = findViewById<GridLayout>(R.id.try_grid)

        for (item in grid.iterator()) {
            item.setOnLongClickListener(this)
            item.setOnDragListener(this)
        }


/*        for (i in 8..20) {
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
        }*/

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
*/

    override fun onResume() {

        super.onResume()
        println("onResume")
        val lp = GridLayout.LayoutParams()
        lp.setMargins(20)
        try_grid_space.layoutParams = lp
        try_grid_space.layoutParams.width = 144
        try_grid_space.layoutParams.height = 144
        try_grid_space.setBackgroundResource(R.color.colorAccent)
        try_grid_space.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            println("$left, $top, $right, $bottom")
        }
    }
/*
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
