package org.p2p.wallet.solend.ui.aboutearn

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.account.AccountStorage
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key.Companion.withCustomKey
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class SolendAboutEarnPresenter(
    private val tokenKeyProvider: TokenKeyProvider,
    private val accountStorage: AccountStorage
) : BasePresenter<SolendAboutEarnContract.View>(), SolendAboutEarnContract.Presenter {
    override fun onNextButtonClicked() {
        view?.slideNext()
    }

    override fun onContinueButtonClicked() {
        launch {
            accountStorage.saveString(
                AccountStorageContract.Key.KEY_SOLEND_ONBOARDING_COMPLETED.withCustomKey(
                    tokenKeyProvider.publicKey
                ),
                "+"
            )
            view?.closeOnboarding()
        }
    }
}
