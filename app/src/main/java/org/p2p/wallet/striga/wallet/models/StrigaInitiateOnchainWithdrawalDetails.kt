package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import java.math.BigInteger
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

@Parcelize
data class StrigaInitiateOnchainWithdrawalDetails(
    val challengeId: StrigaWithdrawalChallengeId,
    val dateExpires: ZonedDateTime,
    val transaction: Transaction,
    val feeEstimate: StrigaOnchainWithdrawalFees,
) : Parcelable {

    /**
     * @param userId is the user id in Striga
     * @param amount in wei/satoshi or cents, depending on is the currency a fiat/stable or plain crypto
     */
    @Parcelize
    data class Transaction(
        val userId: String,
        val sourceAccountId: StrigaAccountId,
        val parentWalletId: StrigaWalletId,
        val currency: StrigaNetworkCurrency,
        val amount: BigInteger,
        val status: StrigaOnchainTxStatus,
        val txType: StrigaOnchainTxType,
        val blockchainDestinationAddress: String,
        val blockchainNetwork: StrigaBlockchainNetworkInfo,
        val transactionCurrency: StrigaNetworkCurrency,
    ) : Parcelable
}
