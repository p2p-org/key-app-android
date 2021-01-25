package com.p2p.wowlet.fragment.dashboard.dialog.currency

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogCurrencyBinding
import com.p2p.wowlet.fragment.dashboard.dialog.profile.viewmodel.ProfileViewModel
import com.wowlet.entities.enums.SelectedCurrency
import org.koin.androidx.viewmodel.ext.android.viewModel

class CurrencyDialog(
    private val onCurrencySelected: () -> Unit
) : DialogFragment() {

    private val profileViewModel: ProfileViewModel by viewModel()

    private var _binding: DialogCurrencyBinding? = null
    private val binding: DialogCurrencyBinding get() = _binding!!

    companion object {

        const val TAG_CURRENCY_DIALOG = "CurrencyDialog"
        fun newInstance(onCurrencySelected: () -> Unit): CurrencyDialog {
            return CurrencyDialog(onCurrencySelected)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_currency, container, false
        )
        binding.apply {
            viewModel = profileViewModel
            vPriceType.setOnCheckedChangeListener { group, checkedId ->  }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vClose.setOnClickListener {
                dismiss()
            }
            vDone.setOnClickListener {
                vPriceType.checkedRadioButtonId.run {
                    when(this) {
                        R.id.rbUSD -> profileViewModel.setSelectedCurrency(SelectedCurrency.USD)
                        R.id.rbEUR -> profileViewModel.setSelectedCurrency(SelectedCurrency.EUR)
                        R.id.rbCNY -> profileViewModel.setSelectedCurrency(SelectedCurrency.CNY)
                        R.id.rbKRW -> profileViewModel.setSelectedCurrency(SelectedCurrency.KRW)
                        R.id.rbRUB -> profileViewModel.setSelectedCurrency(SelectedCurrency.RUB)
                        else -> profileViewModel.setSelectedCurrency(SelectedCurrency.USD)
                    }
                    onCurrencySelected.invoke()
                }
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable=false
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}