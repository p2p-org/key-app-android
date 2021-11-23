package org.p2p.wallet.main.ui.username

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogCancelConfirmationBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TITLE = "EXTRA_TITLE"
private const val EXTRA_SUBTITLE = "EXTRA_SUBTITLE"
private const val EXTRA_PRIMARY_BUTTON = "EXTRA_PRIMARY_BUTTON"
private const val EXTRA_SECONDARY_BUTTON = "EXTRA_SECONDARY_BUTTON"

class UsernameConfirmationDialog : DialogFragment() {

    companion object {
        fun show(
            fm: FragmentManager,
            @StringRes titleRes: Int,
            @StringRes subTitleRes: Int,
            @StringRes primaryButtonRes: Int,
            @StringRes secondaryButtonRes: Int,
            onPrimaryButtonClicked: () -> Unit,
            onSecondaryButtonClicked: () -> Unit,
        ) {
            UsernameConfirmationDialog()
                .withArgs(
                    EXTRA_TITLE to titleRes,
                    EXTRA_SUBTITLE to subTitleRes,
                    EXTRA_PRIMARY_BUTTON to primaryButtonRes,
                    EXTRA_SECONDARY_BUTTON to secondaryButtonRes,
                )
                .apply {
                    this.onPrimaryButtonClicked = onPrimaryButtonClicked
                    this.onSecondaryButtonClicked = onSecondaryButtonClicked
                }
                .show(fm, UsernameConfirmationDialog::javaClass.name)
        }
    }

    private val titleRes: Int by args(EXTRA_TITLE)
    private val subTitleRes: Int by args(EXTRA_SUBTITLE)
    private val primaryButtonRes: Int by args(EXTRA_PRIMARY_BUTTON)
    private val secondaryButtonRes: Int by args(EXTRA_SECONDARY_BUTTON)

    var onPrimaryButtonClicked: (() -> Unit)? = null
    var onSecondaryButtonClicked: (() -> Unit)? = null

    private lateinit var binding: DialogCancelConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.WalletTheme_Dialog_Wide)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogCancelConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            titleTextView.setText(titleRes)
            subTitleTextView.setText(subTitleRes)
            primaryButton.setText(primaryButtonRes)
            secondaryButton.setText(secondaryButtonRes)

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
}