package org.p2p.wallet.striga.ui

import kotlinx.coroutines.launch
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.user.interactor.UserInteractor

class TopUpWalletPresenter(
    private val userInteractor: UserInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<TopUpWalletContract.View>(),
    TopUpWalletContract.Presenter {

    private val isUserAuthByWeb3: Boolean
        get() = seedPhraseProvider.getUserSeedPhrase().provider == SeedPhraseSource.WEB_AUTH

    override fun attach(view: TopUpWalletContract.View) {
        super.attach(view)

        if (true) {
            view.showStrigaBankTransferView()
        } else {
            view.hideStrigaBankTransferView()
        }

        launch {
            val tokenToBuy = userInteractor.getSingleTokenForBuy()
            tokenToBuy?.let(view::showBankCardView) ?: view.hideBankCardView()
        }

        view.showCryptoReceiveView()
    }
}
