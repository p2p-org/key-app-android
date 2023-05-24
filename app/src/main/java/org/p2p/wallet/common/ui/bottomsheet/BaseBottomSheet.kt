package org.p2p.wallet.common.ui.bottomsheet

import androidx.annotation.LayoutRes
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.root.DecorSystemBarsDelegate
import org.p2p.wallet.root.SystemIconsStyle

abstract class BaseBottomSheet(@LayoutRes private val layoutResId: Int) : BottomSheetDialogFragment() {

    private var decorSystemBars: DecorSystemBarsDelegate? = null
    protected open val customStatusBarStyle: SystemIconsStyle? = SystemIconsStyle.WHITE
    protected open val customNavigationBarStyle: SystemIconsStyle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireDialog().window?.let { decorSystemBars = DecorSystemBarsDelegate(it) }
        decorSystemBars?.onCreate()
        updateSystemBarsStyle(customStatusBarStyle, customNavigationBarStyle)
        applyWindowInsets(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decorSystemBars = null
    }

    protected fun setExpanded(isExpanded: Boolean) {
        if (!isExpanded) {
            dialog?.setOnShowListener(null)
            return
        }

        dialog?.setOnShowListener { dialogInterface ->
            val dialogView = dialogInterface as BottomSheetDialog

            val bottomSheet =
                dialogView.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_SETTLING -> behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        else -> Unit
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    protected open fun updateSystemBarsStyle(
        statusBarStyle: SystemIconsStyle? = null,
        navigationBarStyle: SystemIconsStyle? = null,
    ) {
        decorSystemBars?.updateSystemBarsStyle(statusBarStyle, navigationBarStyle)
    }

    protected open fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { view, insets, initialPadding ->
            val systemAndIme = insets.systemAndIme()
            view.updatePadding(
                // top consumed here: com.google.android.material.bottomsheet.BottomSheetDialog.EdgeToEdgeCallback
                left = initialPadding.left + systemAndIme.left,
                right = initialPadding.right + systemAndIme.right,
                bottom = initialPadding.bottom + systemAndIme.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }
    }
}
