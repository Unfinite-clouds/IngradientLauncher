package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.GridLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.iterator
import androidx.core.view.setMargins
import com.example.launchertest.LauncherException
import com.example.launchertest.R

class LauncherScreenGrid : GridLayout, MenuItem.OnMenuItemClickListener, View.OnDragListener, View.OnLongClickListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private var cellWidth = 144
    private var cellHeight = 144
    private var margins = 20

    init {
        fillGrid()
    }

    private fun fillGrid() {
        for (i in 0 until rowCount) {
            for (j in 0 until columnCount) {
                addView(DummyCell(context).apply {
                    position = Point(i,j)
                    layoutParams.width = cellWidth
                    layoutParams.height = cellHeight
                    (layoutParams as LayoutParams).setMargins(margins)
                    (layoutParams as LayoutParams).setGravity(Gravity.CENTER)
                    setOnDragListener(this@LauncherScreenGrid)
                })
            }
        }
    }

    val positipns = Array(rowCount) { IntArray(columnCount) }

    override fun getChildAt(index: Int): DummyCell {
        return super.getChildAt(index) as DummyCell
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (child !is DummyCell) {
            throw LauncherException("LauncherScreenGrid can add only DummyCell view")
        }

        //link position to
        positipns[child.position.x][child.position.y] = indexOfChild(child)
    }

    fun getCellAt(x: Int, y: Int): DummyCell {
        return getChildAt(positipns[x][y])
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        return true
    }

    override fun onLongClick(v: View?): Boolean {
        return true
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

}