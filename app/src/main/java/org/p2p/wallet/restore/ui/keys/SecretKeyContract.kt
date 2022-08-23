package org.p2p.wallet.restore.ui.keys

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.restore.model.SecretKey
import java.io.File

interface SecretKeyContract {

    interface View : MvpView {
        fun updateKeys(secretKeys: List<SecretKey>)
        fun showSuccess(secretKeys: List<SecretKey>)
        fun showError(@StringRes messageRes: Int)
        fun showFile(file: File)
        fun showSeedPhraseValid(isValid: Boolean)
        fun showClearButton(isVisible: Boolean)
        fun addFirstKey(key: SecretKey)
        fun showFocusOnLastKey(lastSecretItemIndex: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun setNewKeys(keys: List<SecretKey>)
        fun verifySeedPhrase()
        fun load()
        fun requestFocusOnLastKey()
    }
}
