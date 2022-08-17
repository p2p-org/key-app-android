package org.p2p.wallet.auth.ui.phone.countrypicker

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.CountryCodeItem
import org.p2p.wallet.auth.ui.phone.maskwatcher.CountryCodeTextWatcher
import org.p2p.wallet.auth.ui.phone.maskwatcher.PhoneNumberTextWatcher
import org.p2p.wallet.auth.widget.AnimatedSearchView
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogCountryPickerBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_KEY = "EXTRA_KEY"
private const val EXTRA_RESULT = "EXTRA_RESULT"
private const val EXTRA_SELECTED_COUNTRY = "EXTRA_SELECTED_COUNTRY"

class CountryCodePickerDialog :
    BaseMvpBottomSheet
    <CountryCodePickerContract.View, CountryCodePickerContract.Presenter>(R.layout.dialog_country_picker),
    CountryCodePickerContract.View {

    override val presenter: CountryCodePickerContract.Presenter by inject()
    private val binding: DialogCountryPickerBinding by viewBinding()
    private val adapter = CountryCodePickerAdapter(::onCountryCodeClicked)

    private lateinit var phoneTextWatcher: PhoneNumberTextWatcher
    private lateinit var countryCodeWatcher: CountryCodeTextWatcher

    companion object {
        fun show(
            selectedCountry: CountryCode?,
            requestKey: String,
            resultKey: String,
            fragmentManager: FragmentManager
        ) = CountryCodePickerDialog().withArgs(
            EXTRA_SELECTED_COUNTRY to selectedCountry,
            EXTRA_KEY to requestKey,
            EXTRA_RESULT to resultKey
        ).show(fragmentManager, CountryCodePickerDialog::javaClass.name)
    }

    private val selectedCountry: CountryCode? by args(EXTRA_SELECTED_COUNTRY)
    private val requestKey: String by args(EXTRA_KEY)
    private val resultKey: String by args(EXTRA_RESULT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                dismissAllowingStateLoss()
            }
            recyclerViewCountryCodes.adapter = adapter

            buttonActionContinue.setOnClickListener {
                presenter.onCountryCodeSelected()
            }
            searchView.doAfterTextChanged { searchText ->
                presenter.search(searchText?.toString() ?: emptyString())
            }
            searchView.setStateListener(object : AnimatedSearchView.SearchStateListener {
                override fun onClosed() {
                    presenter.search(emptyString())
                }
            })
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.searchView.isBackPressEnabled()) {
                dismissAllowingStateLoss()
            } else {
                binding.searchView.closeSearch()
            }
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val layout = requireDialog().findViewById<CoordinatorLayout>(R.id.bottomSheetView)
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels

        presenter.load(selectedCountry)
    }

    override fun showCountries(items: List<CountryCodeItem>) {
        adapter.setItems(items)
        binding.recyclerViewCountryCodes.isVisible = items.isNotEmpty()
        binding.textViewError.isVisible = items.isEmpty()
    }

    override fun setCountryCode(code: CountryCode) {
        setFragmentResult(requestKey, bundleOf(resultKey to code))
        dismissAllowingStateLoss()
    }

    private fun onCountryCodeClicked(countryCode: CountryCodeItem) {
        presenter.onItemSelected(countryCode)
    }
}
