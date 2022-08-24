package org.p2p.wallet.restore.ui.seedphrase

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey
import java.io.File

interface SeedPhraseContract {

    interface View : MvpView {
        fun updateSeedPhrase(seedPhrase: List<SeedPhraseKey>)
        fun showSuccess(seedPhrase: List<SeedPhraseKey>)
        fun showFile(file: File)
        fun showSeedPhraseValid(isValid: Boolean)
        fun showClearButton(isVisible: Boolean)
        fun addFirstKey(key: SeedPhraseKey)
        fun showFocusOnLastKey(lastSecretItemIndex: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun setNewKeys(keys: List<SeedPhraseKey>)
        fun verifySeedPhrase()
        fun load()
        fun requestFocusOnLastKey()
    }
}
