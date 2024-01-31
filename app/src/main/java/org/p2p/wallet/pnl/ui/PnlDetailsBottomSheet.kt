package org.p2p.wallet.pnl.ui

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.DialogPnlDetailsBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding

class PnlDetailsBottomSheet :
    BaseMvpBottomSheet<MvpView, NoOpPresenter<MvpView>>(R.layout.dialog_pnl_details), MvpView {

    companion object {
        private const val KEY_PNL_PERCENTAGE_VALUE = "pnl_percentage"
        fun show(fm: FragmentManager, pnlPercentage: String) {
            val tag = PnlDetailsBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            PnlDetailsBottomSheet().apply {
                arguments = bundleOf(
                    KEY_PNL_PERCENTAGE_VALUE to pnlPercentage
                )
            }.show(fm, tag)
        }
    }

    private val binding: DialogPnlDetailsBinding by viewBinding()

    override val presenter: NoOpPresenter<MvpView> = NoOpPresenter()

    private var pnlPercentage: String by args(KEY_PNL_PERCENTAGE_VALUE)

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            cellPnlInfo.imageViewIcon.setImageResource(R.drawable.ic_lightning)
            cellPnlInfo.textViewTitle.text = getString(R.string.pnl_details_title, pnlPercentage)
            cellPnlInfo.textViewSubtitle.setText(R.string.pnl_details_subtitle)
            buttonDone.setOnClickListener {
                dismiss()
            }
        }
    }
}
