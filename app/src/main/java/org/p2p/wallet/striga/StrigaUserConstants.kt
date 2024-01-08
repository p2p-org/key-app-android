package org.p2p.wallet.striga

import java.util.Calendar
import org.p2p.core.utils.MillisSinceEpoch

object StrigaUserConstants {
    const val EXPECTED_INCOMING_TX_YEARLY = "MORE_THAN_15000_EUR"
    const val EXPECTED_OUTGOING_TX_YEARLY = "MORE_THAN_15000_EUR"
    const val SELF_PEP_DECLARATION = false
    const val PURPOSE_OF_ACCOUNT = "CRYPTO_PAYMENTS"

    val USER_FILTER_START_DATE: MillisSinceEpoch = Calendar.getInstance().run {
        set(2023, 5 /*month is zero based*/, 15)
        timeInMillis
    }
}
