package org.p2p.wallet.auth.ui.phone.countrypicker

import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.CountryCodeItem
import org.p2p.wallet.auth.widget.AnimatedSearchView
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentCountryPickerBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_KEY = "EXTRA_KEY"
private const val EXTRA_RESULT = "EXTRA_RESULT"
private const val EXTRA_SELECTED_COUNTRY = "EXTRA_SELECTED_COUNTRY"

class CountryCodePickerFragment :
    BaseMvpFragment<CountryCodePickerContract.View, CountryCodePickerContract.Presenter>(
        R.layout.fragment_country_picker
    ),
    CountryCodePickerContract.View {

    override val presenter: CountryCodePickerContract.Presenter by inject()
    private val binding: FragmentCountryPickerBinding by viewBinding()
    private val adapter = CountryCodePickerAdapter(::onCountryCodeClicked)

    companion object {
        fun create(
            selectedCountry: CountryCode?,
            requestKey: String,
            resultKey: String
        ): CountryCodePickerFragment =
            CountryCodePickerFragment()
                .withArgs(
                    EXTRA_SELECTED_COUNTRY to selectedCountry,
                    EXTRA_KEY to requestKey,
                    EXTRA_RESULT to resultKey
                )
    }

    private val selectedCountry: CountryCode? by args(EXTRA_SELECTED_COUNTRY)
    private val requestKey: String by args(EXTRA_KEY)
    private val resultKey: String by args(EXTRA_RESULT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { closeWithResult(selectedCode = null) }
            searchView.initSearch()
            recyclerViewCountryCodes.adapter = adapter
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            closeWithResult(selectedCode = null)
        }
        presenter.load(selectedCountry)
    }

    private fun AnimatedSearchView.initSearch() {
        doAfterTextChanged { searchText -> presenter.search(searchText?.toString().orEmpty()) }
        setStateListener { presenter.search(emptyString()) }
        openSearch()
        setBgColor(R.color.bg_snow)
    }

    override fun showCountries(items: List<CountryCodeItem>) {
        adapter.setItems(items)
        binding.recyclerViewCountryCodes.isVisible = items.isNotEmpty()
    }

    private fun onCountryCodeClicked(code: CountryCode) {
        closeWithResult(code)
    }

    private fun closeWithResult(selectedCode: CountryCode?) {
        val bundle = bundleOf().apply {
            selectedCode?.let { putParcelable(resultKey, it) }
        }
        setFragmentResult(requestKey, bundle)
        popBackStack(hideKeyboard = false)
    }
}
