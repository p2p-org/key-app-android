package org.p2p.solanaj.model.core

import java.util.ArrayList
import java.util.Comparator
import java.util.HashMap

class AccountKeysList {
    private val accounts: HashMap<String, AccountMeta> = HashMap()

    fun add(accountMeta: AccountMeta) {
        val key = accountMeta.publicKey.toString()
        if (accounts.containsKey(key)) {
            if (!accounts[key]!!.isWritable && accountMeta.isWritable ||
                !accounts[key]!!.isSigner && accountMeta.isSigner
            ) {
                accounts[key] = accountMeta
            }
        } else {
            accounts[key] = accountMeta
        }
    }

    fun addAll(metas: Collection<AccountMeta>) {
        for (meta in metas) {
            add(meta)
        }
    }

    val list: ArrayList<AccountMeta>
        get() {
            val accountKeysList = ArrayList(accounts.values)
            accountKeysList.sortWith(metaComparator)
            return accountKeysList
        }

    companion object {
        private val metaComparator: Comparator<AccountMeta> = object : Comparator<AccountMeta> {
            override fun compare(am1: AccountMeta, am2: AccountMeta): Int {
                val cmpSigner = if (am1.isSigner == am2.isSigner) 0 else if (am1.isSigner) -1 else 1
                if (cmpSigner != 0) {
                    return cmpSigner
                }
                val cmpkWritable = if (am1.isWritable == am2.isWritable) 0 else if (am1.isWritable) -1 else 1
                return if (cmpkWritable != 0) {
                    cmpkWritable
                } else cmpSigner.compareTo(cmpkWritable)
            }
        }
    }
}