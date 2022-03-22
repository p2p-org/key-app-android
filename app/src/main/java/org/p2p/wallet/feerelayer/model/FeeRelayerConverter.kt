package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.wallet.feerelayer.api.RequestAccountMeta
import org.p2p.wallet.feerelayer.api.RequestInstruction
import org.p2p.wallet.feerelayer.api.SwapTransactionSignaturesRequest

object FeeRelayerConverter {

    fun toNetwork(
        instruction: TransactionInstruction,
        pubkeys: List<String>
    ): RequestInstruction {
        return RequestInstruction(
            programIdIndex = pubkeys.indexOfFirst { it == instruction.programId.toBase58() },
            accounts = instruction.keys.map { toNetwork(it, pubkeys) },
            data = instruction.getUnsignedBytes()
        )
    }

    fun toNetwork(signatures: SwapTransactionSignatures): SwapTransactionSignaturesRequest =
        SwapTransactionSignaturesRequest(
            userAuthoritySignature = signatures.userAuthoritySignature,
            transferAuthoritySignature = signatures.transferAuthoritySignature
        )

    private fun toNetwork(account: AccountMeta, pubkeys: List<String>): RequestAccountMeta =
        RequestAccountMeta(
            pubkeyIndex = pubkeys.indexOfFirst { account.publicKey.toBase58() == it },
            isSigner = account.isSigner,
            isWritable = account.isWritable
        )
}
