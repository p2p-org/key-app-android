package org.p2p.wallet.auth.web3authsdk

import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.edit
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import timber.log.Timber
import java.util.UUID
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.updates.NetworkConnectionStateProvider

private const val USER_CANCELED_DIALOG_CODE = 16
private const val USER_CANCELED_ADD_ACCOUNT_DIALOG_CODE = 13
private const val CALLER_NOT_WHITELISTED_CODE = 10
private const val INTERNAL_ERROR_CODE = 8
private const val PREFS_KEY_TOKEN_EXPIRE_TIME = "PREFS_KEY_TOKEN_EXPIRE_TIME"

class GoogleSignInHelper(
    private val connectionStateProvider: NetworkConnectionStateProvider,
    private val sharedPreferences: SharedPreferences,
    private val alarmErrorsLogger: AlarmErrorsLogger
) {
    private fun getSignInClient(context: Context): SignInClient {
        return Identity.getSignInClient(context)
    }

    fun showSignInDialog(context: Context, googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        val request = GetSignInIntentRequest.builder()
            .setNonce(UUID.randomUUID().toString())
            .setServerClientId(context.getString(R.string.webClientGoogleId))
            .build()

        getSignInClient(context).apply {
            signOut()
            getSignInIntent(request)
                .addOnSuccessListener { handleSuccessResult(it, googleSignInLauncher, context) }
                .addOnFailureListener { handleFailureResult(it, context) }
        }
    }

    private fun handleSuccessResult(
        intent: PendingIntent,
        signInLauncher: ActivityResultLauncher<IntentSenderRequest>,
        context: Context
    ) {
        try {
            signInLauncher.launch(IntentSenderRequest.Builder(intent).build())
        } catch (e: SendIntentException) {
            logWeb3Alarm("Google Client Launch Error: ${e.javaClass.simpleName}, ${e.message}")
            Timber.e(e, "Error on SignInIntent")
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFailureResult(exception: Exception, context: Context) {
        logWeb3Alarm("Google Client Sign In Error: ${exception.message}")
        Timber.e(exception, "Failure on SignInIntent")
        Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
    }

    fun parseSignInResult(
        context: Context,
        result: ActivityResult,
        errorHandler: GoogleSignInErrorHandler
    ): SignInCredential? {
        return try {
            getSignInClient(context).getSignInCredentialFromIntent(result.data)
        } catch (ex: ApiException) {
            sharedPreferences.edit { putLong(PREFS_KEY_TOKEN_EXPIRE_TIME, 0) }
            if (shouldErrorBeHandled(ex)) {
                if (shouldErrorBeLogged(ex)) {
                    logWeb3Alarm("Google Client Error: ${ex.statusCode}, ${ex.status.statusMessage}")
                    Timber.e(ex, "Error on getting Credential from result: ${ex.status.statusMessage}")
                }

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

    private fun shouldErrorBeLogged(exception: ApiException): Boolean {
        return exception.statusCode == USER_CANCELED_ADD_ACCOUNT_DIALOG_CODE ||
            exception.statusCode == CALLER_NOT_WHITELISTED_CODE
    }

    private fun logWeb3Alarm(errorMessage: String) {
        alarmErrorsLogger.triggerWeb3Alarm(errorMessage)
    }

    interface GoogleSignInErrorHandler {
        fun onConnectionError()
        fun onCommonError()
    }
}
