package org.p2p.solanaj.model.core

import java.util.Comparator

class AccountKeysList {

    private val accounts = mutableListOf<AccountMeta>()

    fun addMetas(metas: List<AccountMeta>) {
        metas
            .sortedWith(metaComparator)
            .forEach { updateOrAddMeta(it) }
    }

    fun getSortedAccounts(): MutableList<AccountMeta> = accounts

    /*
    * Checking for duplicates, keeping signer and writable
    *  */
    private fun updateOrAddMeta(accountMeta: AccountMeta) {
        val metaIndex = accounts.indexOfFirst { it.publicKey.equals(accountMeta.publicKey) }
        if (metaIndex != -1) {
            val account = accounts[metaIndex]
            if (!account.isWritable && accountMeta.isWritable || !account.isSigner && accountMeta.isSigner) {
                accounts.removeAt(metaIndex)
                accounts.add(metaIndex, accountMeta)
            }
        } else {
            accounts.add(accountMeta)
        }
    }

    /* Sorting accountMetas, first by isSigner, then by isWritable */
    private val metaComparator: Comparator<AccountMeta> = object : Comparator<AccountMeta> {
        override fun compare(am1: AccountMeta, am2: AccountMeta): Int {
            val cmpSigner = if (am1.isSigner == am2.isSigner) 0 else if (am1.isSigner) -1 else 1
            if (cmpSigner != 0) return cmpSigner

            val cmpkWritable = if (am1.isWritable == am2.isWritable) 0 else if (am1.isWritable) -1 else 1
            return if (cmpkWritable != 0) cmpkWritable else 1
        }
    }
}