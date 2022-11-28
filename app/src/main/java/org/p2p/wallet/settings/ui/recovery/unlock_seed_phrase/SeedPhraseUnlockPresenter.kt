package org.p2p.wallet.settings.ui.recovery.unlock_seed_phrase

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter

private const val VIBRATE_DURATION = 500L

class SeedPhraseUnlockPresenter(
    private val authInteractor: AuthInteractor,
) : BasePresenter<SeedPhraseUnlockContract.View>(),
    SeedPhraseUnlockContract.Presenter
