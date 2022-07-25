package org.p2p.wallet.auth.ui.phone.countrypicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import org.p2p.wallet.databinding.DialogCountryPickerBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class CountryPickerDialog : DialogFragment() {

    private val binding: DialogCountryPickerBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
        }
    }
}
