package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import android.content.Context
import android.util.AttributeSet
import org.koin.core.component.KoinComponent
import org.p2p.core.model.CurrencyMode
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.databinding.WidgetSellInputAmountBinding
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.inflateViewBinding
import java.math.BigDecimal

class UiKitSellDetailsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent {

    private val binding = inflateViewBinding<WidgetSellInputAmountBinding>()

    var onAmountChanged: (newAmount: BigDecimal) -> Unit = {}
    var onCurrencyModeSwitchClicked: () -> Unit = {}
    var onMaxAmountButtonClicked: () -> Unit = {}

    class ViewState(
        val tokenSymbol: String,
        val fiatName: String,
        val currencyMode: CurrencyMode,
        val availableTokenAmount: BigDecimal,
        val fiatEarningAmount: BigDecimal,
        val tokenSellAmount: BigDecimal,
        val feeInFiat: BigDecimal,
        val feeInToken: BigDecimal,
        val sellQuoteInFiat: BigDecimal
    )

    init {
        binding.textViewAvailableAmountValue.setOnClickListener { onMaxAmountButtonClicked.invoke() }
        binding.textViewSwitchCurrency.setOnClickListener { onCurrencyModeSwitchClicked.invoke() }
        binding.editTextAmount.doAfterTextChanged { editable ->
            onAmountChanged.invoke(parseStringToBigDecimal(editable.toString()))
        }
    }

    private fun parseStringToBigDecimal(value: String): BigDecimal = value.toBigDecimalOrZero()

    fun render(viewState: ViewState) {
        with(binding) {
            textViewQuoteValue.text = getString(
                org.p2p.uikit.R.string.sell_quote_in_fiat,
                viewState.tokenSymbol.uppercase(),
                viewState.sellQuoteInFiat.formatFiat(),
                viewState.fiatName
            )

            textViewSwitchCurrency.text = getString(
                org.p2p.uikit.R.string.sell_switch_currency_mode_title,
                viewState.fiatName
            )

            textViewAvailableAmountValue.text = getString(
                org.p2p.uikit.R.string.sell_available_tokens,
                viewState.availableTokenAmount,
                viewState.tokenSymbol
            )

            textViewFiatEarningTitle.text = getString(
                org.p2p.uikit.R.string.sell_fiat_earning_title,
                viewState.tokenSymbol,
                viewState.fiatName
            )

            textViewFiatEarningValue.text = getString(
                org.p2p.uikit.R.string.sell_fiat_earning_value,
                viewState.fiatEarningAmount.formatFiat(),
                viewState.fiatName
            )

            textViewFeesInfo.text = getString(
                org.p2p.uikit.R.string.sell_fees_information,
                viewState.feeInToken,
                viewState.tokenSymbol,
                viewState.feeInFiat,
                viewState.fiatName
            )
        }
    }
}
