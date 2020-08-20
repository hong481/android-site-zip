package kr.co.honga.sitezip.util

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.inputmethod.InputMethodManager

class KeyboardUtil(

    applicationContext: Context,
    private val window: Window,
    private val onShowKeyboard: ((keyboardHeight: Int) -> Unit)? = null,
    private val onHideKeyboard: (() -> Unit)? = null

) {

    companion object {
        const val MIN_KEYBOARD_HEIGHT_PX = 150
    }

    private val inputMethodManager: InputMethodManager by lazy {
        applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private val windowVisibleDisplayFrame = Rect()

    private var lastVisibleDecorViewHeight: Int = 0

    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        window.decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame)
        val visibleDecorViewHeight = windowVisibleDisplayFrame.height()

        if (lastVisibleDecorViewHeight != 0) {
            if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                val currentKeyboardHeight =
                    window.decorView.height - windowVisibleDisplayFrame.bottom
                onShowKeyboard?.invoke(currentKeyboardHeight)
            } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                onHideKeyboard?.invoke()
            }
        }
        lastVisibleDecorViewHeight = visibleDecorViewHeight
    }

    /**
     * 키보드 활성화/비활성화
     */
    fun visibleKeyboard(isVisible: Boolean, targetView: View) {
        if (isVisible) {
            inputMethodManager.showSoftInput(targetView, 0)
        } else {
            inputMethodManager.hideSoftInputFromWindow(targetView.windowToken, 0)
        }
    }

    init {
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    fun detachKeyboardListeners() {
        window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }
}