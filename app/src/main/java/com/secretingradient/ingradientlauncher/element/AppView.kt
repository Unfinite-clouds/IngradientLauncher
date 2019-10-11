package com.secretingradient.ingradientlauncher.element

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import com.secretingradient.ingradientlauncher.R
import com.secretingradient.ingradientlauncher.toPx
import kotlin.math.min


class AppView : TextView, MenuItem.OnMenuItemClickListener, View.OnClickListener {
    var appInfo: AppInfo
        set(value) {
            field = value
            text = field.label
            icon = field.icon
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

    var desiredIconSize = Int.MAX_VALUE
    var goingToRemove = false

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        includeFontPadding = false
//        maxLines = 1
        setLines(1)
        setTextColor(Color.WHITE)
        setOnClickListener(this)
        ellipsize = TextUtils.TruncateAt.END
    }

    constructor(context: Context, appInfo: AppInfo) : super(context) {
        this.appInfo = appInfo
        this.icon = appInfo.icon
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val iconId = attributeSet.getAttributeValue(null, "drawableTop")
        this.appInfo = AppInfo("package", "name",
            attributeSet.getAttributeValue(null, "text") ?: "label",
            ContextCompat.getDrawable(context, iconId?.toInt() ?: R.mipmap.ic_launcher_round))
    }

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

    override fun onClick(v: View?) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(appInfo.packageName))
    }

    fun createDragShadow(): DragShadowBuilder {
        return object : DragShadowBuilder(this) {
            val paint = Paint().apply { colorFilter = PorterDuffColorFilter(Color.rgb(181, 232, 255), PorterDuff.Mode.MULTIPLY) }
            override fun onDrawShadow(canvas: Canvas?) {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val c = Canvas(bitmap)
                view.draw(c)
                canvas?.drawBitmap(bitmap, 0f,0f, paint)
            }
        }
    }

    fun showMenu() {
        val builder = MenuBuilder(this.context)
        val inflater = MenuInflater(this.context)
        inflater.inflate(R.menu.shortcut_popup_menu, builder)
        for (item in builder.iterator()) {
            item.setOnMenuItemClickListener(this)
        }
        menuHelper = MenuPopupHelper(this.context, builder, this)
        menuHelper?.setForceShowIcon(true)
        menuHelper?.show()
    }

    fun dismissMenu() {
        menuHelper?.dismiss()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.popup_menu_info -> context.startActivity(intentToInfo())
            R.id.popup_menu_uninstall -> context.startActivity(intentToUninstall())
        }
        return true
    }

    private fun intentToInfo(): Intent {
        val uri = Uri.fromParts("package", appInfo.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun intentToUninstall(): Intent {
        val uri = Uri.fromParts("package", appInfo.packageName, null)
        val intent = Intent(Intent.ACTION_DELETE, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas, size: Int) {
        drawable.setBounds(0, 0, size, size)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }

    override fun toString(): String {
        return "${this.hashCode().toString(16)} - ${appInfo.label}, icon_bounds: ${icon?.bounds}, parent: $parent"
    }
}