package org.p2p.solanaj.core

class TransactionInstruction(
    val programId: PublicKey,
    val keys: List<AccountMeta>,
    val data: ByteArray
) {

    /*
    * Fee relayer excepts only unsigned bytes values, so we are formatting byteArray before sending
    * */
    fun getUnsignedBytes(): List<Int> =
        data.map { it.toUByte().toInt() }
}
