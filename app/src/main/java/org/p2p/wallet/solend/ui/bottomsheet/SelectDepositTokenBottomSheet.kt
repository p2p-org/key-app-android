package org.p2p.wallet.solend.ui.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseRecyclerDoneBottomSheet
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.withArgs

private const val ARG_DEPOSIT_TOKENS = "ARG_DEPOSIT_TOKENS"

class SelectDepositTokenBottomSheet : BaseRecyclerDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            depositTokens: List<SolendDepositToken>,
            requestKey: String,
            resultKey: String
        ) = SelectDepositTokenBottomSheet().withArgs(
            ARG_TITLE to title,
            ARG_DEPOSIT_TOKENS to depositTokens,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, SelectDepositTokenBottomSheet::javaClass.name)
    }

    private val depositTokens: List<SolendDepositToken> by args(ARG_DEPOSIT_TOKENS)

    private val tokenAdapter: SelectDepositTokenAdapter by unsafeLazy {
        SelectDepositTokenAdapter {
            setFragmentResult(requestKey, bundleOf(resultKey to it))
            dismissAllowingStateLoss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDoneButtonVisibility(isVisible = false)
        setCloseButtonVisibility(isVisible = true)
        with(recyclerBinding) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(tokenAdapter)
            tokenAdapter.setItems(depositTokens)
        }
    }

    override fun getResult(): Any? = tokenAdapter.selectedItem
}
