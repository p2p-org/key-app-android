package org.p2p.solanaj.programs

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction

object MemoProgram {

    const val PROGRAM_ID = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr"

    fun createMemoInstruction(
        signer: PublicKey,
        memo: String
    ): TransactionInstruction {
        val accounts = listOf(
            AccountMeta(signer, isSigner = true, isWritable = true)
        )
        val data = memo.toByteArray()
        return TransactionInstruction(PublicKey(PROGRAM_ID), accounts, data)
    }
}
