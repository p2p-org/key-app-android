package org.p2p.wallet.common.di

import org.koin.core.module.Module

interface InjectionModule {
    fun create(): Module
}
