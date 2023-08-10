package org.p2p.token.service.repository.configurator

import org.p2p.core.token.TokenExtensions

interface TokenConfigurator {
    fun config(): TokenExtensions
}
