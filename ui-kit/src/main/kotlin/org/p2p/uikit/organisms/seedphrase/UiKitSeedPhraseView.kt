package org.p2p.uikit.organisms.seedphrase

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.children
import androidx.core.view.isVisible
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSeedPhraseViewBinding
import org.p2p.uikit.organisms.seedphrase.adapter.SecretPhraseAdapter
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseUtils
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.showSoftKeyboard

class UiKitSeedPhraseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var onSeedPhraseChanged: ((List<SecretKey>) -> Unit)? = null

    private val binding = inflateViewBinding<WidgetSeedPhraseViewBinding>()

    private val phraseAdapter: SecretPhraseAdapter by lazy {
        SecretPhraseAdapter { keys -> onSeedPhraseChanged?.invoke(keys) }
    }

    init {
        setBackgroundResource(R.drawable.bg_smoke_rounded)

        binding.keysRecyclerView.layoutManager = FlexboxLayoutManager(context).also {
            it.flexDirection = FlexDirection.ROW
            it.justifyContent = JustifyContent.FLEX_START
        }
        binding.keysRecyclerView.attachAdapter(phraseAdapter)

        binding.textViewClear.setOnClickListener { phraseAdapter.clear() }

        binding.textViewPaste.setOnClickListener {
            val keys = SeedPhraseUtils.format(getClipboardText())
            if (keys.isNotEmpty()) phraseAdapter.addAllSecretKeys(keys)
        }
    }

    fun updateSecretKeys(secretKeys: List<SecretKey>) {
        phraseAdapter.updateSecretKeys(secretKeys)
    }

    fun addSecretKey(secretKey: SecretKey) {
        phraseAdapter.addSecretKey(secretKey)
    }

    fun showFocusOnLastKey(lastSecretItemIndex: Int) {
        val viewGroup =
            binding.keysRecyclerView.children.toList().getOrNull(lastSecretItemIndex) as? LinearLayout ?: return
        val secretKeyEditText = viewGroup.children.firstOrNull { it.id == R.id.keyEditText }
        secretKeyEditText?.requestFocus()
        secretKeyEditText?.showSoftKeyboard()
    }

    fun showSeedPhraseValid(isValid: Boolean) {
        val textRes = if (isValid) R.string.seed_phrase_check else R.string.seed_phrase
        binding.textViewType.setText(textRes)
    }

    fun showClearButton(isVisible: Boolean) {
        binding.textViewClear.isVisible = isVisible
    }

    fun setPasteEnabled(isEnabled: Boolean) {
        binding.textViewPaste.isEnabled = isEnabled
    }

    fun setOnContainerClickListener(callback: () -> Unit) {
        binding.keysRecyclerView.setOnClickListener { callback() }
    }

    // Getting clipboard here since it's impossible to move `ContextExtensions` to ui-kit at the moment
    // TODO: Use Extensions for getting data from clipboard
    private fun getClipboardText(): String {
        val clipboard = context.getSystemService<ClipboardManager>()
        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
        return text?.trim().orEmpty()
    }
}
