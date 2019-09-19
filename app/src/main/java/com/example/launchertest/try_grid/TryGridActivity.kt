package com.example.launchertest.try_grid

import android.content.ClipData
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
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

    private fun fillGrid(grid: GridLayout) {
        for (i in 0 until grid.rowCount) {
            for (j in 0 until grid.columnCount)
            {
                val dummyCell = DummyCell(this)
                val lp = GridLayout.LayoutParams()
                lp.width = 144
                lp.height = 144
                lp.setGravity(Gravity.CENTER)
                lp.setMargins(20)
                dummyCell.layoutParams = lp
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

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        println(item?.intent)
        return true
    }

    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        if (view !is DummyCell || event == null)
            return false

        val dummyCell = view
        val shortcut = event.localState as ImageView
//        println("view=${dummyCell?.javaClass?.simpleName} ${dummyCell.hashCode()}, event.action=${event.action} event.loacalState=${event.localState.javaClass.simpleName} ${event.localState.hashCode()}")

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                dummyCell.onDragStarted()
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                dummyCell.onDragLocationChanged(event.x, event.y)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                dummyCell.onDragEntered()
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                dummyCell.onDragExited()
            }
            DragEvent.ACTION_DROP -> {
                moveShortcut(shortcut, dummyCell as ViewGroup)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                // back to default state
                dummyCell.onDragEnded()
                endDrag(shortcut)
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

    private fun endDrag(shortcut: ImageView) {
        shortcut.clearColorFilter()
        shortcut.visibility = View.VISIBLE
    }

    private fun moveShortcut(shortcut: ImageView, newDummy: ViewGroup) {
        (shortcut.parent as ViewGroup).removeView(shortcut)
        newDummy.addView(shortcut)
    }

    private fun swapShortcuts() {

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
