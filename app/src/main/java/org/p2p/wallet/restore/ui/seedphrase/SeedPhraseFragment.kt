package org.p2p.wallet.restore.ui.seedphrase

import androidx.core.content.FileProvider
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey
import org.p2p.uikit.utils.hideKeyboard
import org.p2p.uikit.utils.toast
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSeedPhraseBinding
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.utils.getClipboardText
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber
import java.io.File

class SeedPhraseFragment :
    BaseMvpFragment<SeedPhraseContract.View, SeedPhraseContract.Presenter>(R.layout.fragment_seed_phrase),
    SeedPhraseContract.View {

    companion object {
        fun create(): SeedPhraseFragment = SeedPhraseFragment()
    }

    override val presenter: SeedPhraseContract.Presenter by inject()
    private val binding: FragmentSeedPhraseBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.IMPORT_MANUAL)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                it.hideKeyboard()
                popBackStack()
            }

            buttonContinue.setOnClickListener {
                presenter.verifySeedPhrase()
            }

            seedPhraseView.onSeedPhraseChanged = { keys ->
                presenter.setNewKeys(keys)
            }

            seedPhraseView.setOnContainerClickListener {
                presenter.requestFocusOnLastKey()
            }

            checkClipboard()
        }

        presenter.load()
    }

    override fun onResume() {
        super.onResume()
        presenter.requestFocusOnLastKey()
    }

    private fun checkClipboard() {
        val clipboardData = requireContext().getClipboardText()
        binding.seedPhraseView.setPasteEnabled(!clipboardData.isNullOrBlank())
    }

    override fun updateSeedPhrase(seedPhrase: List<SeedPhraseKey>) {
        binding.seedPhraseView.updateSecretKeys(seedPhrase)
    }

    override fun showSuccess(seedPhrase: List<SeedPhraseKey>) {
        replaceFragment(DerivableAccountsFragment.create(seedPhrase))
    }

    override fun showSeedPhraseValid(isValid: Boolean) {
        binding.buttonContinue.isEnabled = isValid
        binding.seedPhraseView.showSeedPhraseValid(isValid)
    }

    override fun showClearButton(isVisible: Boolean) {
        binding.seedPhraseView.showClearButton(isVisible)
    }

    override fun addFirstKey(key: SeedPhraseKey) {
        binding.seedPhraseView.addSecretKey(SeedPhraseKey())
    }

    override fun showFocusOnLastKey(lastSecretItemIndex: Int) {
        binding.seedPhraseView.showFocusOnLastKey(lastSecretItemIndex)
    }

    override fun showFile(file: File) {
        val fromFile = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(fromFile, "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(target)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Cannot open file")
            toast(R.string.error_opening_file)
        }
    }
}
