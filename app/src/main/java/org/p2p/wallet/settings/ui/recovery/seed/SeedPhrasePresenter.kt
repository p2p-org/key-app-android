package org.p2p.wallet.settings.ui.recovery.seed

import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.security.SecureStorageContract

class SeedPhrasePresenter(
    private val secureStorage: SecureStorageContract,
    private val resourcesProvider: ResourcesProvider
) : BasePresenter<SeedPhraseContract.View>(),
    SeedPhraseContract.Presenter
