package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

@Parcelize
data class StrigaInitEurOffRampDetails(
    val challengeId: StrigaWithdrawalChallengeId,
    val dateExpires: ZonedDateTime,
) : Parcelable
