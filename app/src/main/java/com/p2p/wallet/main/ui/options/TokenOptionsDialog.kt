package com.p2p.wallet.main.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.p2p.wallet.R
import com.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import com.p2p.wallet.databinding.DialogTokenOptionsBinding
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.TokenVisibility
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TokenOptionsDialog : NonDraggableBottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun show(fm: FragmentManager, token: Token) {
            TokenOptionsDialog()
                .withArgs(EXTRA_TOKEN to token)
                .show(fm, TokenOptionsDialog::javaClass.name)
        }
    }

    private val userInteractor: UserInteractor by inject()

    private val binding: DialogTokenOptionsBinding by viewBinding()

    private val token: Token by args(EXTRA_TOKEN)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_token_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { dismissAllowingStateLoss() }
            hideSwitch.isChecked = token.isHidden

            visibilityView.setOnClickListener { hideSwitch.isChecked = !hideSwitch.isChecked }
            hideSwitch.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    val visibility = if (isChecked) TokenVisibility.SHOWN else TokenVisibility.HIDDEN
                    userInteractor.setTokenHidden(token.publicKey, visibility.stringValue)
                }

                visibilityTextView.setText(if (isChecked) R.string.main_invisible else R.string.main_visible)
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}