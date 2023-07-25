package org.p2p.wallet.home.ui.crypto.handlers

import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.home.ui.crypto.MyCryptoContract
import org.p2p.wallet.home.ui.crypto.MyCryptoInteractor
import org.p2p.wallet.home.ui.crypto.mapper.MyCryptoMapper
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.progressstate.TransactionState

class BridgeClaimBundleClickHandler(
    private val cryptoMapper: MyCryptoMapper,
    private val transactionManager: TransactionManager,
    private val cryptoInteractor: MyCryptoInteractor,
) {
    suspend fun handle(view: MyCryptoContract.View?, canBeClaimed: Boolean, token: Token.Eth) {
        if (canBeClaimed) {
            view?.navigateToTokenClaim(token)
        } else {
            val latestActiveBundleId = token.latestActiveBundleId ?: return
            val bridgeBundle = cryptoInteractor.getClaimBundleById(latestActiveBundleId) ?: return
            val claimDetails = cryptoMapper.mapToClaimDetails(
                bridgeBundle = bridgeBundle,
                minAmountForFreeFee = cryptoInteractor.getClaimMinAmountForFreeFee(),
            )
            val progressDetails = cryptoMapper.mapShowProgressForClaim(
                amountToClaim = bridgeBundle.resultAmount.amountInToken,
                iconUrl = token.iconUrl.orEmpty(),
                claimDetails = claimDetails
            )
            transactionManager.emitTransactionState(
                latestActiveBundleId,
                TransactionState.Progress(description = R.string.bridge_claim_description_progress)
            )
            view?.showProgressDialog(
                bundleId = bridgeBundle.bundleId,
                progressDetails = progressDetails
            )
        }
    }
}
