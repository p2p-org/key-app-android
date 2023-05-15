package org.p2p.wallet.common.ui.bottomsheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.withImageOrGone
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogErrorBottomSheetBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class ErrorBottomSheet(
    private val actionCallback: (() -> Unit)?,
    private val dismissCallback: (() -> Unit)?
) : BottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_ICON = "EXTRA_ICON"
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_PRIMARY_BUTTON = "EXTRA_PRIMARY_BUTTON"
        private const val EXTRA_SECONDARY_BUTTON = "EXTRA_SECONDARY_BUTTON"

        fun show(
            activity: FragmentActivity,
            @DrawableRes iconRes: Int? = null,
            title: TextContainer,
            @StringRes primaryButtonRes: Int,
            @StringRes secondaryButtonRes: Int,
            actionCallback: (() -> Unit)? = null,
            dismissCallback: (() -> Unit)? = null
        ) {
            show(
                fragmentManager = activity.supportFragmentManager,
                iconRes = iconRes,
                title = title,
                primaryButtonRes = primaryButtonRes,
                secondaryButtonRes = secondaryButtonRes,
                actionCallback = actionCallback,
                dismissCallback = dismissCallback
            )
        }

        fun show(
            fragment: Fragment,
            @DrawableRes iconRes: Int? = null,
            title: TextContainer,
            @StringRes primaryButtonRes: Int,
            @StringRes secondaryButtonRes: Int,
            actionCallback: (() -> Unit)? = null,
            dismissCallback: (() -> Unit)? = null
        ) {
            show(
                fragmentManager = fragment.childFragmentManager,
                iconRes = iconRes,
                title = title,
                primaryButtonRes = primaryButtonRes,
                secondaryButtonRes = secondaryButtonRes,
                actionCallback = actionCallback,
                dismissCallback = dismissCallback
            )
        }

        fun show(
            fragmentManager: FragmentManager,
            @DrawableRes iconRes: Int? = null,
            title: TextContainer,
            @StringRes primaryButtonRes: Int,
            @StringRes secondaryButtonRes: Int,
            actionCallback: (() -> Unit)? = null,
            dismissCallback: (() -> Unit)? = null
        ) {
            val dialogFragment =
                fragmentManager.findFragmentByTag(ErrorBottomSheet::javaClass.name) as? ErrorBottomSheet
            dialogFragment?.dismissAllowingStateLoss()
            ErrorBottomSheet(actionCallback, dismissCallback)
                .withArgs(
                    EXTRA_ICON to iconRes,
                    EXTRA_TITLE to title,
                    EXTRA_PRIMARY_BUTTON to primaryButtonRes,
                    EXTRA_SECONDARY_BUTTON to secondaryButtonRes,
                )
                .show(fragmentManager, ErrorBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogErrorBottomSheetBinding by viewBinding()

    private val icon: Int? by args(EXTRA_ICON)
    private val title: TextContainer by args(EXTRA_TITLE)
    private val primaryButtonRes: Int by args(EXTRA_PRIMARY_BUTTON)
    private val secondaryButtonRes: Int by args(EXTRA_SECONDARY_BUTTON)

    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_error_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            textViewTitle.text = title.getString(requireContext())

            imageViewIcon.withImageOrGone(icon)
            buttonDone.setText(primaryButtonRes)
            buttonDone.isVisible = actionCallback != null
            buttonDone.setOnClickListener {
                actionCallback?.invoke()
                dismissAllowingStateLoss()
            }
            buttonCancel.setText(secondaryButtonRes)
            buttonCancel.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissCallback?.invoke()
        super.onDismiss(dialog)
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow
}
