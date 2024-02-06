package org.p2p.wallet.home.addmoney.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.disableScrolling
import org.p2p.uikit.utils.recycler.decoration.offsetFinanceBlockDecoration
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogAddMoneyBinding
import org.p2p.wallet.home.addmoney.AddMoneyContract
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.ui.new.BuyFragment
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.striga.StrigaFragmentFactory
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.viewBinding

class AddMoneyBottomSheet :
    BaseMvpBottomSheet<AddMoneyContract.View, AddMoneyContract.Presenter>(R.layout.dialog_add_money),
    AddMoneyContract.View {

    companion object {
        fun show(fm: FragmentManager) {
            val tag = AddMoneyBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            AddMoneyBottomSheet().show(fm, tag)
        }
    }

    private val binding: DialogAddMoneyBinding by viewBinding()
    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()
    private val strigaFragmentFactory: StrigaFragmentFactory by inject()

    override val presenter: AddMoneyContract.Presenter by inject()

    private val cellAdapter = CommonAnyCellAdapter(
        mainCellDelegate(inflateListener = {
            it.setOnClickAction { _, item ->
                presenter.onButtonClick(item.typedPayload())
            }
        })
    )

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recyclerViewCells.disableScrolling()
            recyclerViewCells.attachAdapter(cellAdapter)
            recyclerViewCells.addItemDecoration(offsetFinanceBlockDecoration(8.toPx()))
        }
    }

    override fun navigateToBankTransferTarget(target: StrigaUserStatusDestination) {
        strigaFragmentFactory.signupFlowFragment(target)?.let(::dismissAndNavigate)
    }

    override fun navigateToBankCard(tokenToBuy: Token, paymentMethod: PaymentMethod.MethodType) {
        dismissAndNavigate(
            BuyFragment.create(
                token = tokenToBuy,
                preselectedMethodType = paymentMethod
            )
        )
    }

    override fun navigateToKycPending() {
        dismissAllowingStateLoss()
        strigaFragmentFactory.showPendingBottomSheet(requireActivity().supportFragmentManager)
    }

    override fun setCellItems(items: List<AnyCellItem>) {
        cellAdapter.items = items
    }

    override fun navigateToCrypto() {
        dismissAndNavigate(receiveFragmentFactory.receiveFragment())
    }

    /**
     * It's overridden because we need to pass [enableBottomNavOffset] param to false for this dialog
     * fixme: https://p2pvalidator.atlassian.net/browse/PWN-9348
     */
    override fun showUiKitSnackBar(
        message: String?,
        messageResId: Int?,
        onDismissed: () -> Unit,
        actionButtonResId: Int?,
        actionBlock: ((Snackbar) -> Unit)?
    ) {
        require(message != null || messageResId != null) {
            "Snackbar text must be set from `message` or `messageResId` params"
        }
        val snackbarText: String = message ?: messageResId?.let(::getString)!!
        val root = requireView().rootView
        if (actionButtonResId != null && actionBlock != null) {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                actionButtonText = getString(actionButtonResId),
                actionButtonListener = actionBlock,
                enableBottomNavOffset = false
            )
        } else {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                onDismissed = onDismissed,
                enableBottomNavOffset = false
            )
        }
    }

    private fun dismissAndNavigate(fragment: Fragment) {
        replaceFragment(fragment)
        dismiss()
    }
}
