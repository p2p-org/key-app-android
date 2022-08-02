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
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding

class AddNumberFragment :
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
        childFragmentManager.setFragmentResultListener(REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            if (bundle.containsKey(RESULT_KEY)) {
                val selectedCountry = bundle.getParcelable<CountryCode>(RESULT_KEY) ?: return@setFragmentResultListener
                presenter.onCountryChanged(selectedCountry)
            }
        }
        presenter.load()
    }

    override fun showDefaultCountryCode(countryCode: CountryCode?) {
        if (countryCode != null) {
            binding.editText.setup(
                countryCode = countryCode,
                onCountryCodeChanged = ::onCountryCodeChanged,
                onPhoneChanged = ::onPhoneChanged,
                onCountryClickListener = ::onCountryClickListener
            )
        }
    }

    override fun update(countryCode: CountryCode) {
        binding.editText.update(countryCode)
    }

    override fun showNoCountry() {
        binding.editText.showNoCountry()
    }

    override fun onNewCountryDetected(countryCode: CountryCode) {
        binding.editText.onFoundNewCountry(countryCode)
    }

    override fun showCountryPicked(selectedCountryCode: CountryCode?) {
        CountryPickerDialog.create(selectedCountryCode, REQUEST_KEY, RESULT_KEY, childFragmentManager)
    }

    override fun showEnabled(isEnabled: Boolean) = with(binding) {
        if (isEnabled) {
            actionButton.setBackgroundColor(getColor(R.color.night))
            actionButton.setTextColor(getColor(R.color.lime))
            actionButton.setText(R.string.common_continue)
            actionButton.setIconResource(R.drawable.ic_arrow_forward)
            actionButton.isEnabled = true
        } else {

            actionButton.setBackgroundColor(getColor(R.color.rain))
            actionButton.setTextColor(getColor(R.color.mountain))
            actionButton.setText(R.string.auth_fill_your_number)
            actionButton.icon = null
            actionButton.isEnabled = false
        }
    }

    private fun onCountryCodeChanged(countryCode: String) {
        presenter.onCountryCodeChanged(countryCode)
    }

    private fun onPhoneChanged(phone: String) {
        presenter.onPhoneChanged(phone)
    }

    private fun onCountryClickListener() {
        presenter.onCountryClicked()
    }
}
