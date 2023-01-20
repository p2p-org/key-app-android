package org.p2p.wallet.sell.ui.warning

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogSellWarningOnlySolBinding
import org.p2p.wallet.infrastructure.security.SecureStorageContract

class SellOnlySolWarningBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(fm: FragmentManager) {
            SellOnlySolWarningBottomSheet()
                .show(fm, SellOnlySolWarningBottomSheet::class.simpleName)
        }
    }

    private lateinit var binding: DialogSellWarningOnlySolBinding

    private val secureStorage: SecureStorageContract by inject()

    override fun onCreateInnerView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflater.inflateViewBinding(container, attachToRoot = false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseDialogBinding.imageViewClose.setOnClickListener {
            onCloseButtonClicked()
        }
        with(baseDialogBinding.buttonDone) {
            setText(R.string.sell_warning_only_sol_button)
            setBackgroundColor(getColor(R.color.bg_night))
            setTextColorRes(R.color.text_snow)
            setOnClickListener { onCloseButtonClicked() }
        }
    }

    private fun onCloseButtonClicked() {
        secureStorage.putBoolean(
            key = SecureStorageContract.Key.KEY_IS_SELL_WARNING_SHOWED,
            value = true
        )
        dismissAllowingStateLoss()
    }
}
