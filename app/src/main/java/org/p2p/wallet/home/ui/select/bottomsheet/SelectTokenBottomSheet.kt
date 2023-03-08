package org.p2p.wallet.home.ui.select.bottomsheet

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.databinding.DialogSelectTokenBinding
import org.p2p.wallet.home.ui.select.SelectTokenAdapter
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_ALL_TOKENS = "EXTRA_ALL_TOKENS"
private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class SelectTokenBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun show(
            fm: FragmentManager,
            tokens: List<Token>,
            requestKey: String,
            resultKey: String
        ) = SelectTokenBottomSheet().withArgs(
            EXTRA_ALL_TOKENS to tokens,
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        ).show(fm, SelectTokenBottomSheet::javaClass.name)
    }

    private val tokens: List<Token> by args(EXTRA_ALL_TOKENS)
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val buyAnalytics: BuyAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val binding: DialogSelectTokenBinding by viewBinding()

    private val tokenAdapter: SelectTokenAdapter by lazy {
        SelectTokenAdapter {
            setFragmentResult(requestKey, bundleOf(resultKey to it))
            dismissAllowingStateLoss()
            buyAnalytics.logBuyTokenChosen(it.tokenSymbol, analyticsInteractor.getPreviousScreenName())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_select_token, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            downImageView.setOnClickListener { dismissAllowingStateLoss() }
            tokenRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokenRecyclerView.attachAdapter(tokenAdapter)
            tokenAdapter.setItems(tokens)

            val isEmpty = tokens.isEmpty()
            tokenRecyclerView.isVisible = !isEmpty
            emptyTextView.isVisible = isEmpty
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow
}
