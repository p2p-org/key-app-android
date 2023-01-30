package org.p2p.wallet.lokalise

import android.content.Context
import android.content.ContextWrapper
import com.lokalise.sdk.BuildConfig
import com.lokalise.sdk.Lokalise
import com.lokalise.sdk.LokaliseCallback
import com.lokalise.sdk.LokaliseContextWrapper
import com.lokalise.sdk.LokaliseUpdateError
import timber.log.Timber

private const val TAG = "LokaliseService"

object LokaliseService {
    fun setup(context: Context, lokaliseToken: String, projectId: String) {
        with(Lokalise) {
            init(context, lokaliseToken, projectId)
            isPreRelease = BuildConfig.DEBUG
            logsEnabled = BuildConfig.DEBUG
            if (BuildConfig.DEBUG) {
                addCallback(getLokaliseCallback())
            }
            updateTranslations()
        }
    }

    fun wrap(context: Context): ContextWrapper {
        return LokaliseContextWrapper.wrap(context)
    }

    private fun getLokaliseCallback(): LokaliseCallback = object : LokaliseCallback {
        override fun onUpdateNotNeeded() {
            Timber.tag(TAG).i("No update needed")
        }

        override fun onUpdateFailed(error: LokaliseUpdateError) {
            Timber.tag(TAG).e("Error on fetching locale $error")
        }

        override fun onUpdated(oldBundleId: Long, newBundleId: Long) {
            Timber.tag(TAG).i("Update locale bundles: OldId = $oldBundleId, newId = $newBundleId")
        }
    }
}
