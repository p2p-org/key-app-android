package org.p2p.wallet.newsend.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.utils.toPublicKey

const val SEND_LINK_FORMAT = "https://t.key.app/"

@Parcelize
data class TemporaryAccount(
    val symbols: String,
    val address: String,
    val keypair: String
) : Parcelable {

    @IgnoredOnParcel
    val publicKey: PublicKey get() = address.toPublicKey()

    fun generateFormattedLink(): String = "$SEND_LINK_FORMAT$symbols"
}
