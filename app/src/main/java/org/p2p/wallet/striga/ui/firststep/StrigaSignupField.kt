package org.p2p.wallet.striga.ui.firststep

import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

data class StrigaSignupField(
    val fieldValue: String,
    val type: StrigaSignupDataType,
    val isValid: Boolean
)
