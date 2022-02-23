package org.p2p.wallet.renbtc.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogBtcTopupInfoBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class RenBtcTopupBottomSheet(
    private val onTopupClickListener: () -> Unit,
    private val onUseSolanaClickListener: () -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        fun show(
            fm: FragmentManager,
            onTopupClickListener: () -> Unit,
            onUseSolanaClickListener: () -> Unit
        ) = RenBtcTopupBottomSheet(onTopupClickListener, onUseSolanaClickListener).show(
            fm,
            RenBtcTopupBottomSheet::javaClass.name
        )
    }

    private val binding: DialogBtcTopupInfoBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_btc_topup_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        with(binding) {
            topupButton.setOnClickListener {
                onTopupClickListener.invoke()
                dismissAllowingStateLoss()
            }
            progressButton.setOnClickListener {
                onUseSolanaClickListener.invoke()
                dismissAllowingStateLoss()
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}