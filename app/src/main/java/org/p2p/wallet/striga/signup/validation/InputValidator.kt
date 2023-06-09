package org.p2p.wallet.striga.signup.validation

import org.p2p.core.common.TextContainer

interface InputValidator {
    var errorMessage: TextContainer

    fun validate(input: String?): Boolean
}
