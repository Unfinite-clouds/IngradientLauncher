@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.secretingradient.ingradientlauncher.element.ElementInfo
import com.secretingradient.ingradientlauncher.element.FolderView
import kotlinx.android.synthetic.main._research_layout.*


class ResearchActivity : AppCompatActivity() {

    var value = 0
    val apps = mutableListOf<ElementInfo>()
    val stages = mutableListOf<Stage>()
    lateinit var dk: DataKeeper2

    lateinit var popupWindow: PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout._research_layout)

        dk = DataKeeper2(this)

        fillApps()

        research_rv.adapter = object : RecyclerView.Adapter<MyVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVH {
                return MyVH(LayoutInflater.from(this@ResearchActivity).inflate(R.layout._research_item, parent, false))
            }

            override fun getItemCount() = 6

            override fun onBindViewHolder(holder: MyVH, position: Int) {
                holder.folderView.clear()
//                holder.folderView.addApps((DataKeeper2.userStageData[position] as FolderData).ids)
            }

        }

        research_rv.layoutManager = GridLayoutManager(this, 3)

        ItemTouchHelper(Toucher()).attachToRecyclerView(research_rv)

    }

    class Toucher : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
        }

    }

    class MyVH(view: View): RecyclerView.ViewHolder(view) {
        val folderView = view.findViewById<FolderView>(R.id.research_item)
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun fillApps() {
//        val allApps = DataKeeper2.allAppsIds
        for (i in 0..6) {
//            apps.add(i, ElementInfo(allApps[i], SnapLayout.SnapLayoutInfo(i*2 + (i*2/8)*8, 2, 2)))
        }
    }

}


class MyRoot : LinearLayout {

    lateinit var vp: ViewPager2
    lateinit var stages: List<Stage>

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

}


class Stage : FrameLayout {
    var stageNumber: Int = -1
//    val overlayPoint = Point()

    constructor(context: Context, n: Int) : super(context) { this.stageNumber = n}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)


}
