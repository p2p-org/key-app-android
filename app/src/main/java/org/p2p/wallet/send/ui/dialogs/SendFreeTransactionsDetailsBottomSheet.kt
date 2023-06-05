package org.p2p.wallet.send.ui.dialogs

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.shapeRounded16dp
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogSendFreeTransactionsInfoBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val ARG_OPENED_FROM = "ARG_OPENED_FROM"

class SendFreeTransactionsDetailsBottomSheet : BaseDoneBottomSheet() {

    enum class OpenedFrom {
        SEND, SEND_VIA_LINK
    }

    companion object {
        fun show(fm: FragmentManager, openedFrom: OpenedFrom) {
            SendFreeTransactionsDetailsBottomSheet()
                .withArgs(ARG_OPENED_FROM to openedFrom)
                .show(fm, SendFreeTransactionsDetailsBottomSheet::javaClass.name)
        }
    }

    private val openedFrom: OpenedFrom by args(ARG_OPENED_FROM)
    private var _binding: DialogSendFreeTransactionsInfoBinding? = null

    private val binding: DialogSendFreeTransactionsInfoBinding get() = _binding!!

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = inflater.inflateViewBinding(attachToRoot = false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (openedFrom) {
            OpenedFrom.SEND -> renderSendDetails()
            OpenedFrom.SEND_VIA_LINK -> renderSendViaLinkDetails()
        }
    }

    private fun renderSendDetails() {
        with(binding.layoutFreeTransactions) {
            imageViewIcon.setImageResource(R.drawable.ic_lightning)
            textViewTitle.setText(R.string.free_transactions_title)
            textViewSubtitle.text = getString(R.string.free_transactions_subtitle)
        }
        baseDialogBinding.buttonDone.apply {
            text = getString(R.string.free_transactions_button)
            setBackgroundColor(getColor(R.color.bg_night))
            setTextColorRes(R.color.text_snow)
            setOnClickListener { dismissAllowingStateLoss() }
        }
    }

    private fun renderSendViaLinkDetails() {
        with(binding.layoutFreeTransactions) {
            DrawableCellModel(
                drawable = shapeDrawable(shapeRounded16dp()),
                tint = R.color.bg_cloud
            ).applyBackground(root)

            imageViewIcon.setImageResource(R.drawable.ic_lightning)
            textViewTitle.setText(R.string.send_via_link_free_transactions_title)
            textViewSubtitle.setText(R.string.send_via_link_free_transactions_subtitle)
        }
        with(baseDialogBinding.buttonDone) {
            setText(R.string.send_via_link_free_transactions_button)
            setBackgroundColor(getColor(R.color.bg_night))
            setTextColorRes(R.color.text_snow)
            setOnClickListener { dismissAllowingStateLoss() }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
