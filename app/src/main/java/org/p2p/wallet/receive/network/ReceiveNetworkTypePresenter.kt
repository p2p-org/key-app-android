package org.p2p.wallet.receive.network

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TransactionAmountInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toUsd
import timber.log.Timber
import java.math.BigInteger

class ReceiveNetworkTypePresenter(
    private val renBtcInteractor: RenBtcInteractor,
    private val userInteractor: UserInteractor,
    private val transactionAmountInteractor: TransactionAmountInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val receiveAnalytics: ReceiveAnalytics,
    networkType: NetworkType
) : BasePresenter<ReceiveNetworkTypeContract.View>(),
    ReceiveNetworkTypeContract.Presenter {

    private var selectedNetworkType: NetworkType = networkType

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
        if (isSelected) {
            // TODO implement topup
        } else {
            view?.close()
        }
    }

    override fun onBuySelected(isSelected: Boolean) {
        if (isSelected) {
            view?.navigateToReceive(selectedNetworkType)
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
                    onWalletExists()
                }
            } catch (e: Throwable) {
                Timber.e("Error on switch network: $e")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private suspend fun onWalletExists() {
        val session = renBtcInteractor.findActiveSession()
        if (session != null && session.isValid) {
            receiveAnalytics.logReceiveSettingBitcoin()
            view?.navigateToReceive(selectedNetworkType)
        } else {
            view?.showNetworkInfo(selectedNetworkType)
        }
    }

    private suspend fun createBtcWallet(sol: Token.Active) {
        val btcMinPrice = transactionAmountInteractor.getMinBalanceForRentExemption()
        val solAmount = sol.total.toLamports(sol.decimals)
        val isAmountEnough = (solAmount - btcMinPrice) >= BigInteger.ZERO
        if (isAmountEnough) {
            val priceInSol = btcMinPrice.fromLamports(sol.decimals).scaleMedium()
            val priceInUsd = priceInSol.toUsd(sol)
            view?.showBuy(priceInSol, priceInUsd)
        } else {
            view?.showTopup()
        }
    }
}