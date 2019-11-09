package com.secretingradient.ingradientlauncher.element

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.view.menu.MenuPopupHelper
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.SnapLayout
import com.secretingradient.ingradientlauncher.stage.MainStage
import com.secretingradient.ingradientlauncher.toPx
import kotlin.math.min


class AppView : TextView {
    var state: MainStage.AppState? = null
        set(value) {
            field = value
            value?.let {
                text = value.info.label
                icon = value.info.icon
            }
        }

    var icon: Drawable?
        get() = compoundDrawables[1] // returns clone
        set(value) {
            val drawable = value?.constantState?.newDrawable(context.resources)
            drawable?.bounds = iconBounds
            setCompoundDrawables(null, drawable, null, null)
        }

    private var iconBounds = Rect()
    private var iconPaddingBottom = toPx(5).toInt()
    private var menuHelper: MenuPopupHelper? = null
    val snapWidth = 2
    val snapHeight = 2

    var desiredIconSize = Int.MAX_VALUE
    var animator: ObjectAnimator
    var animatorScale: Animator

    val colorMatrix = intArrayOf(
        1, 0, 0, 1, 0,  // = R
        0, 1, 0, 1, 0,  // = G
        0, 0, 1, 1, 0,  // = B
        0, 0, 0, 1, 0   // = A
    ).map { it.toFloat() }.toFloatArray()

    var testa = 0f
    set(value) {
        field = value
        colorMatrix[3] =  testa
        colorMatrix[8] = testa
        colorMatrix[13] = testa
        colorMatrix[0] = 1f-testa
        colorMatrix[6] = 1f-testa
        colorMatrix[12] = 1f-testa
        icon?.colorFilter = ColorMatrixColorFilter(colorMatrix)
    }

    init {
        layoutParams = SnapLayout.SnapLayoutParams(-1, snapWidth, snapHeight)
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        includeFontPadding = false
//        maxLines = 1
        setLines(1)
        setTextColor(Color.WHITE)
        ellipsize = TextUtils.TruncateAt.END
        animator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
            duration = 2000
        }
        isClickable = true
        animatorScale = AnimatorInflater.loadAnimator(context, R.animator.icon_touch)
        animatorScale.setTarget(this)
    }

    constructor(context: Context, appState: MainStage.AppState? = null) : super(context) {
        if (appState != null) {
            this.state = appState
        }
    }
/*    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val iconId = attributeSet.getAttributeValue(null, "drawableTop")
        this.state = MainStage.AppState("package", "name",
            attributeSet.getAttributeValue(null, "text") ?: "label",
            ContextCompat.getDrawable(context, iconId?.toInt() ?: R.mipmap.ic_launcher_round))
    }*/

    private fun computeIconBounds(width: Int, height: Int) {
        val w = width - paddingLeft - paddingRight
        val h = height - paddingTop - paddingBottom - (textSize).toInt() - iconPaddingBottom
        val maxSize = min(w, h)
        val iconSize = if (desiredIconSize != 0) min(desiredIconSize, maxSize) else maxSize
        val x = 0
        val y = h - iconSize
        iconBounds = Rect(x, y, x+iconSize, y+iconSize)
    }

    private fun updateIconBounds() {
        icon?.bounds = iconBounds
        setCompoundDrawables(null, icon, null, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        computeIconBounds(w, h)
        updateIconBounds()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    companion object : OnClickListener {
        override fun onClick(v: View) {
            if (v is AppView)
                v.launchApp()
        }
    }

    fun launchApp() {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(state!!.info.packageName))
    }

    fun intentToInfo() {
        val uri = Uri.fromParts("package", state!!.info.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
//        return intent
    }

    fun intentToUninstall() {
        val uri = Uri.fromParts("package", state!!.info.packageName, null)
        val intent = Intent(Intent.ACTION_DELETE, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
//        return intent
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas, size: Int) {
        drawable.setBounds(0, 0, size, size)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }

    override fun toString(): String {
        return "${this.hashCode().toString(16)} - ${state!!.info.label}, icon_bounds: ${icon?.bounds}, parent: $parent"
    }

    class AppInfo2

/*    class AppInfo(packageName: String? = null, name: String? = null, label: String? = null, icon: Drawable? = null) : ElementInfo {
        lateinit var packageName: String
        lateinit var name: String
        lateinit var label: String
        var icon: Drawable? = null

        val id: String
            get() = "${packageName}_$name" // .split('_')

        init {
            if (packageName != null) this.packageName = packageName
            if (name != null) this.name = name
            if (label != null) this.label = label
            if (icon != null) this.icon = icon
        }

        fun set(appInfo: com.secretingradient.ingradientlauncher.element.AppInfo) {
            this.packageName = appInfo.packageName
            this.name = appInfo.name
            this.label = appInfo.label
            this.icon = appInfo.icon
        }

        fun createView(context: Context): AppView {
            return AppView(context, this)
        }
    }*/
}