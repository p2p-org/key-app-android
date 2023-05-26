package org.p2p.wallet.newsend.smartselection

import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.SendSolanaFee

class FeeDebugInfoBuilder(
    private val sendInteractor: SendInteractor
) {

    suspend fun buildDebugInfo(solanaFee: SendSolanaFee?): String {
        val relayAccount = sendInteractor.getUserRelayAccount()
        val relayInfo = sendInteractor.getRelayInfo()
        return buildString {
            append("Relay account is created: ${relayAccount.isCreated}, balance: ${relayAccount.balance} (A)")
            appendLine()
            append("Min relay account balance required: ${relayInfo.minimumRelayAccountRent} (B)")
            appendLine()
            if (relayAccount.balance != null) {
                val diff = relayAccount.balance - relayInfo.minimumRelayAccountRent
                append("Remainder (A - B): $diff (R)")
                appendLine()
            }

            if (solanaFee == null) {
                append("Expected total fee in SOL: 0 (E)")
                appendLine()
                append("Needed top up amount (E - R): 0 (S)")
                appendLine()
                append("Expected total fee in Token: 0 (T)")
            } else {
                val accountBalances = solanaFee.feeRelayerFee.expectedFee.accountBalances
                val expectedFee = if (!relayAccount.isCreated) {
                    accountBalances + relayInfo.minimumRelayAccountRent
                } else {
                    accountBalances
                }
                append("Expected total fee in SOL: $expectedFee (E)")
                appendLine()

                val neededTopUpAmount = solanaFee.feeRelayerFee.totalInSol
                append("Needed top up amount (E - R): $neededTopUpAmount (S)")

                appendLine()

                val feePayerToken = solanaFee.feePayerToken
                val expectedFeeInSpl = solanaFee.feeRelayerFee.totalInSpl.orZero()
                    .fromLamports(feePayerToken.decimals)
                    .scaleLong()
                append("Expected total fee in Token: $expectedFeeInSpl ${feePayerToken.tokenSymbol} (T)")
            }
        }
    }
}
