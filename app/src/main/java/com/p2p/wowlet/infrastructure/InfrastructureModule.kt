package com.p2p.wowlet.infrastructure

import android.content.Context
import com.p2p.wowlet.common.di.InjectionModule
import org.koin.dsl.module

object InfrastructureModule : InjectionModule {

    override fun create() = module {
        single {
            val context = get<Context>()
            val name = "${context.packageName}.prefs"
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }
    }
}