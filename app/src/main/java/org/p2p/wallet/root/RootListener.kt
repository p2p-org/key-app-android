package org.p2p.wallet.root

import android.net.Uri
import org.p2p.wallet.transaction.model.NewShowProgress

interface RootListener {
    fun showTransactionProgress(internalTransactionId: String, data: NewShowProgress)
    fun popBackStackToMain()
    fun triggerOnboardingDeeplink(deeplink: Uri)
    fun executeTransferViaLink(deeplink: Uri)
}
