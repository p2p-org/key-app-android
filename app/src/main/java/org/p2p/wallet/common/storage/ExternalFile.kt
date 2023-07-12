package org.p2p.wallet.common.storage

data class ExternalFile(
    val data: String,
    val fileName: String
) {

    fun getLastModified(prefix: String): String {
        return fileName.substringAfter(prefix)
    }
}
