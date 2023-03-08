package org.p2p.wallet.common.ui.bottomsheet

import androidx.annotation.DrawableRes
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
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"

        fun show(
            activity: FragmentActivity,
            @DrawableRes iconRes: Int,
            title: TextContainer,
            message: TextContainer,
            actionCallback: (() -> Unit)? = null,
            dismissCallback: (() -> Unit)? = null
        ) {
            show(activity.supportFragmentManager, iconRes, title, message, actionCallback, dismissCallback)
        }

        fun show(
            fragment: Fragment,
            @DrawableRes iconRes: Int,
            title: TextContainer,
            message: TextContainer,
            actionCallback: (() -> Unit)? = null,
            dismissCallback: (() -> Unit)? = null
        ) {
            show(fragment.childFragmentManager, iconRes, title, message, actionCallback, dismissCallback)
        }

        fun show(
            fragmentManager: FragmentManager,
            @DrawableRes iconRes: Int,
            title: TextContainer,
            message: TextContainer,
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
                    EXTRA_MESSAGE to message
                )
                .show(fragmentManager, ErrorBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogErrorBottomSheetBinding by viewBinding()

    private val icon: Int by args(EXTRA_ICON)
    private val title: TextContainer by args(EXTRA_TITLE)
    private val message: TextContainer by args(EXTRA_MESSAGE)

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
            titleTextView.text = title.getString(requireContext())
            messageTextView.text = message.getString(requireContext())

            iconImageView.setImageResource(icon)

            actionButton.clipToOutline = true
            actionButton.setOnClickListener {
                actionCallback?.invoke()
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
