package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.view.View

// Can only store Bitmap icons (Adaptive icons is not supported)
class AppInfo(packageName: String? = null, name: String? = null, label: String? = null, icon: Drawable? = null) :
    ElementInfo() {
    companion object {
        private const val serialVersionUID = 4401L

        fun createFromResolveInfo(context: Context, resolveInfo: ResolveInfo): AppInfo {
            val pm = context.packageManager
            val ai = resolveInfo.activityInfo
            return AppInfo(
                ai.packageName,
                ai.name,
                ai.loadLabel(pm).toString(),
                ai.loadIcon(pm)
            )
        }

        fun getIdFromResolveInfo(resolveInfo: ResolveInfo): String {
            return "${resolveInfo.activityInfo.packageName}_${resolveInfo.activityInfo.name}"
        }
    }

    override val snapWidth
        get() = 2
    override val snapHeight
        get() = 2

    fun createView(context: Context): View {
        return AppView(context, this)
    }

    lateinit var packageName: String
    lateinit var name: String
    lateinit var label: String
    @Transient var icon: Drawable? = null
    private var iconByteArray: ByteArray? = null

    init {
        if (packageName != null) this.packageName = packageName
        if (name != null) this.name = name
        if (label != null) this.label = label
        if (icon != null) this.icon = icon
    }

    val id: String
        get() = "${packageName}_$name" // .split('_')

    fun set(appInfo: AppInfo) {
        this.packageName = appInfo.packageName
        this.name = appInfo.name
        this.label = appInfo.label
        this.icon = appInfo.icon
    }

    fun prepareIconToDump(size: Int?) {
//        iconByteArray = encodeBitmap(if (size == null) icon!!.toBitmap() else icon!!.toBitmap(size, size))
    }

    fun loadIconFromDump(context: Context) {
//        icon = BitmapDrawable(context.resources, decodeBitmap(iconByteArray!!))
    }

}