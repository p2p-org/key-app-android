package org.p2p.wallet.receive.network

import kotlinx.coroutines.launch
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toUsd
import timber.log.Timber
import java.math.BigInteger

class ReceiveNetworkTypePresenter(
    private val renBtcInteractor: RenBtcInteractor,
    private val userInteractor: UserInteractor,
    private val transactionAmountRepository: RpcAmountRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val tokenInteractor: TokenInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val environmentManager: EnvironmentManager,
    networkType: NetworkType
) : BasePresenter<ReceiveNetworkTypeContract.View>(),
    ReceiveNetworkTypeContract.Presenter {

    private var selectedNetworkType: NetworkType = networkType

    private val tokensValidForBuy = listOf("SOL", "USDC")

    override fun load() {
        view?.setCheckState(selectedNetworkType)
    }

    override fun onNetworkChanged(networkType: NetworkType) {
        launch {
            selectedNetworkType = networkType
            if (selectedNetworkType == NetworkType.SOLANA) {
                view?.navigateToReceive(selectedNetworkType)
            } else {
                onBitcoinSelected()
            }
            val analyticsNetworkType = if (networkType == NetworkType.SOLANA) {
                ReceiveAnalytics.ReceiveNetwork.SOLANA
            } else {
                ReceiveAnalytics.ReceiveNetwork.BITCOIN
            }
            receiveAnalytics.logReceiveChangingNetwork(analyticsNetworkType)
        }
    }

    override fun onTopupSelected(isSelected: Boolean) {
        if (!isSelected) {
            view?.close()
            return
        }
        launch {
            try {
                val tokensForBuy = userInteractor.getTokensForBuy(tokensValidForBuy)
                view?.showTokensForBuy(tokensForBuy)
                return@launch
            } catch (e: Exception) {
                view?.showErrorMessage(e)
            }
        }
    }

    override fun onBuySelected(isSelected: Boolean) {
        launch {
            try {
                val mintAddress = when (environmentManager.loadEnvironment()) {
                    Environment.DEVNET -> Constants.REN_BTC_DEVNET_MINT
                    else -> Constants.REN_BTC_DEVNET_MINT_ALTERNATE
                }
                tokenInteractor.openTokenAccount(mintAddress)
                view?.navigateToReceive(selectedNetworkType)
            } catch (e: Exception) {
                Timber.e("Error on launching RenBtc session $e")
                view?.showErrorMessage(e)
            }
        }
    }

    override fun onBtcSelected(isSelected: Boolean) {
        if (isSelected) {
            view?.navigateToReceive(selectedNetworkType)
        }
    }

    private fun onBitcoinSelected() {
        launch {
            try {
                view?.showLoading(true)
                val userTokens = userInteractor.getUserTokens()
                val renBtcWallet = userTokens.firstOrNull { it.isRenBTC }

                if (renBtcWallet == null) {
                    val userPublicKey = tokenKeyProvider.publicKey
                    val sol = userTokens.find { it.isSOL && it.publicKey == userPublicKey }
                        ?: throw IllegalStateException("No SOL account found")
                    createBtcWallet(sol)
                } else {
                    if (isRenBtcSessionActive()) {
                        receiveAnalytics.logReceiveSettingBitcoin()
                        view?.navigateToReceive(selectedNetworkType)
                    } else {
                        view?.showNetworkInfo(selectedNetworkType)
                    }
                    launchRenBtcSession()
                }
            } catch (e: Throwable) {
                Timber.e("Error on switch network: $e")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private suspend fun launchRenBtcSession() {
        val session = renBtcInteractor.findActiveSession()
        if (session != null && session.isValid) {
            receiveAnalytics.logReceiveSettingBitcoin()
            view?.navigateToReceive(selectedNetworkType)
        } else {
            view?.showNetworkInfo(selectedNetworkType)
        }
    }

    private suspend fun isRenBtcSessionActive(): Boolean {
        val session = renBtcInteractor.findActiveSession()
        return session != null && session.isValid
    }

    private suspend fun createBtcWallet(sol: Token.Active) {
        val btcMinPrice = transactionAmountRepository.getMinBalanceForRentExemption()
        val solAmount = sol.total.toLamports(sol.decimals)
        val isAmountEnough = (solAmount - btcMinPrice) >= BigInteger.ZERO
        if (isAmountEnough) {
            val priceInSol = btcMinPrice.fromLamports(sol.decimals).scaleLong()
            val priceInUsd = priceInSol.toUsd(sol)
            view?.showBuy(priceInSol, priceInUsd)
        } else {
            view?.showTopup()
        }
    }
}
