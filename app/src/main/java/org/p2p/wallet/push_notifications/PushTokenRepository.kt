package org.p2p.wallet.push_notifications

import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class PushToken(val value: String)

class PushTokenRepository {
    private val firebaseMessaging: FirebaseMessaging
        get() = FirebaseMessaging.getInstance()

    suspend fun getPushToken(): PushToken {
        return suspendCancellableCoroutine { continuation ->
            firebaseMessaging.token
                .addOnSuccessListener { continuation.resume(PushToken(it)) }
                .addOnCanceledListener { continuation.cancel() }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}
