package org.p2p.core.common.di

import org.koin.core.module.Module

interface InjectionModule {
    fun create(): Module
}
