package com.p2p.wallet.user.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenAccount(
    val tokenName: String,
    val tokenSymbol: String,
    val iconUrl: String,
    val programAccount: TokenProgramAccount,
) : Parcelable