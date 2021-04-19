package com.p2p.wallet.dashboard.ui.dialog.editwalletname

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.restore.ui.secretkeys.utils.hideSoftKeyboard
import com.p2p.wallet.dashboard.ui.viewmodel.DashboardViewModel
import com.p2p.wallet.databinding.DialogEditNameBinding
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.utils.bindadapter.imageSource
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditWalletNameBottomSheet(
    val walletItem: Token,
    private val changedName: (Token) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        const val EDIT_WALLET_NAME = "EditWalletName"
        fun newInstance(
            walletItem: Token,
            changedName: (Token) -> Unit
        ): EditWalletNameBottomSheet =
            EditWalletNameBottomSheet(walletItem, changedName)
    }

    private var bottomSheet: View? = null
    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val binding: DialogEditNameBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_edit_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            currencyIcon.imageSource(walletItem.iconUrl)
            vEditTitle.setText(walletItem.tokenName)
            closeBtn.setOnClickListener { dismiss() }
            vEditTitle.setOnEditorActionListener { _, keyCode, _ ->
                if (keyCode == EditorInfo.IME_ACTION_DONE) {
                    val walletTitle = vEditTitle.text.toString()
                    dashboardViewModel.setChangeWallet(walletItem.depositAddress, walletTitle)
//                    walletItem.apply { tokenName = walletTitle }
                    activity?.run { hideSoftKeyboard(this@EditWalletNameBottomSheet) }
                    dismiss()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bottomSheet =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) ?: return
        bottomSheet!!.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheet?.let { BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED }
    }

    override fun onDestroy() {
        super.onDestroy()
        changedName.invoke(walletItem)
    }
}