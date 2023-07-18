package org.p2p.wallet.striga.signup.steps.interactor

import org.p2p.core.common.TextContainer
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

data class StrigaSignupFieldState(
    val fieldValue: String,
    val type: StrigaSignupDataType,
    val isValid: Boolean,
    val errorMessage: TextContainer? = null
)
