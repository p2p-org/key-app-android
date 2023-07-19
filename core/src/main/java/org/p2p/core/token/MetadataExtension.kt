package org.p2p.core.token

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetadataExtension(
    val ruleOfProcessingTokenPriceWs: String?,
    val isPositionOnWs: Boolean?,
    val isTokenCellVisibleOnWs: Boolean?,
    val percentDifferenceToShowByPriceOnWs: Int?,
    val calculationOfFinalBalanceOnWs: Boolean?,
    val ruleOfFractionalPartOnWs: String?,
    val canBeHidden: Boolean?
) : Parcelable
