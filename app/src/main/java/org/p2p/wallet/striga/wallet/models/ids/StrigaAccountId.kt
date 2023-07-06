package org.p2p.wallet.striga.wallet.models.ids

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param value The ID of an account. Format example: 01c1f4e73d8b2587921c74e98951add0
 */
@JvmInline
@Parcelize
value class StrigaAccountId(val value: String) : Parcelable
