package com.p2p.wallet.dialog.utils

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.p2p.wallet.R
import java.lang.Exception

fun Dialog.makeFullScreen(root: View, fragment: Fragment) {
    val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
    bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
    val behavior = BottomSheetBehavior.from<View>(bottomSheet!!)
    root.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                try {
                    root.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    behavior.peekHeight = root.height
                    fragment.view?.requestLayout()
                } catch (e: Exception) { }
            }
        })
}