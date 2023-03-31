package org.p2p.wallet.newsend.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogSendFreeTransactionsInfoBinding

class FreeTransactionsDetailsBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
        ) {
            FreeTransactionsDetailsBottomSheet().show(
                fm, FreeTransactionsDetailsBottomSheet::javaClass.name
            )
        }
    }

    private lateinit var binding: DialogSendFreeTransactionsInfoBinding

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSendFreeTransactionsInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.layoutFreeTransactions) {
            imageViewIcon.setImageResource(R.drawable.ic_lightning)
            textViewTitle.setText(R.string.free_transactions_title)
            textViewSubtitle.text = getString(R.string.free_transactions_subtitle)
        }
        baseDialogBinding.buttonDone.apply {
            text = getString(R.string.free_transactions_button)
            setBackgroundColor(getColor(R.color.bg_night))
            setTextColor(getColor(R.color.text_snow))
            setOnClickListener { dismissAllowingStateLoss() }
        }
    }
}
