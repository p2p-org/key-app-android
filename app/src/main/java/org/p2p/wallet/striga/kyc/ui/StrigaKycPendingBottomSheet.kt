package org.p2p.wallet.striga.kyc.ui

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.DialogStrigaKycPendingBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaKycPendingBottomSheet :
    BaseMvpBottomSheet<MvpView, NoOpPresenter<MvpView>>(R.layout.dialog_striga_kyc_pending) {

    companion object {
        fun show(fm: FragmentManager) {
            val tag = StrigaKycPendingBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            StrigaKycPendingBottomSheet().show(fm, tag)
        }
    }

    override val presenter: NoOpPresenter<MvpView> = NoOpPresenter()

    private val binding: DialogStrigaKycPendingBinding by viewBinding()

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDone.setOnClickListener {
            dismiss()
        }
    }
}
