package org.p2p.wallet.receive.network

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TransactionAmountInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toUsd
import java.math.BigInteger

class ReceiveNetworkTypePresenter(
    private val renBtcInteractor: RenBtcInteractor,
    private val userInteractor: UserInteractor,
    private val transactionAmountInteractor: TransactionAmountInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val networkType: NetworkType
) : BasePresenter<ReceiveNetworkTypeContract.View>(),
    ReceiveNetworkTypeContract.Presenter {

    override fun load() {
        view?.setCheckState(networkType)
    }

    override fun onNetworkChanged(type: NetworkType) {
        launch {
            when (type) {
                NetworkType.SOLANA -> {
                    view?.navigateToReceive(type)
                }
                NetworkType.BITCOIN -> {
                    onBitcoinSelected(type, ::onWalletExists, ::createBtcWallet)
                }
            }
        }
    }

    private fun onBitcoinSelected(
        type: NetworkType,
        onWalletExists: suspend (type: NetworkType) -> Unit,
        createWallet: suspend (Token.Active) -> Unit
    ) {
        launch {
            try {
                view?.showLoading(true)
                val userTokens = userInteractor.getUserTokens()
                val bitcoinWallet = userTokens.firstOrNull { it.isRenBTC }

                if (bitcoinWallet == null) {
                    val userPublicKey = tokenKeyProvider.publicKey
                    val sol = userTokens.find { it.isSOL && it.publicKey == userPublicKey }
                        ?: throw IllegalStateException("No SOL account found")
                    createWallet(sol)
                    return@launch
                }
                onWalletExists(type)
            } catch (e: Exception) {
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private suspend fun onWalletExists(type: NetworkType) {
        val session = renBtcInteractor.findActiveSession()
        if (session != null && session.isValid) {
            view?.navigateToReceive(type)
        } else {
            view?.showNetworkInfo(type)
        }
    }

    private suspend fun createBtcWallet(sol: Token.Active) {
        val btcMinPrice = transactionAmountInteractor.getMinBalanceForRentExemption()
        val solAmount = sol.total.toLamports(sol.decimals)
        val isAmountEnough = (solAmount - btcMinPrice) >= BigInteger.ZERO
        if (isAmountEnough) {
            val priceInSol = btcMinPrice.fromLamports().scaleMedium()
            val priceInUsd = priceInSol.toUsd(sol)
            view?.showBuy(priceInSol, priceInUsd)
        } else {
            view?.showTopup()
        }
    }
}