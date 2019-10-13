package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.secretingradient.ingradientlauncher.decodeBitmap
import com.secretingradient.ingradientlauncher.encodeBitmap
import java.io.Serializable

// Can only store Bitmap icons (Adaptive icons is not supported)
class AppInfo(packageName: String? = null, name: String? = null, label: String? = null, icon: Drawable? = null) :
    Serializable {
    lateinit var packageName: String
    lateinit var name: String
    lateinit var label: String
    @Transient var icon: Drawable? = null

    val id: String
        get() = "${packageName}_$name" // .split('_')

    init {
        if (packageName != null) this.packageName = packageName
        if (name != null) this.name = name
        if (label != null) this.label = label
        if (icon != null) this.icon = icon
    }

    private var iconByteArray: ByteArray? = null

    fun set(appInfo: AppInfo) {
        this.packageName = appInfo.packageName
        this.name = appInfo.name
        this.label = appInfo.label
        this.icon = appInfo.icon
    }

    fun prepareIconToDump(size: Int?) {
        iconByteArray = encodeBitmap(if (size == null) icon!!.toBitmap() else icon!!.toBitmap(size, size))
    }

    fun loadIconFromDump(context: Context) {
        icon = BitmapDrawable(context.resources, decodeBitmap(iconByteArray!!))
    }

    companion object {
        private const val serialVersionUID = 4400L

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
}