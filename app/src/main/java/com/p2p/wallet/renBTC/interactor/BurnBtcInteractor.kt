package com.p2p.wallet.renBTC.interactor

import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.kits.renBridge.BurnAndRelease
import org.p2p.solanaj.kits.renBridge.NetworkConfig
import org.p2p.solanaj.rpc.Environment
import java.math.BigDecimal
import java.math.BigInteger

class BurnBtcInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: EnvironmentManager,
    private val rpcRepository: RpcRepository
) {

    companion object {
        private const val BURN_FEE_LENGTH = 97L
    }

    suspend fun submitBurnTransaction(recipient: String, amount: BigInteger): String = withContext(Dispatchers.IO) {
        val signer = tokenKeyProvider.publicKey.toPublicKey()
        val signerSecretKey = tokenKeyProvider.secretKey
        val burnAndRelease = BurnAndRelease(getNetworkConfig())

        val burnDetails = burnAndRelease.submitBurnTransaction(
            signer,
            amount.toString(),
            recipient,
            Account(signerSecretKey)
        )

        val burnState = burnAndRelease.getBurnState(burnDetails, amount.toString())
        println("txHash " + burnState.txHash)
        return@withContext burnAndRelease.release()
    }

    suspend fun getBurnFee(): BigDecimal {
        val fee = rpcRepository.getMinimumBalanceForRentExemption(BURN_FEE_LENGTH).toBigInteger()
        return fee.fromLamports().add(BigDecimal("0.000005")).scaleMedium()
    }

    private fun getNetworkConfig(): NetworkConfig =
        when (environmentManager.loadEnvironment()) {
            Environment.DEVNET -> NetworkConfig.DEVNET()
            Environment.MAINNET,
            Environment.SOLANA -> NetworkConfig.MAINNET()
        }
}