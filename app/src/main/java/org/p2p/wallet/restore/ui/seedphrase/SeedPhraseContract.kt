package org.p2p.wallet.restore.ui.seedphrase

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.io.File

interface SeedPhraseContract {

    interface View : MvpView {
        fun updateSeedPhraseView(seedPhrase: List<SeedPhraseWord>)
        fun navigateToDerievableAccounts(seedPhrase: List<SeedPhraseWord>)
        fun showFile(file: File)
        fun showSeedPhraseValid(isSeedPhraseValid: Boolean)
        fun setClearButtonVisible(isVisible: Boolean)
        fun addFirstSeedPhraseWord()
        fun showFocusOnLastWord()
    }

    interface Presenter : MvpPresenter<View> {
        fun setNewSeedPhrase(seedPhrase: List<SeedPhraseWord>)
        fun verifySeedPhrase()
        fun requestFocusOnLastWord()
    }
}
