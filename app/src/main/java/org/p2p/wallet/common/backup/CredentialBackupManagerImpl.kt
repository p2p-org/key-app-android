package org.p2p.wallet.common.backup

import android.content.Context
import com.google.android.gms.auth.blockstore.Blockstore
import com.google.android.gms.auth.blockstore.StoreBytesData
import timber.log.Timber

private const val TAG = "CREDENTIAL_BACKUP_MANAGER"

class CredentialBackupManagerImpl(private val context: Context) : CredentialBackupManager {

    private val blockStoreClient = Blockstore.getClient(context)

    private val listeners = mutableListOf<OnBackupResultListener>()

    override fun saveBytes(bytes: ByteArray) {
        val data = StoreBytesData.Builder()
            .setBytes(bytes)
            .setShouldBackupToCloud(true)
            .build()

        blockStoreClient.storeBytes(data)
            .addOnSuccessListener {
                Timber.tag(TAG).i("User sensitive data had been saved $it")
            }.addOnFailureListener {
                Timber.tag(TAG).e("Error while trying to save sensitive user data $it")
            }
    }

    override fun requestBytes() {
        blockStoreClient.retrieveBytes()
            .addOnSuccessListener { result ->
                val result = BackupState.BackupFound(result)
                Timber.tag(TAG).i("Backup was successfully received, backup size = ${result.result.size} bytes")
                listeners.forEach { it.onReceiveResult(result) }
            }.addOnFailureListener {
                val result = BackupState.BackupRetrieveFailure(it)
                Timber.tag(TAG).e("Error while try to retrieve backup $result")
                listeners.forEach { it.onReceiveResult(result) }
            }
    }

    override fun addOnBackupResultListener(listener: OnBackupResultListener) {
        listeners.add(listener)
    }

    override fun removeOnBackupResultListener(listener: OnBackupResultListener) {
        listeners.remove(listener)
    }
}

sealed interface BackupState {
    data class BackupFound(val result: ByteArray) : BackupState {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BackupFound

            if (!result.contentEquals(other.result)) return false

            return true
        }

        override fun hashCode(): Int {
            return result.contentHashCode()
        }
    }

    data class BackupRetrieveFailure(val exception: Exception) : BackupState
}

interface OnBackupResultListener {
    fun onReceiveResult(result: BackupState)
}
