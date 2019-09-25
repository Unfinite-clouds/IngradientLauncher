package com.example.launchertest.launcher_skeleton

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.example.launchertest.decodeBitmap
import com.example.launchertest.encodeBitmap
import java.io.Serializable

// Can only store Bitmap icons (Adaptive icons is not supported now)
class AppInfo(var packageName: String, var name:String, var label: String? = null, @Transient var icon: Drawable? = null) :
    Serializable {
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

    val id: String
        get() = "${packageName}_$name" // .split('_')

    private var iconByteArray: ByteArray? = null

    fun prepareIconToDump(size: Int?) {
        iconByteArray = encodeBitmap(if (size == null) icon!!.toBitmap() else icon!!.toBitmap(size, size))
    }

    fun loadIconFromDump(context: Context) {
        icon = BitmapDrawable(context.resources, decodeBitmap(iconByteArray!!))
    }
}