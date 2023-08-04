package org.p2p.wallet.striga.onramp.iban

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.disableScrolling
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogStrigaIbanImportantNotesBinding
import org.p2p.wallet.striga.onramp.iban.adapter.StrigaUserIbanItemDecoration
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val ARG_CHECK_BOX_IS_CHECKED = "ARG_CHECK_BOX_IS_CHECKED"

class StrigaIbanImportantNotesDialog :
    BaseMvpBottomSheet<StrigaIbanImportantNotesContract.View, StrigaIbanImportantNotesContract.Presenter>(
        R.layout.dialog_striga_iban_important_notes
    ),
    StrigaIbanImportantNotesContract.View {
    companion object {
        fun show(checkBoxIsChecked: Boolean, fm: FragmentManager) {
            val tag = StrigaIbanImportantNotesDialog::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return

            StrigaIbanImportantNotesDialog()
                .apply {
                    arguments = bundleOf(
                        ARG_CHECK_BOX_IS_CHECKED to checkBoxIsChecked
                    )
                }
                .show(fm, tag)
        }
    }

    private val binding: DialogStrigaIbanImportantNotesBinding by viewBinding()

    override val presenter: StrigaIbanImportantNotesContract.Presenter by inject()

    private val checkBoxIsChecked: Boolean by args(ARG_CHECK_BOX_IS_CHECKED)

    private val adapter = CommonAnyCellAdapter(
        mainCellDelegate()
    )

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            checkBoxDontShowAgain.isChecked = checkBoxIsChecked
            checkBoxDontShowAgain.setOnCheckedChangeListener { _, isChecked ->
                presenter.onDontShowAgainChecked(isChecked)
            }

            recyclerViewCells.disableScrolling()
            recyclerViewCells.attachAdapter(adapter)
            recyclerViewCells.addItemDecoration(
                StrigaUserIbanItemDecoration(
                    context = requireContext(),
                    leftMarginPx = 20.toPx()
                )
            )

            buttonDone.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun showNotes(details: List<AnyCellItem>) {
        adapter.items = details
    }
}
