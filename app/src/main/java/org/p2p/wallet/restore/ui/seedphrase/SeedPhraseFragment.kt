package org.p2p.wallet.restore.ui.seedphrase

import androidx.core.content.FileProvider
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.hideKeyboard
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.uikit.utils.toast
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSeedPhraseBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
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
            initToolbar()

            buttonContinue.setOnClickListener {
                presenter.verifySeedPhrase()
            }

            seedPhraseView.onSeedPhraseChanged = { keys ->
                presenter.setNewSeedPhrase(keys)
            }

            seedPhraseView.setOnContainerClickListener {
                presenter.requestFocusOnLastWord()
            }

            seedPhraseView.focusAndShowKeyboard()
        }

        presenter.load()
    }

    private fun FragmentSeedPhraseBinding.initToolbar() {
        with(toolbar) {
            setNavigationOnClickListener {
                it.hideKeyboard()
                popBackStack()
            }

            setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    hideKeyboard()
                    IntercomService.showMessenger()
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.requestFocusOnLastWord()
    }

    override fun updateSeedPhrase(seedPhrase: List<SeedPhraseWord>) {
        binding.seedPhraseView.updateSecretKeys(seedPhrase)
    }

    override fun navigateToDerievableAccounts(seedPhrase: List<SeedPhraseWord>) {
        replaceFragment(DerivableAccountsFragment.create(seedPhrase))
    }

    override fun showSeedPhraseValid(isSeedPhraseValid: Boolean) {
        renderButtonState(isButtonEnabled = isSeedPhraseValid)
        binding.seedPhraseView.showSeedPhraseValid(isSeedPhraseValid)
    }

    private fun renderButtonState(isButtonEnabled: Boolean) {
        with(binding.buttonContinue) {
            isEnabled = isButtonEnabled
            if (isButtonEnabled) {
                setText(R.string.common_continue)
                setTextColorRes(R.color.text_lime)
                setIconResource(R.drawable.ic_arrow_forward)
                setBackgroundColor(getColor(R.color.bg_night))
            } else {
                setText(R.string.seed_phrase_button_disabled_text)
                setTextColorRes(R.color.text_mountain)
                setBackgroundColor(getColor(R.color.bg_rain))
                icon = null
            }
        }
    }

    override fun showClearButton(isVisible: Boolean) {
        binding.seedPhraseView.showClearButton(isVisible)
    }

    override fun addFirstKey(seedPhraseWord: SeedPhraseWord) {
        binding.seedPhraseView.addSecretKey(SeedPhraseWord.EMPTY_WORD)
    }

    override fun showFocusOnLastWord(lastSecretItemIndex: Int) {
        binding.seedPhraseView.showFocusOnLastKey(lastSecretItemIndex)
    }

    override fun showFile(file: File) {
        val fromFile = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        val target = Intent(Intent.ACTION_VIEW)
            .setDataAndType(fromFile, "application/pdf")
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(target)
        } catch (e: ActivityNotFoundException) {
            Timber.i(file.toString())
            Timber.e(e, "Cannot open file")
            toast(R.string.error_opening_file)
        }
    }
}
