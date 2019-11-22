package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.data.*

class FolderView : RecyclerView {
    var info: FolderInfo = FolderInfo(mutableListOf())
    val folderSize
        get() = info.apps.size
    val apps: MutableList<AppInfo>
        get() = info.apps

    init {
        setBackgroundResource(R.color.TransparentBlackDarker)
        adapter = FolderAdapter(context, apps)
        layoutManager = GridLayoutManager(context, 2)
    }

    constructor(context: Context, vararg apps: AppInfo) : super(context){
        addApps(apps.asList())
    }
    constructor(context: Context, apps: Collection<AppInfo>) : super(context){
        addApps(apps)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun addApps(vararg newApps: AppInfo) {
        addApps(newApps.asList())
    }

    fun addApps(newApps: Collection<AppInfo>) {
        info.apps.addAll(newApps)
        update()
    }

    fun removeApp(i: Int) {
        info.apps.removeAt(i)
        update()
    }

    fun clear() {
        info.apps.clear()
        update()
    }

    fun update() {
        adapter?.notifyDataSetChanged()
    }

    fun revert(dataset: Dataset<Data, Info>) {
        val pos = (this.layoutParams as SnapLayout.SnapLayoutParams).position
        val p = parent as? SnapLayout ?: return
        p.removeView(this)
        val app = apps[0].createView(context)
        p.addNewView(app, pos, 2, 2)
        dataset.put(pos, app.info!!, true)
    }

    class IconHolder(item: View) : ViewHolder(item)

    class FolderAdapter(val context: Context, val data: MutableList<AppInfo>) : RecyclerView.Adapter<IconHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconHolder {
            // todo: 102. height = snapStepY
            return IconHolder(ImageView(context).apply { layoutParams = GridLayoutManager.LayoutParams(-2, 102) })
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: IconHolder, position: Int) {
            (holder.itemView as ImageView).setImageDrawable(data[position].icon)
        }

    }
}