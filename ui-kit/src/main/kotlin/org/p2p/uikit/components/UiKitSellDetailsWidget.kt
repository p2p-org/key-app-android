package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.koin.core.component.KoinComponent
import org.p2p.core.model.CurrencyMode
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.utils.MOONPAY_DECIMAL
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatTokenForMoonpay
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSellInputAmountBinding
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.inflateViewBinding
import java.math.BigDecimal

data class SellWidgetViewState(
    val tokenSymbol: String,
    val fiatName: String,
    val currencyMode: CurrencyMode,
    val currencyModeToSwitch: CurrencyMode,
    val availableTokenAmount: BigDecimal,
    val fiatEarningAmount: BigDecimal,
    val feeInFiat: BigDecimal,
    val feeInToken: BigDecimal,
    val inputAmount: String,
    val sellQuoteInFiat: BigDecimal
)

class UiKitSellDetailsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent {

    private val binding = inflateViewBinding<WidgetSellInputAmountBinding>()

    var onAmountChanged: (newAmount: String) -> Unit = {}
    var onCurrencyModeSwitchClicked: () -> Unit = {}
    var onMaxAmountButtonClicked: () -> Unit = {}
    var onInputFocusChanged: OnFocusChangeListener? = null
        set(value) {
            binding.editTextAmount.onFocusChangeListener = value
            field = value
        }

    init {
        binding.textViewAvailableAmountValue.setOnClickListener { onMaxAmountButtonClicked.invoke() }
        binding.textViewSwitchCurrency.setOnClickListener { onCurrencyModeSwitchClicked.invoke() }
        binding.viewEditTextClickable.setOnClickListener {
            binding.editTextAmount.focusAndShowKeyboard(true)
        }
        AmountFractionTextWatcher.installOn(
            editText = binding.editTextAmount,
            maxDecimalsAllowed = MOONPAY_DECIMAL,
            maxIntLength = Int.MAX_VALUE,
            onValueChanged = { onAmountChanged.invoke(it) }
        )
    }

    fun render(viewState: SellWidgetViewState) {
        with(binding) {
            textViewQuoteValue.text = getString(
                R.string.sell_quote_in_fiat,
                viewState.tokenSymbol.uppercase(),
                viewState.sellQuoteInFiat.formatFiat(),
                viewState.fiatName
            )

            val switchModeSymbol = when (val mode = viewState.currencyModeToSwitch) {
                is CurrencyMode.Token -> mode.symbol
                is CurrencyMode.Fiat -> mode.fiatAbbreviation
            }

            textViewSwitchCurrency.text = getString(
                R.string.sell_switch_currency_mode_title,
                switchModeSymbol
            )

            textViewAvailableAmountValue.text = getString(
                R.string.sell_available_tokens,
                viewState.availableTokenAmount.formatTokenForMoonpay(),
                viewState.tokenSymbol
            )

            val activeModeSymbol = when (val mode = viewState.currencyMode) {
                is CurrencyMode.Token -> mode.symbol
                is CurrencyMode.Fiat -> mode.fiatAbbreviation
            }
            textViewAmountName.text = activeModeSymbol

            editTextAmount.setText(viewState.inputAmount)

            textViewFiatEarningTitle.text = getString(
                R.string.sell_fiat_earning_title,
                viewState.tokenSymbol,
                viewState.fiatName
            )

            textViewFiatEarningValue.text = getString(
                R.string.sell_fiat_earning_value,
                viewState.fiatEarningAmount.formatFiat(),
                viewState.fiatName
            )

            textViewFeesInfo.text = getString(
                R.string.sell_fees_information,
                viewState.feeInToken.formatTokenForMoonpay(),
                viewState.tokenSymbol,
                viewState.feeInFiat.formatFiat(),
                viewState.fiatName
            )
        }
    }

    fun focusInputAndShowKeyboard() {
        binding.editTextAmount.focusAndShowKeyboard()
    }
}
