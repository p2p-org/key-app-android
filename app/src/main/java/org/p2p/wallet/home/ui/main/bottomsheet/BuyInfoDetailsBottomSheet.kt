package org.p2p.wallet.home.ui.main.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getColorStateList
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogBuyInfoDetailsPartBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

const val ARG_TOKEN_KEY = "ARG_TOKEN_KEY"

class BuyInfoDetailsBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            token: Token,
            requestKey: String = ARG_REQUEST_KEY,
            resultKey: String = ARG_RESULT_KEY
        ) = BuyInfoDetailsBottomSheet().withArgs(
            ARG_TOKEN_KEY to token,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, BuyInfoDetailsBottomSheet::javaClass.name)
    }

    private lateinit var binding: DialogBuyInfoDetailsPartBinding

    private val token: Token by args(ARG_TOKEN_KEY)

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBuyInfoDetailsPartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tokenSymbol = token.tokenSymbol
        with(binding) {
            textViewTitle.text = getString(R.string.buy_info_details_title, tokenSymbol)
            textViewSubTitle.text = getString(R.string.buy_info_details_subtitle, tokenSymbol)
            textViewExplain2.text = getString(R.string.buy_info_details_explain_2, tokenSymbol)
        }
        baseDialogBinding.buttonDone.apply {
            text = getString(R.string.buy_info_details_button)
            backgroundTintList = getColorStateList(R.color.bg_night)
            setTextColor(getColor(R.color.text_lime))
        }
    }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun getResult(): Any = token
}
