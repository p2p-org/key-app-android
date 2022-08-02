package org.p2p.wallet.auth.ui.phone.countrypicker

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.inject
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.auth.ui.phone.model.CountryCodeAdapterItem
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogCountryPickerBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_KEY = "EXTRA_KEY"
private const val EXTRA_RESULT = "EXTRA_RESULT"
private const val EXTRA_SELECTED_COUNTRY = "EXTRA_SELECTED_COUNTRY"

class CountryPickerDialog :
    BaseMvpBottomSheet<CountryPickerContract.View, CountryPickerContract.Presenter>(R.layout.dialog_country_picker),
    CountryPickerContract.View,
    SearchView.OnQueryTextListener {

    override val presenter: CountryPickerContract.Presenter by inject()
    private val binding: DialogCountryPickerBinding by viewBinding()
    private val adapter = CountryPickerAdapter(::onItemClicked)

    companion object {
        fun create(
            selectedCountry: CountryCode?,
            requestKey: String,
            resultKey: String,
            fragmentManager: FragmentManager
        ) = CountryPickerDialog().withArgs(
            EXTRA_SELECTED_COUNTRY to selectedCountry,
            EXTRA_KEY to requestKey,
            EXTRA_RESULT to resultKey
        ).show(fragmentManager, CountryPickerDialog::javaClass.name)
    }

    private val selectedCountry: CountryCode? by args(EXTRA_SELECTED_COUNTRY)
    private val requestKey: String by args(EXTRA_KEY)
    private val resultKey: String by args(EXTRA_RESULT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            inflateSearchMenu(toolbar)
            recyclerView.adapter = adapter

            actionButton.setOnClickListener {
                presenter.onCountrySelected()
            }
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val layout = requireDialog().findViewById<CoordinatorLayout>(R.id.bottomSheetView)
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels

        presenter.load(selectedCountry)
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(searchText: String): Boolean {
        presenter.search(searchText)
        return true
    }

    private fun inflateSearchMenu(toolbar: UiKitToolbar) {
        toolbar.inflateMenu(R.menu.menu_search_country)

        val search = toolbar.menu.findItem(R.id.menu_search_country)
        val searchView = search.actionView as SearchView

        searchView.apply {
            onActionViewExpanded()
            setOnQueryTextListener(this@CountryPickerDialog)
        }
        searchView.showSoftKeyboard()
    }

    override fun showCountries(items: List<CountryCodeAdapterItem>) {
        adapter.setItems(items)
    }

    override fun setCountryCode(code: CountryCode) {
        setFragmentResult(requestKey, Bundle().apply { putParcelable(resultKey, code) })
        dismissAllowingStateLoss()
    }

    private fun onItemClicked(countryCode: CountryCodeAdapterItem) {
        presenter.onItemSelected(countryCode)
    }
}
