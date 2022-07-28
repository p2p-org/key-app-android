package org.p2p.wallet.auth.ui.phone

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryPickerDialog
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentAddNumberBinding
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class AddNumberFragment() :
    BaseMvpFragment<AddNumberContract.View, AddNumberContract.Presenter>(R.layout.fragment_add_number),
    AddNumberContract.View {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val RESULT_KEY = "RESULT_KEY"
        fun create() = AddNumberFragment()
    }

    override val presenter: AddNumberContract.Presenter by inject()
    private val binding: FragmentAddNumberBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(REQUEST_KEY) { key, bundle ->
            if (bundle.containsKey(RESULT_KEY)) {
                val selectedCountry = bundle.getParcelable<CountryCode>(RESULT_KEY)
                showCountry(selectedCountry)
            }
        }
        presenter.load()
    }

    override fun showCountry(country: CountryCode?) {
        if (country == null) {
            showNoCountry()
        } else {
            binding.emojiTextView.text = country.flagEmoji
            binding.editText.setHint("+1(###)###-####")
            binding.editText.addPhoneWatcher(country)
        }
        binding.countryPickerView.setOnClickListener {
            replaceFragment(CountryPickerDialog.create(country, REQUEST_KEY, RESULT_KEY))
        }
    }

    private fun showNoCountry() {
    }
}
