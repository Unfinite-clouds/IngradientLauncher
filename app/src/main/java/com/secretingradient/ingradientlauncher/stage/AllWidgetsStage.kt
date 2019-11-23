package com.secretingradient.ingradientlauncher.stage

import android.appwidget.AppWidgetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.LauncherRootLayout
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.data.WidgetPreviewInfo
import com.secretingradient.ingradientlauncher.toPx

val ICON_PADDING = toPx(10)
val PREVIEW_PADDING_IF_ICON = toPx(25)
val PREVIEW_PADDING = toPx(12)
val ICON_SIZE = toPx(28)
val CARD_MARGIN = toPx(8)

class AllWidgetsStage(launcherRootLayout: LauncherRootLayout) : BaseStage(launcherRootLayout) {
    override val stageLayoutId = R.layout.stage_3_all_widgets
    lateinit var recyclerView: RecyclerView

    override fun initInflate(stageRootLayout: StageRootLayout) {
        super.initInflate(stageRootLayout)
        recyclerView = stageRootLayout.findViewById(R.id.stage_3_rv)
        recyclerView.adapter = AllWidgetsAdapter(dataKeeper)
        recyclerView.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.HORIZONTAL, false)
        recyclerView.addOnScrollListener(scroller)
        stageRootLayout.doOnLayout {
            scroller.maxScroll = recyclerView.computeHorizontalScrollRange() - recyclerView.width
        }
    }

    inner class AllWidgetsAdapter(val dataKeeper: DataKeeper) : RecyclerView.Adapter<WidgetPreviewHolder>() {
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
                dataset.add(WidgetPreviewInfo(widget, dataKeeper))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetPreviewHolder {
            val widgetView = LayoutInflater.from(context).inflate(R.layout.preview_widget_item, parent, false) as ViewGroup
            widgetView.apply {
                val lp = layoutParams as RecyclerView.LayoutParams
                lp.setMargins(0, 0, CARD_MARGIN, CARD_MARGIN)
                lp.width = 300  // todo 300?
            }
            return WidgetPreviewHolder(widgetView, this@AllWidgetsStage)
        }

        override fun getItemCount() = dataset.size

        override fun onBindViewHolder(holder: WidgetPreviewHolder, position: Int) {
            holder.bindWidgetInfo(dataset[position])
        }
    }

    class WidgetPreviewHolder(view: ViewGroup, val stage: AllWidgetsStage) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
        val titleTextView = view.findViewById<TextView>(R.id.label)
        val previewImageView = view.findViewById<ImageView>(R.id.preview_image)
        val sizeTextView = view.findViewById<TextView>(R.id.size)
        lateinit var previewInfo: WidgetPreviewInfo

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
            this.previewInfo = widgetPreviewInfo
        }

        override fun onLongClick(v: View?): Boolean {
            stage.transferWidget(previewInfo)
            return true
        }

    }

    fun transferWidget(previewInfo: WidgetPreviewInfo) {
        launcherRootLayout.transferEvent(1, previewInfo)
    }


}