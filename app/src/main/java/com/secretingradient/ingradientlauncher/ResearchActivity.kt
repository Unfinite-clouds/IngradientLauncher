@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.element.FolderView
import kotlinx.android.synthetic.main._research_layout.*
import kotlinx.android.synthetic.main.stage_3_all_widgets.*


class ResearchActivity : AppCompatActivity() {
    lateinit var dk: DataKeeper
    lateinit var wMan: AppWidgetManager
    lateinit var pm: PackageManager
    lateinit var wHost: WidgetHost
    lateinit var context: Context
    lateinit var wallpaper: WallpaperFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stage_3_all_widgets)
        context = applicationContext
//        wallpaper = WallpaperFlow(context, stage_3_rv.windowToken)
        wMan = AppWidgetManager.getInstance(context)
        pm = packageManager


//        research_btn.setOnClickListener {
//            val img = ImageView(this).apply { setBackgroundResource(R.drawable.ic_info) }
//        }

        wHost = WidgetHost(applicationContext)
        wHost.startListening()
        val wid = wHost.allocateAppWidgetId()
        dk = DataKeeper(this, wHost)

/*        val intentGetAllWidgets = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        val list = pm.queryBroadcastReceivers(intentGetAllWidgets, 0)
        val providers = wMan.installedProviders
        val prov = providers[53].also { println(it.provider.className) }
        providers.forEachIndexed {i, it ->
            println("$i) ${it.provider.className}")
            println("${it.minWidth}, ${it.minHeight}, ${it.minResizeWidth}, ${it.minResizeHeight}")
            println(" ")
        }*/

//        stage_3_rv.adapter = AllWidgetsStage.AllWidgetsAdapter(dk)
        stage_3_rv.layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val wId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: return
        val wInfo: AppWidgetProviderInfo = wMan.getAppWidgetInfo(wId) ?: return

        if (requestCode == 0 && wInfo.configure != null)
            startConfigureActivity(wId, wInfo)
        else
            addWidget(wId, wInfo)
    }

    fun loadPreview(context: Context, density: Int, providerInfo: AppWidgetProviderInfo): Drawable? {
        val resources: Resources = context.packageManager.getResourcesForApplication(providerInfo.provider.packageName)
        var preview: Drawable? = null
        if (providerInfo.previewImage != 0)
            try {
                preview = ResourcesCompat.getDrawableForDensity(resources, providerInfo.previewImage, density, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        if (preview == null) {
            val id = dk.allAppsIds.find { it.startsWith(providerInfo.provider.packageName) }!!
            preview = dk.getAppIcon(id).constantState?.newDrawable()
//            preview = ResourcesCompat.getDrawableForDensity(resources, providerInfo.icon, density, null)
        }
        return preview
    }

    fun loadIcon(context: Context, density: Int, providerInfo: AppWidgetProviderInfo): Drawable? {
        val resources: Resources = context.packageManager.getResourcesForApplication(providerInfo.provider.packageName)
        return ResourcesCompat.getDrawableForDensity(resources, providerInfo.icon, density, null)
    }


    fun createWidget(wid: Int, prov: AppWidgetProviderInfo) {
        val isBindAllowed = wMan.bindAppWidgetIdIfAllowed(wid, prov.provider)
        println("bind is allowed: $isBindAllowed")

        if (!isBindAllowed) {
            println("asking for permission...")
            val intentBind = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
            intentBind.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wid)
            intentBind.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, prov.provider)
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options)
            startActivityForResult(intentBind, 0)
        }
        else if (prov.configure != null)
            startConfigureActivity(wid, prov)
        else
            addWidget(wid, prov)
    }

    fun startConfigureActivity(widgetId: Int, wInfo: AppWidgetProviderInfo) {
        println("start configure activity...")
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
        intent.component = wInfo.configure
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        startActivityForResult(intent, 1)
    }

    fun addWidget(widgetId: Int, wInfo: AppWidgetProviderInfo){
        println("adding widget...")
        val hostView = wHost.createView(context, widgetId, wInfo)
        val size = toDp(500).toInt()
        wInfo.apply {
            val w = minWidth  // why in px?
            val h = minHeight

            println("$w, $h")
//            hostView.updateAppWidgetSize(null, minWidth, minHeight, minWidth, minHeight)
            println("$minWidth, $minHeight, $minResizeWidth, $minResizeHeight")
            hostView.layoutParams = FrameLayout.LayoutParams(w, h)
        }
        research_root.addView(hostView)
        val frame = LayoutInflater.from(context).inflate(R.layout.resize_frame, research_root, false) as WidgetResizeFrameOld
//        frame.attachToWidget(hostView)
        research_root.addView(frame)
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

}

class ResearchAdapter(val dk: DataKeeper) : RecyclerView.Adapter<WidgetPreviewHolder>() {
    val context = dk.context
    val widgetInfos = AppWidgetManager.getInstance(context).installedProviders

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetPreviewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.preview_widget_item, parent, false) as ViewGroup
        v.apply {
            val lp = GridLayoutManager.LayoutParams(-1, -1)
            layoutParams = lp
            lp.setMargins(0, 0, 16, 16)
            lp.width = 300
//            layoutParams.height = 400
//            layoutParams.bottomMargin(0, 0, 0, 20)
        }
        val h = WidgetPreviewHolder(v)
        return h
    }

    override fun getItemCount() = dk.widgetPreviewDrawables.size

    override fun onBindViewHolder(holder: WidgetPreviewHolder, position: Int) {
        val widgetInfo = widgetInfos[position]
        val id = dk.getWidgetId(widgetInfo)
        var icon: Drawable? = null
        val icons = dk.iconDrawables.filterKeys { it.startsWith(widgetInfo.provider.packageName) }
        if (!icons.isEmpty())
            icon = icons.values.first()
        else
            println("ass")
        icon?.apply {
            setBounds(0, 0, 50, 50)
        }
        val preview = dk.widgetPreviewDrawables[id]

        holder.label.text = widgetInfo.label
        holder.label.setCompoundDrawables(icon, null, null, null)
        holder.label.compoundDrawablePadding = 20
        holder.preview.setImageDrawable(preview)
        if (widgetInfo.previewImage == 0) {
            holder.preview.setPadding(50)
        }
        holder.sizeLabel.text = "${widgetInfo.minWidth/80} x ${widgetInfo.minHeight/80}"
    }

}

class WidgetPreviewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
    val label = view.findViewById<TextView>(R.id.label)
    val preview = view.findViewById<ImageView>(R.id.preview_image)
    val sizeLabel = view.findViewById<TextView>(R.id.size)
}

class MyRoot : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }
}

class MyVH(view: View): RecyclerView.ViewHolder(view) {
    val folderView = view.findViewById<FolderView>(R.id.research_item)
}

class ItemDragger : ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}