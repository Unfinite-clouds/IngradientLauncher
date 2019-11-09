@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.launchertest

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.launchertest.data.DataHelper
import com.secretingradient.launchertest.data.DataKeeper
import kotlinx.android.synthetic.main.research_layout.*


class ResearchActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var dk: DataKeeper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.research_layout)

        dk = DataKeeper(this)
        val dh = dk.dataHelper

//        dk.allAppsIds.forEachIndexed {i, s ->
//            if (i < 50)
//                dh.insert(i, dk.createAppInfo(s))
//        }
//        dh.dumpFileData()

        research_rv.adapter = ResearchAdapter(dh)
        research_rv.layoutManager = GridLayoutManager(this, 3)
        ItemTouchHelper(ItemDragger(dh)).attachToRecyclerView(research_rv)
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onClick(v: View?) {
        if (v == research_btn) {

        }
    }

}


class MyRoot : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
}

class ResearchAdapter(val dataHelper: DataHelper) : RecyclerView.Adapter<AppHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        return AppHolder(AppView(parent.context))
    }
    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        holder.appView.info = dataHelper[position]
    }
    override fun getItemCount() = dataHelper.size
}

class ItemDragger(val dataHelper: DataHelper) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition
        dataHelper.move(from, to, true)
        recyclerView.adapter?.notifyDataSetChanged()
        return true
    }
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}