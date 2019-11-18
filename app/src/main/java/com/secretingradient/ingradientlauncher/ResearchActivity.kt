@file:Suppress("RemoveSingleExpressionStringTemplate")

package com.secretingradient.ingradientlauncher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.secretingradient.ingradientlauncher.data.AppData
import com.secretingradient.ingradientlauncher.data.AppInfo
import com.secretingradient.ingradientlauncher.data.DataKeeper
import com.secretingradient.ingradientlauncher.data.Dataset
import com.secretingradient.ingradientlauncher.element.AppView
import com.secretingradient.ingradientlauncher.element.FolderView
import com.secretingradient.ingradientlauncher.stage.AppHolder
import kotlinx.android.synthetic.main._research_layout.*


class ResearchActivity : AppCompatActivity() {
    lateinit var dk: DataKeeper
    lateinit var wMan: AppWidgetManager
    lateinit var pm: PackageManager
    lateinit var wHost: AppWidgetHost
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout._research_layout)
        context = applicationContext
        wMan = AppWidgetManager.getInstance(context)
        pm = packageManager

//        dk = DataKeeper(this)

//        research_btn.setOnClickListener {
//            val img = ImageView(this).apply { setBackgroundResource(R.drawable.ic_info) }
//        }

        wHost = AppWidgetHost(applicationContext, WIDGET_HOST_ID)
        wHost.startListening()
        val wid = wHost.allocateAppWidgetId()

        val intentGetAllWidgets = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        val list = pm.queryBroadcastReceivers(intentGetAllWidgets, 0)
        val providers = wMan.installedProviders
        val prov = providers[53].also { println(it.provider.className) }
        providers.forEachIndexed {i, it ->
            println("$i) ${it.provider.className}")
            println("${it.minWidth}, ${it.minHeight}, ${it.minResizeWidth}, ${it.minResizeHeight}")
            println(" ")
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val wId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: return
        val wInfo: AppWidgetProviderInfo = wMan.getAppWidgetInfo(wId) ?: return

        if (requestCode == 0 && wInfo.configure != null)
            startConfigureActivity(wId, wInfo)
        else
            addWidget(wId, wInfo)
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
        val frame = LayoutInflater.from(context).inflate(R.layout._frame_test, research_root, false) as WidgetResizeFrame
        frame.hostView = hostView
        research_root.addView(frame)
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

}


class MyRoot : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }
}

class ResearchAdapter(val dataset: Dataset<AppData, AppInfo>) : RecyclerView.Adapter<AppHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        return AppHolder(AppView(parent.context))
    }

    override fun getItemCount() = 6

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        holder.app.info = dataset[position]
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