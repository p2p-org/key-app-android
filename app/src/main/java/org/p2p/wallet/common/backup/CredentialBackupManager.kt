package org.p2p.wallet.common.backup

/*
Interface was created to add support backup user sensitive data, such as passwords, or token's to auth flow,
If you want to check it:
1 - Step -> go to device settings, find backup, and ON it
2 - Try to save any byteArray
3 - Check size of saved array, and compare it

Cloud restore was not tested yet, i didn't find a way to make this
 */
interface CredentialBackupManager {

    fun saveBytes(bytes: ByteArray)
    fun requestBytes()
    fun addOnBackupResultListener(listener: OnBackupResultListener)
    fun removeOnBackupResultListener(listener: OnBackupResultListener)
}
