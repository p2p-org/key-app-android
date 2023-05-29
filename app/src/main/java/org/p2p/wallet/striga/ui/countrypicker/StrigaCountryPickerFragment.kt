package org.p2p.wallet.striga.ui.countrypicker

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaCountryPickerBinding
import org.p2p.wallet.striga.model.StrigaCountryPickerItem
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

typealias IView = StrigaCountryPickerContract.View
typealias IPresenter = StrigaCountryPickerContract.Presenter

private const val EXTRA_KEY = "EXTRA_KEY"
private const val EXTRA_RESULT = "EXTRA_RESULT"
private const val EXTRA_SELECTED_COUNTRY = "EXTRA_SELECTED_COUNTRY"

class StrigaCountryPickerFragment : BaseMvpFragment<IView, IPresenter>(R.layout.fragment_striga_country_picker), IView {

    companion object {
        fun create(
            selectedCountry: CountryCode?,
            requestKey: String,
            resultKey: String
        ) = StrigaCountryPickerFragment().withArgs(
            EXTRA_SELECTED_COUNTRY to selectedCountry,
            EXTRA_KEY to requestKey,
            EXTRA_RESULT to resultKey
        )
    }

    override val presenter: IPresenter by inject {
        parametersOf(selectedCountry)
    }
    private val binding: FragmentStrigaCountryPickerBinding by viewBinding()
    private val adapter: StrigaCountryPickerAdapter = StrigaCountryPickerAdapter(::onCountrySelected)
    private val selectedCountry: CountryCode? by args(EXTRA_SELECTED_COUNTRY)
    private val requestKey: String by args(EXTRA_KEY)
    private val resultKey: String by args(EXTRA_RESULT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                setFragmentResult(requestKey, bundleOf())
                popBackStack()
            }
        }
    }

    override fun showCountries(items: List<StrigaCountryPickerItem>) {
        adapter.setItems(items)
    }

    private fun onCountrySelected(selectedItem: StrigaCountryPickerItem.CountryItem) {
        setFragmentResult(requestKey, bundleOf(resultKey to selectedItem))
        popBackStack()
    }
}
