package com.p2p.wallet.main.model

import org.p2p.solanaj.kits.renBridge.LockAndMint

sealed class RenSessionResult {
    data class Active(val session: LockAndMint.Session) : RenSessionResult()

}