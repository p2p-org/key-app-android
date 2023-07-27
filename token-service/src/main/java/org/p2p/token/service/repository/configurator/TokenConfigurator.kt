package org.p2p.token.service.repository.configurator

interface TokenConfigurator<T> {
    fun config(): T
}
