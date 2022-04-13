package org.p2p.wallet.push_notifications.repository

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PushToken(val value: String)

class PushTokenRepository {

    private val firebaseMessaging: FirebaseMessaging
        get() = FirebaseMessaging.getInstance()

    suspend fun getPushToken(): PushToken = suspendCancellableCoroutine { continuation ->
        firebaseMessaging.token
            .addOnSuccessListener { continuation.resume(PushToken(it)) }
            .addOnCanceledListener { continuation.cancel() }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }

    suspend fun deletePushToken(): Unit = suspendCancellableCoroutine { continuation ->
        firebaseMessaging.deleteToken()
            .addOnSuccessListener { continuation.resume(Unit) }
            .addOnCanceledListener { continuation.cancel() }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}
