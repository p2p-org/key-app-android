package com.p2p.wallet.dashboard.ui.dialog.currency

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.dashboard.ui.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wallet.databinding.DialogCurrencyBinding
import com.p2p.wallet.dashboard.model.SelectedCurrency
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CurrencyDialog(
    private val onCurrencySelected: () -> Unit
) : DialogFragment() {

    companion object {
        const val TAG_CURRENCY_DIALOG = "CurrencyDialog"

        fun newInstance(onCurrencySelected: () -> Unit): CurrencyDialog {
            return CurrencyDialog(onCurrencySelected)
        }
    }

    private val profileViewModel: ProfileViewModel by viewModel()

    private val binding: DialogCurrencyBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_currency, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vPriceType.setOnCheckedChangeListener { group, checkedId ->
            }

            vClose.setOnClickListener {
                dismiss()
            }
            vDone.setOnClickListener {
                vPriceType.checkedRadioButtonId.run {
                    when (this) {
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

            val id = profileViewModel.getSelectedCurrencyCheckbox()
            vPriceType.check(id)
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}