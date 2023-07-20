package org.p2p.core.token

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenMetadataExtension(
    val ruleOfProcessingTokenPriceWs: String? = null,
    val isPositionOnWs: Boolean? = null,
    val isTokenCellVisibleOnWs: Boolean? = null,
    val percentDifferenceToShowByPriceOnWs: Int? = null,
    val calculationOfFinalBalanceOnWs: Boolean? = null,
    val ruleOfFractionalPartOnWs: String? = null,
    val canBeHidden: Boolean? = null
) : Parcelable {

    companion object {
        val NONE = TokenMetadataExtension()
    }
}
