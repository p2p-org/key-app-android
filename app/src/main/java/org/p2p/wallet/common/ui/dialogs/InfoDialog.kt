package org.p2p.wallet.common.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogInfoBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.showAllowingStateLoss
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val EXTRA_TITLE = "EXTRA_TITLE"
private const val EXTRA_SUBTITLE = "EXTRA_SUBTITLE"
private const val EXTRA_PRIMARY_BUTTON = "EXTRA_PRIMARY_BUTTON"
private const val EXTRA_SECONDARY_BUTTON = "EXTRA_SECONDARY_BUTTON"
private const val EXTRA_PRIMARY_BUTTON_COLOR = "EXTRA_PRIMARY_BUTTON_COLOR"
private const val EXTRA_CANCELABLE = "EXTRA_CANCELABLE"

class InfoDialog : DialogFragment() {

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            @StringRes titleRes: Int?,
            subTitle: String,
            @StringRes primaryButtonRes: Int,
            @StringRes secondaryButtonRes: Int?,
            @ColorRes primaryButtonTextColor: Int? = null,
            onPrimaryButtonClicked: () -> Unit,
            onSecondaryButtonClicked: () -> Unit,
            isCancelable: Boolean
        ) {
            InfoDialog()
                .withArgs(
                    EXTRA_TITLE to titleRes,
                    EXTRA_SUBTITLE to subTitle,
                    EXTRA_PRIMARY_BUTTON to primaryButtonRes,
                    EXTRA_SECONDARY_BUTTON to secondaryButtonRes,
                    EXTRA_PRIMARY_BUTTON_COLOR to primaryButtonTextColor,
                    EXTRA_CANCELABLE to isCancelable
                )
                .apply {
                    this.onPrimaryButtonClicked = onPrimaryButtonClicked
                    this.onSecondaryButtonClicked = onSecondaryButtonClicked
                }
                .showAllowingStateLoss(fragmentManager)
        }
    }

    private val titleRes: Int? by args(EXTRA_TITLE)
    private val subTitle: String by args(EXTRA_SUBTITLE)
    private val primaryButtonRes: Int by args(EXTRA_PRIMARY_BUTTON)
    private val secondaryButtonRes: Int? by args(EXTRA_SECONDARY_BUTTON)
    private val primaryButtonTextColor: Int? by args(EXTRA_PRIMARY_BUTTON_COLOR)
    private val isDialogCancelable: Boolean by args(EXTRA_CANCELABLE)

    var onPrimaryButtonClicked: (() -> Unit)? = null
    var onSecondaryButtonClicked: (() -> Unit)? = null

    private lateinit var binding: DialogInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.WalletTheme_Dialog_Wide)
        isCancelable = isDialogCancelable
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            titleTextView withTextOrGone titleRes?.let { getString(it) }
            subTitleTextView.setHtml(subTitle)
            primaryButton.setText(primaryButtonRes)

            secondaryButtonRes?.let {
                secondaryButton.isVisible = true
                secondaryButton.setText(it)
            }
            primaryButtonTextColor?.let {
                primaryButton.setTextColor(requireContext().getColor(it))
            }

            primaryButton.setOnClickListener {
                onPrimaryButtonClicked?.invoke()
                dismissAllowingStateLoss()
            }

            secondaryButton.setOnClickListener {
                onSecondaryButtonClicked?.invoke()
                dismissAllowingStateLoss()
            }
        }
    }

    private fun TextView.setHtml(html: String) {
        text = HtmlCompat.fromHtml(
            html,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }
}
