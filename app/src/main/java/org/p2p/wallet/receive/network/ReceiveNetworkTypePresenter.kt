package org.p2p.wallet.receive.network

import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.auth.analytics.RenBtcAnalytics
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.core.token.Token
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.newsend.model.NetworkType
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.core.utils.Constants
import org.p2p.core.utils.Constants.MIN_REQUIRED_ACCOUNT_INFO_DATA_LENGTH
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import timber.log.Timber
import kotlinx.coroutines.launch

class ReceiveNetworkTypePresenter(
    private val renBtcInteractor: RenBtcInteractor,
    private val userInteractor: UserInteractor,
    private val transactionAmountRepository: RpcAmountRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val tokenInteractor: TokenInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val environmentManager: NetworkEnvironmentManager,
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val renBtcAnalytics: RenBtcAnalytics,
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
                resolveBuyScreensByFeatureToggle(tokensForBuy)
            } catch (e: Exception) {
                view?.showErrorMessage(e)
            }
        }
    }

    private fun resolveBuyScreensByFeatureToggle(tokensForBuy: List<Token>) {
        if (newBuyFeatureToggle.value) {
            val usdc = tokensForBuy.first { it.tokenSymbol == Constants.USDC_SYMBOL }
            view?.showNewBuyFragment(usdc)
        } else {
            view?.showTokensForBuy(tokensForBuy)
        }
    }

    override fun onBuySelected(isSelected: Boolean) {
        launch {
            try {
                view?.showLoading(true)
                val mintAddress = when (environmentManager.loadCurrentEnvironment()) {
                    NetworkEnvironment.DEVNET -> Constants.REN_BTC_DEVNET_MINT
                    else -> Constants.REN_BTC_DEVNET_MINT_ALTERNATE
                }
                tokenInteractor.createAccount(mintAddress)
                renBtcAnalytics.logRenBtcAccountCreated(creationSuccess = true)
                view?.navigateToReceive(selectedNetworkType)
            } catch (e: Exception) {
                Timber.e("Error on launching RenBtc session $e")
                renBtcAnalytics.logRenBtcAccountCreated(creationSuccess = false)

                view?.showErrorMessage(e)
                view?.showLoading(false)
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

                    val isWeb3AuthUser = userSignUpDetailsStorage.getLastSignUpUserDetails() != null
                    if (isWeb3AuthUser) {
                        showCreateByFeeRelay()
                    } else {
                        createBtcWallet(sol)
                    }
                } else {
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
        val btcCreationFee = transactionAmountRepository.getMinBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH)
        val minRequiredBalance =
            transactionAmountRepository.getMinBalanceForRentExemption(MIN_REQUIRED_ACCOUNT_INFO_DATA_LENGTH)
        val solAmount = sol.total.toLamports(sol.decimals)
        val isAmountEnough = (solAmount - minRequiredBalance).isMoreThan(btcCreationFee)
        if (isAmountEnough) {
            val priceInSol = btcCreationFee.fromLamports(sol.decimals).scaleLong()
            val priceInUsd = priceInSol.toUsd(sol)
            view?.showBuy(priceInSol, priceInUsd)
        } else {
            view?.showTopup()
        }
    }

    private fun showCreateByFeeRelay() {
        view?.showCreateByFeeRelay()
    }
}
