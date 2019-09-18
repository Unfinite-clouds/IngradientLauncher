package com.example.launchertest.try_grid

import android.content.ClipData
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.iterator
import androidx.core.view.setMargins
import com.example.launchertest.AppInfo
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.activity_try_grid.*
import kotlin.random.Random


class TryGridActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener, View.OnDragListener, View.OnLongClickListener {
    lateinit var enterShape: Drawable
    lateinit var normalShape: Drawable

    override fun onDrag(iconView: View?, event: DragEvent?): Boolean {


        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                (event.localState as ImageView).setColorFilter(Color.LTGRAY)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                swap()
            }
            DragEvent.ACTION_DRAG_EXITED -> {}
            DragEvent.ACTION_DROP -> {
                val view2 = event.localState as View
                val owner = view2.parent as ViewGroup
                owner.removeView(view2)
                val container = try_grid
                container.addView(view2)
                view2.visibility = View.VISIBLE
            }
//            DragEvent.ACTION_DRAG_ENDED -> icon.setBackgroundDrawable(normalShape)
            else -> {
                // do nothing
            }
        }
        return true
    }

    override fun onLongClick(view: View?): Boolean {
//        createPopupMenu(view!!)
        startDrag(view!!)
        return true
    }

    private fun startDrag(view: View) {
        val data = ClipData.newPlainText("", "")
        val shadowBuilder = View.DragShadowBuilder(view)
        view.startDrag(data, shadowBuilder, view, 0)
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        println(item?.intent)
        return true
    }

    private fun swap() {

    }

    lateinit var allApps: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_grid)

        try_grid.setOnDragListener(this)

        try_grid.columnCount = 5
        try_grid.rowCount = 7

        allApps = getAllAppsList(this)

        fillGrid(try_grid)
    }

    fun createPopupMenu(view: View) {
        val builder = MenuBuilder(view.context)
        val inflater = MenuInflater(view.context)
        inflater.inflate(R.menu.shortcut_popup_menu, builder)
        for (item in builder.iterator()) {
            item.setOnMenuItemClickListener(this)
        }
        val menuHelper = MenuPopupHelper(view.context, builder, view)
        menuHelper.setForceShowIcon(true)
        menuHelper.show()
    }

    fun fillGrid(grid: GridLayout) {
        for (i in 0 until grid.rowCount) {
            for (j in 0 until grid.columnCount)
            {
                val dummyCell = LinearLayout(this)
                val lp = GridLayout.LayoutParams()
                lp.width = 144
                lp.height = 144
                lp.setGravity(Gravity.CENTER)
                lp.setMargins(20)
                dummyCell.layoutParams = lp
                dummyCell.setBackgroundColor(Color.argb(40,0,0,0))
                dummyCell.setOnDragListener(this)

                if (Random.nextInt(100) > 70) {
                    val img = ImageView(this)
                    img.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    img.setOnLongClickListener(this)
                    img.setImageDrawable(getAllAppsList(this)[Random.nextInt(getAllAppsList(this).size)].icon)
                    dummyCell.addView(img)
                }

                grid.addView(dummyCell)
            }
        }
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
