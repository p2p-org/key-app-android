package org.p2p.wallet.pnl.ui

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.DialogPnlDetailsBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class PnlDetailsBottomSheet :
    BaseMvpBottomSheet<MvpView, NoOpPresenter<MvpView>>(R.layout.dialog_pnl_details), MvpView {

    companion object {
        private const val KEY_PERCENTAGE_VALUE = "percentage"
        fun show(fm: FragmentManager, percentage: String) {
            val tag = PnlDetailsBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            PnlDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(KEY_PERCENTAGE_VALUE, percentage)
                }
            }.show(fm, tag)
        }
    }

    private val binding: DialogPnlDetailsBinding by viewBinding()

    override val presenter: NoOpPresenter<MvpView> = NoOpPresenter()

    private var percentage: String = ""

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        percentage = arguments?.getString(KEY_PERCENTAGE_VALUE, "0%") ?: error("Percentage is null")

        with(binding) {
            cellPnlInfo.imageViewIcon.setImageResource(R.drawable.ic_lightning)
            cellPnlInfo.textViewTitle.text = getString(R.string.pnl_details_title, percentage)
            cellPnlInfo.textViewSubtitle.setText(R.string.pnl_details_subtitle)
            buttonDone.setOnClickListener {
                dismiss()
            }
        }
    }
}
