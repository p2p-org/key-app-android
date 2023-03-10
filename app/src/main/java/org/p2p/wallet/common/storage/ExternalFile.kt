package org.p2p.wallet.common.storage

data class ExternalFile(
    val data: String,
    val lastModified: Long
) {

    fun isOutdated(outdatedTime: Long): Boolean {
        return System.currentTimeMillis() - lastModified > outdatedTime
    }
}
