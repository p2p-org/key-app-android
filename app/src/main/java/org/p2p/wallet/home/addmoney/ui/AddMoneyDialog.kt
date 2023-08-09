package org.p2p.wallet.home.addmoney.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.core.common.DrawableContainer
import org.p2p.core.token.Token
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.disableScrolling
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.recycler.decoration.offsetFinanceBlockDecoration
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogAddMoneyBinding
import org.p2p.wallet.home.addmoney.AddMoneyDialogContract
import org.p2p.wallet.home.addmoney.model.AddMoneyItemType
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.striga.StrigaFragmentFactory
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.viewBinding

class AddMoneyDialog :
    BaseMvpBottomSheet<AddMoneyDialogContract.View, AddMoneyDialogContract.Presenter>(R.layout.dialog_add_money),
    AddMoneyDialogContract.View {

    companion object {
        fun show(fm: FragmentManager) {
            val tag = AddMoneyDialog::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            AddMoneyDialog().show(fm, tag)
        }
    }

    private val binding: DialogAddMoneyBinding by viewBinding()
    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()
    private val strigaFragmentFactory: StrigaFragmentFactory by inject()

    override val presenter: AddMoneyDialogContract.Presenter by inject()

    private val cellAdapter = CommonAnyCellAdapter(
        mainCellDelegate(inflateListener = {
            it.setOnClickAction { _, item ->
                presenter.onItemClick(item.typedPayload())
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

    override fun showItemProgress(itemType: AddMoneyItemType, showProgress: Boolean) {
        cellAdapter.updateItem<MainCellModel>(
            predicate = { it is MainCellModel && it.payload == itemType },
            transform = {
                val rightSideCellModel = if (showProgress) {
                    RightSideCellModel.Progress(
                        indeterminateProgressTint = R.color.night
                    )
                } else {
                    RightSideCellModel.IconWrapper(
                        iconWrapper = IconWrapperCellModel.SingleIcon(
                            icon = ImageViewCellModel(
                                icon = DrawableContainer(R.drawable.ic_chevron_right),
                                iconTint = R.color.icons_mountain
                            )
                        )
                    )
                }
                it.copy(rightSideCellModel = rightSideCellModel)
            }
        )
    }

    override fun navigateToBankTransferTarget(target: StrigaUserStatusDestination) {
        strigaFragmentFactory.signupFlowFragment(target)?.let(::dismissAndNavigate)
    }

    override fun navigateToBankCard(tokenToBuy: Token, paymentMethod: PaymentMethod.MethodType) {
        dismissAndNavigate(
            NewBuyFragment.create(
                token = tokenToBuy,
                preselectedMethodType = paymentMethod
            )
        )
    }

    override fun navigateToKycPending() {
        dismiss()
        strigaFragmentFactory.showPendingBottomSheet(requireActivity().supportFragmentManager)
    }

    override fun setCellItems(items: List<AnyCellItem>) {
        cellAdapter.items = items
    }

    override fun navigateToCrypto() {
        dismissAndNavigate(receiveFragmentFactory.receiveFragment())
    }

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
