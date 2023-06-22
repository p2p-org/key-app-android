package org.p2p.wallet.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import org.p2p.uikit.utils.dip
import org.p2p.uikit.utils.findSuitableParent
import org.p2p.wallet.common.ui.widget.CustomSnackBar
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.databinding.WidgetBottomSheetSnackbarBinding

@Deprecated("use uikit snackbar")
fun Fragment.snackbar(action: (CustomSnackBar) -> Unit) {
    val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
    viewGroup.snackbar(action)
}
@Deprecated("use uikit snackbar")
fun AppCompatActivity.snackbar(action: (CustomSnackBar) -> Unit) {
    val viewGroup = findViewById<View>(android.R.id.content) as ViewGroup
    viewGroup.snackbar(action)
}

@Deprecated("use uikit snackbar")
fun ViewGroup.snackbar(action: (CustomSnackBar) -> Unit) {
    val parent = findSuitableParent()
    val binding =
        WidgetBottomSheetSnackbarBinding.inflate(
            LayoutInflater.from(context), parent, false
        )

    val lp = CoordinatorLayout.LayoutParams(
        CoordinatorLayout.LayoutParams.MATCH_PARENT,
        CoordinatorLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        bottomMargin = dip(24)
    }
    action.invoke(binding.snackbar)
    binding.snackbar.layoutParams = lp
    SnackBarView(this, binding.snackbar).apply { duration = Snackbar.LENGTH_LONG }.show()
}
