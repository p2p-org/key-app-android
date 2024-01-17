package org.p2p.wallet.send.repository

import org.p2p.core.network.gson.GsonProvider
import org.p2p.solanaj.kits.AccountInfoParsed
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig

object AccountInfoTokenExtensionsMapper {
    fun AccountInfoParsed.parseTokenExtensions(): Map<String, AccountInfoTokenExtensionConfig> {
        val gson = GsonProvider().provide()
        val extensions = data.parsed.info.extensions?.associateBy { it.name } ?: emptyMap()

        return extensions.mapNotNull {
            if (it.value.state == null) return@mapNotNull null
            val rawValue = it.value.state!!.toString()

            val configResult: AccountInfoTokenExtensionConfig = when (it.key) {
                AccountInfoTokenExtensionConfig.TransferFeeConfig.NAME -> {
                    gson.fromJson(
                        rawValue,
                        AccountInfoTokenExtensionConfig.TransferFeeConfig::class.java
                    )
                }
                AccountInfoTokenExtensionConfig.InterestBearingConfig.NAME -> {
                    gson.fromJson(
                        rawValue,
                        AccountInfoTokenExtensionConfig.InterestBearingConfig::class.java
                    )
                }
                else -> null
            } ?: return@mapNotNull null

            it.key to configResult
        }.toMap()
    }
}
