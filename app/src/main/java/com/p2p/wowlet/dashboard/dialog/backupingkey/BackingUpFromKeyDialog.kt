package com.p2p.wowlet.dashboard.dialog.backupingkey

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import kotlinx.android.synthetic.main.dialog_backing_up_keys.vClose
import kotlinx.android.synthetic.main.dialog_backing_up_keys.vDone
import org.koin.androidx.viewmodel.ext.android.viewModel

class BackingUpFromKeyDialog : DialogFragment() {
    private val secretKeyViewModel: SecretKeyViewModel by viewModel()

    companion object {

        const val TAG_BACKUP_UP_KEY_DIALOG = "BackingUpFromKeyDialog"
        fun newInstance(): BackingUpFromKeyDialog {
            return BackingUpFromKeyDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_backing_up_keys, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vClose.setOnClickListener {
            dismiss()
        }
        vDone.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}