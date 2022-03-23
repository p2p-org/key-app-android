package org.p2p.wallet.renbtc.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogBtcTopupInfoBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class RenBtcTopupBottomSheet : NonDraggableBottomSheetDialogFragment() {

    companion object {
        fun show(
            fm: FragmentManager,
            requestKey: String,
            resultKey: String
        ) = RenBtcTopupBottomSheet().withArgs(
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        ).show(fm, RenBtcTopupBottomSheet::javaClass.name)
    }

    private val binding: DialogBtcTopupInfoBinding by viewBinding()
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_btc_topup_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            topupButton.setOnClickListener {
                setResult(isTopupSelected = true)
            }
            progressButton.setOnClickListener {
                setResult(isTopupSelected = false)
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    private fun setResult(isTopupSelected: Boolean) {
        setFragmentResult(requestKey, bundleOf(Pair(resultKey, isTopupSelected)))
        dismissAllowingStateLoss()
    }
}
