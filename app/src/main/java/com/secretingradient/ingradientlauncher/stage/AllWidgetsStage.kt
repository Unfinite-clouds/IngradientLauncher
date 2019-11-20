package com.secretingradient.ingradientlauncher.stage

import android.appwidget.AppWidgetManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.*
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.data.WidgetPreviewInfo
import kotlin.math.ceil

val ICON_PADDING = toPx(10)
val PREVIEW_PADDING_IF_ICON = toPx(25)
val PREVIEW_PADDING = toPx(12)
val ICON_SIZE = toPx(28)
val CARD_MARGIN = toPx(8)

class AllWidgetsStage(launcherRootLayout: LauncherRootLayout) : BaseStage(launcherRootLayout) {
    override val stageLayoutId = R.layout.stage_3_all_widgets
    lateinit var recyclerView: RecyclerView
    val scroller = WallpaperFlow.RecyclerScroller(launcherRootLayout)

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        recyclerView = stageRootLayout.findViewById(R.id.stage_3_rv)
        recyclerView.adapter = AllWidgetsAdapter(dataKeeper)
        recyclerView.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.HORIZONTAL, false)
        recyclerView.addOnScrollListener(scroller)
    }

    override fun onStageSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        scroller.maxScroll = recyclerView.computeHorizontalScrollRange() - recyclerView.width
    }

    class AllWidgetsAdapter(val dataKeeper: DataKeeper) : RecyclerView.Adapter<WidgetPreviewHolder>() {
        val context = dataKeeper.context
        val dataset = mutableListOf<WidgetPreviewInfo>()
        val installedWidgets = AppWidgetManager.getInstance(context).installedProviders
        // todo: reInit after any app has installed/uninstalled

        init {
            prepareDataset()
        }

        fun prepareDataset() {
            installedWidgets.forEachIndexed {i, it ->
                val widget = installedWidgets[i]
                val id = dataKeeper.getWidgetId(widget)

                // Icon
                var icon: Drawable? = null
                // 1) try to find icon from appIcons in the same packageName
                for (entry in dataKeeper.iconDrawables) {
                    if (entry.key.startsWith(widget.provider.packageName)) {
                        icon = entry.value
                        break
                    }
                }
                if (icon == null)
                    // 2) try to get icon from widget
                    icon = dataKeeper.loadWidgetIcon(getDensity(context), widget)
                icon?.setBounds(0, 0, ICON_SIZE, ICON_SIZE)

                val preview = dataKeeper.widgetPreviewDrawables[id]
                val size = "${ceil(widget.minWidth/80f).toInt()} x ${ceil(widget.minHeight/80f).toInt()}"  // todo 80f
                dataset.add(WidgetPreviewInfo(preview, icon, it.label, size))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetPreviewHolder {
            val widgetView = LayoutInflater.from(context).inflate(R.layout.preview_widget_item, parent, false) as ViewGroup
            widgetView.apply {
                val lp = layoutParams as RecyclerView.LayoutParams
                lp.setMargins(0, 0, CARD_MARGIN, CARD_MARGIN)
                lp.width = 300  // todo 300?
            }
            return WidgetPreviewHolder(widgetView)
        }

        override fun getItemCount() = dataset.size

        override fun onBindViewHolder(holder: WidgetPreviewHolder, position: Int) {
            holder.bindWidgetInfo(dataset[position])
        }
    }

    class WidgetPreviewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
        val titleTextView = view.findViewById<TextView>(R.id.label)
        val previewImageView = view.findViewById<ImageView>(R.id.preview_image)
        val sizeTextView = view.findViewById<TextView>(R.id.size)

        fun bindWidgetInfo(widgetPreviewInfo: WidgetPreviewInfo) {
            titleTextView.text = widgetPreviewInfo.label
            titleTextView.setCompoundDrawables(widgetPreviewInfo.icon, null, null, null)
            titleTextView.compoundDrawablePadding = ICON_PADDING
            previewImageView.setImageDrawable(widgetPreviewInfo.previewImage)
            if (widgetPreviewInfo.previewImage == null) {
                previewImageView.setImageDrawable(widgetPreviewInfo.icon?.constantState?.newDrawable())
                previewImageView.setPadding(PREVIEW_PADDING_IF_ICON)
            }
            else
                previewImageView.setPadding(PREVIEW_PADDING)
            sizeTextView.text = widgetPreviewInfo.size
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            println("onLongClick")
            return false
        }
    }

}