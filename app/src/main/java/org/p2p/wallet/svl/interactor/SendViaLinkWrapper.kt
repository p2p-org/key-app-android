package org.p2p.wallet.svl.interactor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SendViaLinkWrapper(val link: String) : Parcelable
