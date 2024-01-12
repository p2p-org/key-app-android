package org.p2p.wallet.deeplinks

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository

sealed interface SwapDeeplinkData {
    data class TokensFound(
        val tokenAMint: Base58String,
        val tokenBMint: Base58String,
    ) : SwapDeeplinkData

    data class NonStrictTokensFound(
        val nonStrictTokenA: String?,
        val nonStrictTokenB: String?
    ) : SwapDeeplinkData

    object TokensNotFound : SwapDeeplinkData
}

class SwapDeeplinkHandler(
    private val context: Context,
    private val swapTokensRepository: JupiterSwapTokensRepository
) {
    /**
     * https://s.key.app/...
     * or keyapp://s/... if came from website
     */
    fun isSwapDeeplink(data: Uri): Boolean {
        val swapSchemeMain = "https"
        val swapHostMain = context.getString(R.string.transfer_app_host_swap)
        val isHttpsFormat = data.host == swapHostMain && data.scheme == swapSchemeMain

        val transferHostAlternative = "s"
        val transferSchemeAlternative = context.getString(R.string.transfer_app_scheme_alternative)
        val isAlternativeFormat = data.host == transferHostAlternative && data.scheme == transferSchemeAlternative

        return isHttpsFormat || isAlternativeFormat
    }

    fun createSwapDeeplinkData(intent: Intent): DeeplinkData? {
        val data = intent.data ?: return null
        val target = DeeplinkTarget.fromScreenTab(ScreenTab.SWAP_SCREEN) ?: return null

        return DeeplinkData(
            target = target,
            pathSegments = data.pathSegments,
            args = data.queryParameterNames
                .filter { !data.getQueryParameter(it).isNullOrBlank() }
                .associateWith { data.getQueryParameter(it)!! },
            intent = intent
        )
    }

    suspend fun parseDeeplink(data: DeeplinkData): SwapDeeplinkData {
        if (!data.args.containsKey("from") || !data.args.containsKey("to")) {
            return SwapDeeplinkData.TokensNotFound
        }

        val inputMintOrSymbol = data.args.getValue("from")
        val outputMintOrSymbol = data.args.getValue("to")

        Timber.i("Tokens from deeplink: $inputMintOrSymbol $outputMintOrSymbol")

        val inputToken = swapTokensRepository.findTokenBySymbol(inputMintOrSymbol)
            ?: swapTokensRepository.findTokenByMint(inputMintOrSymbol.toBase58Instance())

        val outputToken = swapTokensRepository.findTokenBySymbol(outputMintOrSymbol)
            ?: swapTokensRepository.findTokenByMint(outputMintOrSymbol.toBase58Instance())

        if (inputToken == null || outputToken == null) {
            Timber.e(IllegalArgumentException("Tokens from deeplink not found: $inputMintOrSymbol $outputMintOrSymbol"))
            return SwapDeeplinkData.TokensNotFound
        }

        return if (inputToken.isStrictToken && outputToken.isStrictToken) {
            SwapDeeplinkData.TokensFound(
                tokenAMint = inputToken.tokenMint,
                tokenBMint = outputToken.tokenMint,
            )
        } else {
            SwapDeeplinkData.NonStrictTokensFound(
                nonStrictTokenA = inputToken.takeUnless { it.isStrictToken }?.tokenSymbol,
                nonStrictTokenB = outputToken.takeUnless { it.isStrictToken }?.tokenSymbol
            )
        }
    }
}
