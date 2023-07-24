package org.p2p.wallet.home.ui.crypto.bottomsheet

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.core.token.Token
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogTokensVisibilityChangePartBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val ARG_TOKEN = "ARG_TOKEN"
private const val ARG_TOKEN_VISIBILITY_STATE = "ARG_TOKEN_VISIBILITY_STATE"

class TokenVisibilityChangeBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            token: Token.Active,
            isTokenHidden: Boolean,
            requestKey: String = ARG_REQUEST_KEY,
            resultKey: String = ARG_RESULT_KEY
        ) = TokenVisibilityChangeBottomSheet().withArgs(
            ARG_TOKEN to token,
            ARG_TOKEN_VISIBILITY_STATE to isTokenHidden,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        )
            .show(fm, TokenVisibilityChangeBottomSheet::javaClass.name)
    }

    private val token: Token.Active by args(ARG_TOKEN)
    private val isTokenHidden: Boolean by args(ARG_TOKEN_VISIBILITY_STATE)

    private lateinit var binding: DialogTokensVisibilityChangePartBinding

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = inflater.inflateViewBinding(container, attachToRoot = false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDoneButtonVisibility(isVisible = false)
        binding.buttonTokenVisibilityChangeState.apply {
            setText(
                if (isTokenHidden) {
                    R.string.my_crypto_show_token_button
                } else {
                    R.string.my_crypto_hide_token_button
                }
            )
            setOnClickListener {
                setFragmentResult(requestKey, bundleOf(resultKey to getResult()))
                dismissAllowingStateLoss()
            }
        }
    }

    override fun getResult(): Token.Active = token
}
