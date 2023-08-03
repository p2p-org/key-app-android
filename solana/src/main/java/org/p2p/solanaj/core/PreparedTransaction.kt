package org.p2p.solanaj.core

class PreparedTransaction(
    val transaction: Transaction,
    val signers: List<Account>,
    val expectedFee: FeeAmount
) {
    fun toFormattedString(): String {
        val signersKeys = signers.joinToString {
            it.publicKey.toBase58()
        }
        val instructions = transaction.instructions.joinToString {
            "program_id: ${it.programId} keys: ${it.keys} data: ${it.data.size}"
        }

        return """
            Prepared transaction: 
            signers = $signersKeys,
            expectedFee = ($expectedFee)
            recent_bh = ${transaction.recentBlockHash}
            instructions = $instructions
            fee_payer = ${transaction.getFeePayer()?.toBase58()}
            signatures = ${transaction.allSignatures}
        """.trimIndent()
    }
}
