package org.p2p.wallet.auth.common

import android.content.Context
import android.content.IntentSender.SendIntentException
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import org.p2p.wallet.R
import timber.log.Timber
import java.util.UUID

private const val USER_CANCELED_DIALOG_CODE = 16

class GoogleSignInHelper() {

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
                }.addOnFailureListener {
                    Timber.w(it, "Failure on SignInIntent")
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun parseSignInResult(context: Context, result: ActivityResult): SignInCredential? {
        return try {
            getSignInClient(context).getSignInCredentialFromIntent(result.data)
        } catch (ex: ApiException) {
            if (ex.statusCode != USER_CANCELED_DIALOG_CODE) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show()
            }
            Timber.w(ex, "Error on getting Credential from result")
            null
        }
    }
}
