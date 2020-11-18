package com.p2p.wowlet.fragment.swap.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogSwapCoinProcessingBinding
import com.p2p.wowlet.fragment.swap.viewmodel.SwapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SwapCoinProcessingDialog (private val goToWallet: () -> Unit): DialogFragment() {

    private val swapViewModel: SwapViewModel by viewModel()
    lateinit var binding: DialogSwapCoinProcessingBinding

    companion object {
        const val SWAP_COIN_PROGRESS = "SWAP_COIN_PROGRESS"
        fun newInstance(goToWallet: () -> Unit): SwapCoinProcessingDialog = SwapCoinProcessingDialog(goToWallet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_swap_coin_processing, container, false
        )
        binding.viewModel = swapViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                goToWallet.invoke()
                dismiss()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}