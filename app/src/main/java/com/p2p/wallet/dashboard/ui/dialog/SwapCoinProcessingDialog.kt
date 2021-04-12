package com.p2p.wallet.dashboard.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.dashboard.ui.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wallet.databinding.DialogSwapCoinProcessingBinding
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SwapCoinProcessingDialog(private val goToWallet: () -> Unit) : DialogFragment() {

    private val swapViewModel: SwapViewModel by viewModel()

    companion object {
        const val SWAP_COIN_PROGRESS = "SWAP_COIN_PROGRESS"
        fun newInstance(goToWallet: () -> Unit): SwapCoinProcessingDialog = SwapCoinProcessingDialog(goToWallet)
    }

    private val binding: DialogSwapCoinProcessingBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_swap_coin_processing, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                goToWallet.invoke()
                dismiss()
            }
        }.start()*/
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}