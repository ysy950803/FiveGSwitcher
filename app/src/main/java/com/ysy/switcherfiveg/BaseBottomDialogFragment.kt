package com.ysy.switcherfiveg

import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomDialogFragment : BottomSheetDialogFragment() {

    var onDismissListener: (() -> Unit)? = null

    open fun getDefaultBgResId(): Int = R.drawable.bg_more_bottom_sheet

    open fun enableOnBackInvokedCallback(): Boolean = false

    open fun onApplySystemBarsInsets(insets: Insets) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.run {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            if (enableOnBackInvokedCallback() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
            }
            delegate.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )?.let { root ->
                // API小于35时，此代码必须前置
                BottomSheetBehavior.from<View>(root).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    isHideable = true
                    peekHeight = getScreenHeight(false)
                    onApplySheetBehavior(this@run, this)
                }
                view.apply {
                    // API小于35时，此代码必须有
                    getDialogTopPadding()?.let { layoutParams.height = getScreenHeight() - it }
                }
                root.applySystemBarsInsets { v, insets ->
                    runCatching { onApplySystemBarsInsets(insets) }
                    v.postRunCatching {
                        view.apply {
                            layoutParams.height = getDialogTopPadding()?.let {
                                getScreenHeight(false) - it - if (it == 0) 0 else insets.top
                            } ?: height
                            setBackgroundResource(getDefaultBgResId())
                        }
                        root.apply {
                            setBackgroundColor(Color.TRANSPARENT)
                            updatePadding(
                                left = insets.left,
                                right = insets.right,
                                bottom = -insets.bottom
                            )
                        }
                    }
                }
            }
        }
    }

    protected open fun getDialogTopPadding(): Int? = null

    protected open fun onApplySheetBehavior(
        dialog: BottomSheetDialog,
        behavior: BottomSheetBehavior<View>
    ) {
    }

    override fun dismiss() {
        runCatching { super.dismiss() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        runCatching { onDismissListener?.invoke() }
    }
}
