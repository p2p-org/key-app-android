package org.p2p.wallet.renbtc.model

import org.p2p.solanaj.kits.renBridge.LockAndMint

sealed class RenBtcSession {
    data class Active(val session: LockAndMint.Session) : RenBtcSession()
    data class Error(val throwable: Throwable) : RenBtcSession()
    object Loading : RenBtcSession()
}
