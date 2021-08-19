package org.p2p.solanaj.model.core

import org.p2p.solanaj.programs.TokenProgram
import java.util.Comparator

class AccountKeysList {

    private val accounts = mutableListOf<AccountMeta>()

    fun addAccounts(metas: List<AccountMeta>) {
        metas.forEach { updateOrAddMeta(it) }
    }

    fun addAccount(meta: AccountMeta) {
        accounts.removeAll { it.publicKey.toBase58() == meta.publicKey.toBase58() }
        accounts.add(meta)
    }

    fun getSortedAccounts(): List<AccountMeta> {
        return accounts.sortedWith(metaComparator) as MutableList<AccountMeta>
    }

    /*
    * Checking for duplicates, keeping signer and writable
    *  */
    private fun updateOrAddMeta(accountMeta: AccountMeta) {
        val metaIndex = accounts.indexOfFirst { it.publicKey.equals(accountMeta.publicKey) }
        if (metaIndex != -1) {
            val account = accounts[metaIndex]
            if (!account.isWritable && accountMeta.isWritable || !account.isSigner && accountMeta.isSigner) {
                val isWritable = account.isWritable || accountMeta.isWritable
                accounts.removeAt(metaIndex)
                accounts.add(metaIndex, accountMeta.copy(isWritable = isWritable))
            }
        } else {
            accounts.add(accountMeta)
        }
    }

    /* Sorting accountMetas, first by isSigner, then by isWritable */
    private val metaComparator: Comparator<AccountMeta> = object : Comparator<AccountMeta> {
        override fun compare(x: AccountMeta, y: AccountMeta): Int {
            val cmpSigner = if (x.isSigner == y.isSigner) 0 else if (x.isSigner) -1 else 1
            if (cmpSigner != 0) return cmpSigner

            val cmpkWritable = if (x.isWritable == y.isWritable) 0 else if (x.isWritable) -1 else 1
            return compareTokenProgramIds(cmpkWritable, x, y)
        }
    }

    private fun compareTokenProgramIds(cmpkWritable: Int, x: AccountMeta, y: AccountMeta): Int {
        if (cmpkWritable == 0) {
            val sysvar = Sysvar.SYSVAR_RENT_ADDRESS.toBase58()
            if (x.publicKey.toBase58().startsWith(sysvar) &&
                y.publicKey.toBase58().startsWith(TokenProgram.PROGRAM_ID.toBase58()) ||
                y.publicKey.toBase58().startsWith(sysvar) &&
                x.publicKey.toBase58().startsWith(TokenProgram.PROGRAM_ID.toBase58())
            ) {
                return -1
            }
        }

        return if (cmpkWritable != 0) cmpkWritable else 1
    }
}