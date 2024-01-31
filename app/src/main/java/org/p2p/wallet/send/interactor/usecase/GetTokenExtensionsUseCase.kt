package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import org.p2p.core.crypto.Base58String
import org.p2p.solanaj.kits.TokenExtensionsMap
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.send.repository.AccountInfoTokenExtensionsMapper.parseTokenExtensions

class GetTokenExtensionsUseCase(
    private val accountRepository: RpcAccountRepository,
) {

    suspend fun execute(mintAddress: String): TokenExtensionsMap {
        val tokenInfo = accountRepository.getAccountInfoParsed(mintAddress)
        return tokenInfo?.parseTokenExtensions()
            .orEmpty()
            .also { Timber.d("Token $mintAddress extensions: $it") }
    }

    suspend fun execute(mintAddress: Base58String): TokenExtensionsMap {
        return execute(mintAddress.base58Value)
    }
}
