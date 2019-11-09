package com.secretingradient.ingradientlauncher.element

// Can only store Bitmap icons (Adaptive icons is not supported)
/*
class AppInfo(packageName: String? = null, name: String? = null, label: String? = null, icon: Drawable? = null) : ElementInfo <Int, String> {
    override fun getIndex(): Int = 0

    override fun getData(): String = id

    lateinit var packageName: String
    lateinit var name: String
    lateinit var label: String
    @Transient var icon: Drawable? = null
    private var iconByteArray: ByteArray? = null

    val id: String
        get() = "${packageName}_$name" // .split('_')

    init {
        if (packageName != null) this.packageName = packageName
        if (name != null) this.name = name
        if (label != null) this.label = label
        if (icon != null) this.icon = icon
    }

    fun set(appInfo: AppInfo) {
        this.packageName = appInfo.packageName
        this.name = appInfo.name
        this.label = appInfo.label
        this.icon = appInfo.icon
    }

    fun createView(context: Context): AppView {
        return AppView(context, this)
    }

    @Deprecated("")
    fun prepareIconToDump(size: Int?) {
        iconByteArray = encodeBitmap(if (size == null) icon!!.toBitmap() else icon!!.toBitmap(size, size))
    }

    @Deprecated("")
    fun loadIconFromDump(context: Context) {
        icon = BitmapDrawable(context.resources, decodeBitmap(iconByteArray!!))
    }

}*/
