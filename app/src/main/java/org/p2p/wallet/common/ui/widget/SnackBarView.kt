package org.p2p.wallet.common.ui.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import org.p2p.wallet.databinding.WidgetBottomSheetSnackbarBinding
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.findSuitableParent
import timber.log.Timber

private const val TAG = "SnackBarView"

class SnackBarView(
    parent: ViewGroup,
    content: SimpleSnackBar
) : BaseTransientBottomBar<SnackBarView>(parent, content, content) {

    init {
        view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        view.setPadding(0, 0, 0, 0)
    }

    companion object {
        fun make(
            view: View,
            message: String,
            iconResId: Int = -1,
            duration: Int = Snackbar.LENGTH_SHORT,
            listener: View.OnClickListener? = null,
            actionTitle: String = "",
            actionImage: Int = -1,
        ): SnackBarView? {
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )
            try {
                val binding = WidgetBottomSheetSnackbarBinding.inflate(
                    LayoutInflater.from(view.context), parent, false
                )

                val lp = CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = view.context.dip(36)
                }
                binding.snackbar.layoutParams = lp

                with(binding) {
                    snackbar.setMessage(message)

                    if (iconResId != -1) {
                        snackbar.setIcon(iconResId)
                    }
                    if (actionTitle.isNotEmpty()) {
                        snackbar.setAction(actionTitle) { listener?.onClick(snackbar.getActionTextView()) }
                    }
                    if (actionImage != -1) {
                        snackbar.setAction(iconResId) { listener?.onClick(snackbar.getActionImageView()) }
                    }
                    return SnackBarView(parent, snackbar).setDuration(duration)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).d(e)
            }
            return null
        }
    }
}