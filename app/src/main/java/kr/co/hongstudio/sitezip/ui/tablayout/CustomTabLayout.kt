package kr.co.hongstudio.sitezip.ui.tablayout

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout


class CustomTabLayout : TabLayout {

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (tabCount == 0) return
        try {
            tabMode = if (tabCount <= 3) MODE_FIXED else MODE_SCROLLABLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}