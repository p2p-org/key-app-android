package org.p2p.wallet.intercom

import android.app.Application
import org.koin.core.component.KoinComponent

object IntercomService : KoinComponent {
    fun setup(app: Application, apiKey: String, appId: String) = Unit

    fun signIn(userId: String) = Unit

    fun logout() = Unit

    fun showMessenger() = Unit
}
