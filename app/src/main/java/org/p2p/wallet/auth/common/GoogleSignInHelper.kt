package org.p2p.wallet.auth.common

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import org.p2p.wallet.R
import timber.log.Timber
import java.util.UUID

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
                    googleSignInLauncher.launch(IntentSenderRequest.Builder(it).build())
                }.addOnFailureListener {
                    Timber.w(it, "Error on SignInIntent")
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun parseSignInResult(context: Context, result: ActivityResult): SignInCredential? {
        return if (result.resultCode == Activity.RESULT_OK) {
            getSignInClient(context).getSignInCredentialFromIntent(result.data)
        } else {
            Timber.w("Error on getting Credential from result: $result")
            null
        }
    }
}
