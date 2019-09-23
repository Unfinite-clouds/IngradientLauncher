package com.example.launchertest.try_grid

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlin.reflect.KProperty

@RequiresApi(Build.VERSION_CODES.O)
class AdaptiveIconView(context: Context, attrs: AttributeSet?) : View(context, attrs){
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val layerSize: Int
    private val iconSize: Int
    private val layerCenter: Float
    private val viewportOffset: Int
    private val background: Bitmap
    private val foreground: Bitmap
    private var left = 0f
    private var top = 0f
    private var foregroundDx by InvalidateDelegate(0f)
    private var foregroundDy by InvalidateDelegate(0f)
    private var backgroundDx by InvalidateDelegate(0f)
    private var backgroundDy by InvalidateDelegate(0f)
    private var backgroundScale by InvalidateDelegate(1f)
    private var foregroundScale by InvalidateDelegate(2f)

    var cornerRadius by InvalidateDelegate(16f)
    // scale & translate factors [0,1]

    var foregroundScaleFactor by FloatRangeDelegate(1f)
    var backgroundScaleFactor by FloatRangeDelegate(1f)

    @Keep // called by @animator/scale
    var scale = 0f
        set(@FloatRange(from = 0.0, to = 1.0) value) {
            field = Math.max(0f, Math.min(1f, value))
            backgroundScale = 1f + backgroundScaleFactor * value
            foregroundScale = 1f + foregroundScaleFactor * value
            scaleX = backgroundScale
            scaleY = backgroundScale
        }

    init {
        layerSize = Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40f, context.resources.displayMetrics))
        layerCenter = (layerSize / 2).toFloat()
        iconSize = (layerSize / (1 + 2 * AdaptiveIconDrawable.getExtraInsetFraction())).toInt()
        viewportOffset = (layerSize - iconSize) / 2

        background = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ARGB_8888)
        backgroundPaint.shader = BitmapShader(background, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        foreground = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ARGB_8888)
        foregroundPaint.shader = BitmapShader(foreground, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    fun setIcon(icon: AdaptiveIconDrawable) {
        background.eraseColor(Color.TRANSPARENT)
        foreground.eraseColor(Color.TRANSPARENT)
        val c = Canvas()
        icon.background?.let { rasterize(it, background, c) }
        icon.foreground?.let { rasterize(it, foreground, c) }
    }

    // we have to draw icon in center
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        left = (w - iconSize) / 2f
        top = (h - iconSize) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // apply any velocity translations or touch scaling to the shaders
        transformLayer(backgroundPaint, backgroundDx, backgroundDy, backgroundScale)
        transformLayer(foregroundPaint, foregroundDx, foregroundDy, foregroundScale)

        canvas.run {
            val saveCount = save()
            translate(left, top)

            drawRoundRect(0f, 0f, iconSize.toFloat(), iconSize.toFloat(),
                cornerRadius, cornerRadius, backgroundPaint)
            drawRoundRect(0f, 0f, iconSize.toFloat(), iconSize.toFloat(),
                cornerRadius, cornerRadius, foregroundPaint)
            restoreToCount(saveCount)
        }

        val roundedBgDrawable = RoundedBitmapDrawableFactory.create(resources, background)
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas) {
        drawable.setBounds(0, 0, layerSize, layerSize)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }

    private fun transformLayer(layer: Paint, dx: Float, dy: Float, layerScale: Float) {
        with(layer.shader as BitmapShader) {
            getLocalMatrix(tempMatrix)
            tempMatrix.setScale(layerScale, layerScale, layerCenter, layerCenter)
            tempMatrix.postTranslate(dx - viewportOffset, dy - viewportOffset)
            setLocalMatrix(tempMatrix)
        }
    }

    companion object {
        private val tempMatrix = Matrix()
    }
}

/**
 * A [View] Delegate which [invalidates][View.postInvalidateOnAnimation] it when set.
 */
private class InvalidateDelegate<T : Any>(var value: T) {
    operator fun getValue(thisRef: View, property: KProperty<*>) = value
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    operator fun setValue(thisRef: View, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.postInvalidateOnAnimation()
    }
}

/**
 * A [Float] Delegate which constrains its value between a minimum and maximum.
 */
private class FloatRangeDelegate(
    var value: Float,
    val min: Float = 0f,
    val max: Float = 1f) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        this.value = value.coerceIn(min, max)
    }
}