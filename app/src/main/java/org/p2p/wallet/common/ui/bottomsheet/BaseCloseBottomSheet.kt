package org.p2p.wallet.common.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogBaseCloseBinding
import org.p2p.wallet.utils.args

/**
 * For bottomSheets with just close button without any result - so show info for example
 */
abstract class BaseCloseBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val ARG_TITLE = "ARG_TITLE"
    }

    private val title: String by args(ARG_TITLE)

    lateinit var baseDialogBinding: DialogBaseCloseBinding

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        baseDialogBinding = DialogBaseCloseBinding.inflate(inflater, container, false)
        val innerView = onCreateInnerView(inflater, container, savedInstanceState)
        baseDialogBinding.viewInner.addView(innerView)
        return baseDialogBinding.root
    }

    abstract fun onCreateInnerView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(baseDialogBinding) {
            textViewTitle.text = title
            imageViewClose.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}
