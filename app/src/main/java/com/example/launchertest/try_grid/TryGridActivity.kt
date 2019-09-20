package com.example.launchertest.try_grid

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.launchertest.AppInfo
import com.example.launchertest.R
import com.example.launchertest.getAllAppsList
import kotlinx.android.synthetic.main.activity_try_grid.*
import kotlin.random.Random


class TryGridActivity : AppCompatActivity() {
    lateinit var allApps: ArrayList<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_grid)

        allApps = getAllAppsList(this)

        fillGrid(try_grid)
    }

    private fun fillGrid(grid: LauncherScreenGrid) {
        for (i in 0 until (0.5*grid.childCount).toInt()) {
            val img = ImageView(this)
            img.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            img.setOnLongClickListener(try_grid)
            img.setImageDrawable(getAllAppsList(this)[Random.nextInt(getAllAppsList(this).size)].icon)
            grid.addViewTo(img, i%grid.columnCount, i/grid.columnCount)
        }
    }

/*    fun onDrag(view: View?, event: DragEvent?): Boolean {
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
    }*/





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
