package com.p2p.wallet.swap.interactor.orca

import com.p2p.wallet.R
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.model.orca.OrcaSwapRequest
import com.p2p.wallet.swap.model.orca.OrcaSwapResult
import com.p2p.wallet.swap.model.orca.OrcaToken
import com.p2p.wallet.swap.repository.OrcaSwapRepository
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import timber.log.Timber
import java.math.BigInteger

const val SWAP_TAG = "OrcaSwap"

class OrcaSwapInteractor(
    private val rpcRepository: RpcRepository,
    private val swapRepository: OrcaSwapRepository,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val orcaAddressInteractor: OrcaAddressInteractor
) {

    /*
     * TODO: optimize this one! remove [TokenSwap] move logic here
     * */
    suspend fun swap(
        request: OrcaSwapRequest
    ): OrcaSwapResult {
        val accountAddressA = userInteractor.findAccountAddress(request.sourceMint.toBase58())

        val owner = tokenKeyProvider.publicKey.toPublicKey()
        val associatedAddress = try {
            Timber.tag(SWAP_TAG).d("Searching for SPL token address")
            orcaAddressInteractor.findSplTokenAddress(request.destinationMint.toBase58(), owner)
        } catch (e: IllegalStateException) {
            Timber.tag(SWAP_TAG).d("Searching address failed, address is wrong")
            return OrcaSwapResult.Error(R.string.error_invalid_address)
        }

        /* If account is not found, create one */
        val accountInfo = rpcRepository.getAccountInfo(associatedAddress)
        val value = accountInfo?.value
        val associatedNotNeeded = value?.owner == TokenProgram.PROGRAM_ID.toString() && value.data != null

        val signature = swapRepository.swap(
            Account(tokenKeyProvider.secretKey),
            request,
            accountAddressA,
            associatedAddress,
            !associatedNotNeeded
        )

        return OrcaSwapResult.Success(signature)
    }

    suspend fun mapTokensForDestination(orcaTokens: List<OrcaToken>): List<Token> {
        val userTokens = userInteractor.getUserTokens()
        val allTokens = orcaTokens.mapNotNull { orcaToken ->
            val userToken = userTokens.find { it.mintAddress == orcaToken.mint }
            if (userToken != null) {
                return@mapNotNull userToken
            } else {
                return@mapNotNull userInteractor.findTokenData(orcaToken.mint)
            }
        }

        return allTokens.sortedByDescending { it is Token.Active }
    }

    suspend fun getLamportsPerSignature(): BigInteger = rpcRepository.getFees(null)

    suspend fun getAccountMinForRentExemption(): BigInteger =
        rpcRepository
            .getMinimumBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH.toLong())
            .toBigInteger()

    fun calculateNetworkFee(
        source: Token,
        destination: Token,
        lamportsPerSignature: BigInteger,
        minRentExemption: BigInteger
    ): BigInteger {
        // default fee
        var feeInLamports = lamportsPerSignature * BigInteger.valueOf(2L)

        // if token is native, a fee for creating wrapped SOL is needed
        if (source.isSOL) {
            feeInLamports += lamportsPerSignature
            feeInLamports += minRentExemption
        }

        // if destination wallet is selected
        // if destination wallet is a wrapped sol or not yet created a fee for creating it is needed
        if (destination.mintAddress == Token.WRAPPED_SOL_MINT || destination is Token.Other) {
            feeInLamports += minRentExemption
        }

        // fee relayer
        if (isFeeRelayerEnabled(source, destination)) {
            // fee for creating a SOL account
            feeInLamports += lamportsPerSignature
        }

        return feeInLamports
    }

    private fun isFeeRelayerEnabled(source: Token, destination: Token): Boolean {
        return !source.isSOL && !destination.isSOL
    }
}