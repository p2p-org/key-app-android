package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.Transaction
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures

class FeeRelayerTopUpInteractor {

    /// Get signature from transaction
    private fun getSignatures(
        transaction: Transaction,
        owner: Account,
        transferAuthorityAccount: Account?
    ): SwapTransactionSignatures {
        val signers = mutableListOf(owner)

        if (transferAuthorityAccount != null) {
            // fixme: this may cause presigner error
            signers.add(0, transferAuthorityAccount)
        }

        transaction.sign(signers)

        val ownerSignatureData = transaction.findSignature(owner.publicKey)?.signature

        val transferAuthoritySignatureData = if (transferAuthorityAccount != null) {
            transaction.findSignature(transferAuthorityAccount.publicKey)?.signature
        } else {
            null
        }

        if (ownerSignatureData.isNullOrEmpty()) {
            throw IllegalStateException("Invalid owner signature")
        }

        return SwapTransactionSignatures(
            userAuthoritySignature = ownerSignatureData,
            transferAuthoritySignature = transferAuthoritySignatureData
        )
    }
}