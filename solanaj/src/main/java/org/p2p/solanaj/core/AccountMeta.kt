package org.p2p.solanaj.core

data class AccountMeta(
    val publicKey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
) {

    companion object {
        fun findAccountIndex(accountMetaList: List<AccountMeta>, key: PublicKey): Int {
            for (i in accountMetaList.indices) {
                if (accountMetaList[i].publicKey.equals(key)) {
                    return i
                }
            }
            return -1
        }
    }
}
