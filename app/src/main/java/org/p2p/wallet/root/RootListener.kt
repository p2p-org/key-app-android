package org.p2p.wallet.root

import android.net.Uri
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.transaction.model.NewShowProgress

interface RootListener {
    fun showTransactionProgress(internalTransactionId: String, data: NewShowProgress, handlerQualifierName: String)
    fun popBackStackToMain()
    fun triggerOnboardingDeeplink(deeplink: Uri)
    fun parseTransferViaLink(deeplink: SendViaLinkWrapper): Boolean
}
