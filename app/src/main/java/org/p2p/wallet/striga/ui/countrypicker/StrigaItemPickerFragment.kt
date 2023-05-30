package org.p2p.wallet.striga.ui.countrypicker

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.components.finance_block.financeBlockCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.sectionHeaderCellDelegate
import org.p2p.wallet.R
import org.p2p.wallet.auth.widget.AnimatedSearchView
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.adapter.DividerItemDecorator
import org.p2p.wallet.databinding.FragmentStrigaCountryPickerBinding
import org.p2p.wallet.striga.signup.model.StrigaPickerItem
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

typealias IView = StrigaItemPickerContact.View
typealias IPresenter = StrigaItemPickerContact.Presenter

private const val EXTRA_KEY = "EXTRA_KEY"
private const val EXTRA_RESULT = "EXTRA_RESULT"
private const val EXTRA_SELECTED_COUNTRY = "EXTRA_SELECTED_COUNTRY"

class StrigaItemPickerFragment : BaseMvpFragment<IView, IPresenter>(R.layout.fragment_striga_country_picker), IView {

    companion object {
        fun create(
            selectedCountry: StrigaPickerItem,
            requestKey: String,
            resultKey: String
        ): Fragment = StrigaItemPickerFragment().withArgs(
            EXTRA_SELECTED_COUNTRY to selectedCountry,
            EXTRA_KEY to requestKey,
            EXTRA_RESULT to resultKey
        )
    }

    override val presenter: IPresenter by inject {
        parametersOf(selectedItem)
    }
    private val binding: FragmentStrigaCountryPickerBinding by viewBinding()
    private val adapter = CommonAnyCellAdapter(
        sectionHeaderCellDelegate(),
        financeBlockCellDelegate { view, _ -> view.setOnClickAction { _, item -> onCountrySelected(item.payload) } },
    )
    private val selectedItem: StrigaPickerItem by args(EXTRA_SELECTED_COUNTRY)
    private val requestKey: String by args(EXTRA_KEY)
    private val resultKey: String by args(EXTRA_RESULT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                onCountrySelected(selectedItem)
            }
            binding.searchView.setBgColor(getColor(R.color.bg_smoke))
            binding.recyclerViewCountries.adapter = adapter
            binding.recyclerViewCountries.addItemDecoration(
                DividerItemDecorator(
                    context = requireContext(),
                    dividerDrawableRes = R.drawable.list_divider_smoke
                )
            )
            initSearch()
        }
    }

    private fun onCountrySelected(selectedItem: Any?) {
        val selectedCountry = selectedItem as? StrigaPickerItem
        setFragmentResult(requestKey, bundleOf(resultKey to selectedCountry))
        popBackStack()
    }

    private fun FragmentStrigaCountryPickerBinding.initSearch() = with(searchView) {
        doAfterTextChanged { searchText ->
            presenter.search(searchText?.toString() ?: emptyString())
        }

        setStateListener(object : AnimatedSearchView.SearchStateListener {
            override fun onClosed() {
                presenter.search(emptyString())
            }
        })
        setTitle(R.string.striga_country)
        searchView.openSearch()
    }

    override fun showItems(items: List<AnyCellItem>) {
        adapter.items = items
        binding.containerErrorView.isVisible = items.isEmpty()
    }

    override fun updateSearchTitle(titleResId: Int) {
        binding.searchView.setTitle(titleResId)
    }
}
