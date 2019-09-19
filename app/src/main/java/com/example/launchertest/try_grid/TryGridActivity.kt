package com.example.launchertest.try_grid

import android.content.ClipData
import android.graphics.Color
import android.graphics.PorterDuff
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

    lateinit var allApps: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_grid)

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

    override fun onDrag(dummyCell: View?, event: DragEvent?): Boolean {
        val shortcut = event?.localState as ImageView
        shortcut.visibility = View.INVISIBLE
//        println("view=${dummyCell?.javaClass?.simpleName} ${dummyCell.hashCode()}, event.action=${event.action} event.loacalState=${event.localState.javaClass.simpleName} ${event.localState.hashCode()}")

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // change listeners
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                dummyCell?.setBackgroundResource(R.drawable.bot_gradient)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                dummyCell?.setBackgroundColor(Color.argb(40,0,0,0))
            }
            DragEvent.ACTION_DROP -> {
                println(dummyCell)
                moveShortuct(shortcut, dummyCell as ViewGroup)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                // back to default state
                endDrag(shortcut, dummyCell!!)
            }
        }
        return true
    }

    override fun onLongClick(view: View?): Boolean {
//        createPopupMenu(view!!)
        startDrag(view!! as ImageView)
        return true
    }

    private fun startDrag(shortcut: ImageView) {
        println("${shortcut.javaClass.simpleName} ${shortcut.hashCode()}")
        shortcut.visibility = View.INVISIBLE
        shortcut.setColorFilter(Color.rgb(181, 232, 255), PorterDuff.Mode.MULTIPLY)
        val data = ClipData.newPlainText("", "")
        val shadowBuilder = View.DragShadowBuilder(shortcut)
        shortcut.startDrag(data, shadowBuilder, shortcut, 0)
    }

    private fun endDrag(shortcut: ImageView, dummyCell: View) {
        shortcut.clearColorFilter()
        shortcut.visibility = View.VISIBLE
        dummyCell.setBackgroundColor(Color.argb(40,0,0,0))
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        println(item?.intent)
        return true
    }

    private fun moveShortuct(shortcut: ImageView, toDummy: ViewGroup) {
        //remove shortcut from parent
        toDummy.addView(shortcut)
    }
    private fun swap() {

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
