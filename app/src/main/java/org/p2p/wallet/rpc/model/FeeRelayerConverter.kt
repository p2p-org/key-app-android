package org.p2p.wallet.rpc.model

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.wallet.rpc.api.RequestAccountMeta
import org.p2p.wallet.rpc.api.RequestInstruction

object FeeRelayerConverter {

    fun toNetwork(
        instruction: TransactionInstruction,
        pubkeys: List<String>
    ): RequestInstruction {
        return RequestInstruction(
            programIdIndex = pubkeys.indexOfFirst { it == instruction.programId.toBase58() },
            accounts = instruction.keys.map { toNetwork(it, pubkeys) },
            data = instruction.data
        )
    }

    private fun toNetwork(account: AccountMeta, pubkeys: List<String>): RequestAccountMeta =
        RequestAccountMeta(
            pubkeyIndex = pubkeys.indexOfFirst { account.publicKey.toBase58() == it },
            isSigner = account.isSigner,
            isWritable = account.isWritable
        )
}