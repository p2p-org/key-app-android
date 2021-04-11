package com.p2p.wowlet.dashboard.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogEnterAppleIdPassBinding
import com.p2p.wowlet.utils.viewbinding.viewBinding
import kotlinx.android.synthetic.main.dialog_enter_apple_id_pass.vCancel
import kotlinx.android.synthetic.main.dialog_enter_apple_id_pass.vEnable

class EnterAppleIdPasswordDialog() : DialogFragment() {

    companion object {
        const val TAG_APPLE_ID_DIALOG = "EnterAppleIdPasswordDialog"
        fun newInstance(): EnterAppleIdPasswordDialog {
            return EnterAppleIdPasswordDialog()
        }
    }

    private val binding: DialogEnterAppleIdPassBinding by viewBinding()

    // Really?
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val binding: DialogEnterAppleIdPassBinding = DataBindingUtil.inflate(
//            inflater, R.layout.dialog_enter_apple_id_pass, container, false
//        )
//        return binding.root
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vEnable.setOnClickListener {
            dismiss()
        }
        vCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setLayout(
                resources.getDimensionPixelSize(R.dimen.dp_270),
                resources.getDimensionPixelSize(R.dimen.dp_178)
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}