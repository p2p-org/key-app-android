package org.p2p.wallet.restore.ui.seedphrase

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import java.io.File

interface SeedPhraseContract {

    interface View : MvpView {
        fun updateSeedPhrase(seedPhrase: List<SeedPhraseWord>)
        fun navigateToDerievableAccounts(seedPhrase: List<SeedPhraseWord>)
        fun showFile(file: File)
        fun showSeedPhraseValid(isSeedPhraseValid: Boolean)
        fun showClearButton(isVisible: Boolean)
        fun addFirstKey(seedPhraseWord: SeedPhraseWord)
        fun showFocusOnLastWord(lastSecretItemIndex: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun setNewSeedPhrase(seedPhrase: List<SeedPhraseWord>)
        fun verifySeedPhrase()
        fun load()
        fun requestFocusOnLastWord()
    }
}
