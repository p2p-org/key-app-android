package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import org.p2p.core.token.Token
import org.p2p.solanaj.kits.TokenExtensionsMap
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.send.repository.AccountInfoTokenExtensionsMapper.parseTokenExtensions

class GetTokenExtensionsUseCase(
    private val accountRepository: RpcAccountRepository,
) {

    suspend fun execute(token: Token.Active): TokenExtensionsMap {
        val tokenInfo = accountRepository.getAccountInfoParsed(token.mintAddress)
        return tokenInfo?.parseTokenExtensions().orEmpty().also {
            Timber.d("Token ${token.tokenSymbol} (${token.mintAddress}) extensions: $it")
        }
    }
}
