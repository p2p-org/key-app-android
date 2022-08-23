package org.p2p.wallet.restore.ui.keys

import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.core.view.isVisible
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.hideKeyboard
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.uikit.utils.toast
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecretKeyBinding
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.restore.ui.keys.adapter.SecretPhraseAdapter
import org.p2p.wallet.restore.ui.keys.adapter.SeedPhraseUtils
import org.p2p.wallet.utils.getClipboardText
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber
import java.io.File

class SecretKeyFragment :
    BaseMvpFragment<SecretKeyContract.View, SecretKeyContract.Presenter>(R.layout.fragment_secret_key),
    SecretKeyContract.View {

    companion object {
        fun create(): SecretKeyFragment = SecretKeyFragment()
    }

    override val presenter: SecretKeyContract.Presenter by inject()
    private val binding: FragmentSecretKeyBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val phraseAdapter: SecretPhraseAdapter by unsafeLazy {
        SecretPhraseAdapter { keys -> presenter.setNewKeys(keys) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.IMPORT_MANUAL)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                it.hideKeyboard()
                popBackStack()
            }
            initKeysList()

            continueButton.setOnClickListener {
                presenter.verifySeedPhrase()
            }

            textViewClear.setOnClickListener {
                phraseAdapter.clear()
            }

            textViewPaste.setOnClickListener {
                val seedPhrase = requireContext().getClipboardText(trimmed = true).orEmpty()
                val keys = SeedPhraseUtils.format(seedPhrase)
                if (keys.isNotEmpty()) {
                    phraseAdapter.addAllSecretKeys(keys)
                }
            }

            keysRecyclerView.setOnClickListener {
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
        binding.textViewPaste.isEnabled = !clipboardData.isNullOrBlank()
    }

    private fun FragmentSecretKeyBinding.initKeysList() {
        keysRecyclerView.layoutManager = FlexboxLayoutManager(requireContext()).also {
            it.flexDirection = FlexDirection.ROW
            it.justifyContent = JustifyContent.FLEX_START
        }
        keysRecyclerView.attachAdapter(phraseAdapter)
        keysRecyclerView.isVisible = true
    }

    override fun updateKeys(secretKeys: List<SecretKey>) {
        phraseAdapter.updateSecretKeys(secretKeys)
    }

    override fun showSuccess(secretKeys: List<SecretKey>) {
        replaceFragment(DerivableAccountsFragment.create(secretKeys))
    }

    override fun showSeedPhraseValid(isValid: Boolean) {
        binding.continueButton.isEnabled = isValid

        val text = if (isValid) R.string.seed_phrase_check else R.string.seed_phrase
        binding.textViewType.text = getString(text)
    }

    override fun showClearButton(isVisible: Boolean) {
        binding.textViewClear.isVisible = isVisible
    }

    override fun addFirstKey(key: SecretKey) {
        phraseAdapter.addSecretKey(SecretKey())
    }

    override fun showError(messageRes: Int) = with(binding) {

    }

    override fun showFocusOnLastKey(lastSecretItemIndex: Int) {
        val viewGroup =
            binding.keysRecyclerView.children.toList().getOrNull(lastSecretItemIndex) as? LinearLayout ?: return
        val secretKeyEditText = viewGroup.children.firstOrNull { it.id == R.id.keyEditText }
        secretKeyEditText?.requestFocus()
        secretKeyEditText?.showSoftKeyboard()
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
