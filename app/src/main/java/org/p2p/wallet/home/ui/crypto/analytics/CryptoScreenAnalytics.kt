package org.p2p.wallet.home.ui.crypto.analytics

import java.math.BigDecimal
import org.p2p.core.model.CurrencyMode
import org.p2p.core.utils.isMoreThan
import org.p2p.wallet.common.analytics.Analytics

private const val CRYPTO_SCREEN_OPENED = "Crypto_Screen_Opened"
private const val CRYPTO_AMOUNT_CLICK = "Crypto_Amount_Click"

private const val CRYPTO_RECEIVE_CLICK = "Crypto_Receive_Click"
private const val CRYPTO_SWAP_CLICK = "Crypto_Swap_Click"
private const val CRYPTO_TOKEN_CLICK = "Crypto_Token_Click"

private const val CRYPTO_CLAIM_TRANSFERED_VIEWED = "Crypto_Claim_Transfered_Viewed"
private const val CRYPTO_CLAIM_TRANSFERED_CLICK = "Crypto_Claim_Transfered_Click"

private const val USER_AGGREGATE_BALANCE_BASE = "User_Aggregate_Balance_Base"
private const val USER_HAS_POSITIVE_BALANCE_BASE = "User_Has_Positive_Balance_Base"

class CryptoScreenAnalytics(
    private val tracker: Analytics
) {

    fun logCryptoScreenOpened() {
        tracker.logEvent(event = CRYPTO_SCREEN_OPENED)
    }

    fun logCryptoAmountClick() {
        tracker.logEvent(event = CRYPTO_AMOUNT_CLICK)
    }

    fun logCryptoReceiveClick() {
        tracker.logEvent(event = CRYPTO_RECEIVE_CLICK)
    }

    fun logCryptoSwapClick() {
        tracker.logEvent(event = CRYPTO_SWAP_CLICK)
    }

    fun logCryptoTokenClick(tokenName: String, tokenSymbol: String) {
        tracker.logEvent(
            event = CRYPTO_TOKEN_CLICK,
            params = mapOf(
                "Token_Name" to tokenName,
                "Token_Symbol" to tokenSymbol,
            ),
        )
    }

    fun logCryptoClaimTransferedViewed(claimCount: Int) {
        tracker.logEvent(
            event = CRYPTO_CLAIM_TRANSFERED_VIEWED,
            params = mapOf(
                "Claim_Count" to claimCount,
            ),
        )
    }

    fun logCryptoClaimTransferedClicked() {
        tracker.logEvent(event = CRYPTO_CLAIM_TRANSFERED_CLICK)
    }

    fun logUserAggregateBalanceBase(balanceAmount: BigDecimal) {
        val amountInUsd = balanceAmount // TODO convert to Usd even if it will be euro.. etc
        tracker.logEvent(
            event = USER_AGGREGATE_BALANCE_BASE,
            params = mapOf(
                "Amount_USD" to amountInUsd,
                "Currency" to CurrencyMode.Fiat.Usd.fiatAbbreviation,
            ),
        )
        val hasPositiveBalance = balanceAmount.isMoreThan(BigDecimal.ZERO)
        logUserHasPositiveBalanceBase(hasPositiveBalance)
    }

    private fun logUserHasPositiveBalanceBase(isNotZero: Boolean) {
        tracker.logEvent(
            event = USER_HAS_POSITIVE_BALANCE_BASE,
            params = mapOf(
                "State" to isNotZero,
            ),
        )
    }
}
