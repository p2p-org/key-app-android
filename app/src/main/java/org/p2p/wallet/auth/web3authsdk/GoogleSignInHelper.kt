package org.p2p.wallet.auth.web3authsdk

import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.widget.Toast
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import org.p2p.wallet.R
import org.p2p.wallet.updates.ConnectionStateProvider
import timber.log.Timber
import java.util.UUID

private const val USER_CANCELED_DIALOG_CODE = 16
private const val INTERNAL_ERROR_CODE = 8

class GoogleSignInHelper(
    private val connectionStateProvider: ConnectionStateProvider,
) {
    private fun getSignInClient(context: Context): SignInClient {
        return Identity.getSignInClient(context)
    }

    fun showSignInDialog(context: Context, googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        val request = GetSignInIntentRequest.builder()
            .setNonce(UUID.randomUUID().toString())
            .setServerClientId(context.getString(R.string.googleClientId))
            .build()

        getSignInClient(context).apply {
            signOut()
            getSignInIntent(request)
                .addOnSuccessListener {
                    try {
                        googleSignInLauncher.launch(IntentSenderRequest.Builder(it).build())
                    } catch (e: SendIntentException) {
                        Timber.w(e, "Error on SignInIntent")
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Timber.w(it, "Failure on SignInIntent")
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun parseSignInResult(
        context: Context,
        result: ActivityResult,
        errorHandler: GoogleSignInErrorHandler
    ): SignInCredential? {
        return try {
            throw ApiException(Status.RESULT_TIMEOUT)
            getSignInClient(context).getSignInCredentialFromIntent(result.data)
        } catch (ex: ApiException) {
            Timber.e(ex, "Error on getting Credential from result")

            if (shouldErrorBeHandled(ex)) {
                if (connectionStateProvider.hasConnection()) {
                    errorHandler.onCommonError()
                } else {
                    errorHandler.onConnectionError()
                }
            }
            null
        }
    }

    private fun shouldErrorBeHandled(exception: ApiException): Boolean {
        return exception.statusCode != USER_CANCELED_DIALOG_CODE &&
            exception.statusCode != INTERNAL_ERROR_CODE
    }

    interface GoogleSignInErrorHandler {
        fun onConnectionError()
        fun onCommonError()
    }
}
