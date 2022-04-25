package org.p2p.wallet.swap.model.orca

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.swap.model.Slippage

@Parcelize
class OrcaSettingsResult(
    val newSlippage: Slippage,
    val newFeePayerToken: Token.Active
) : Parcelable
