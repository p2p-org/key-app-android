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
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.adapter.DividerItemDecorator
import org.p2p.wallet.databinding.FragmentStrigaPresetDataPickerBinding
import org.p2p.wallet.striga.signup.model.StrigaPickerItem
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

typealias ContractView = StrigaPresetDataPickerContract.View
typealias ContractPresenter = StrigaPresetDataPickerContract.Presenter

private const val EXTRA_KEY = "EXTRA_KEY"
private const val EXTRA_RESULT = "EXTRA_RESULT"
private const val EXTRA_SELECTED_COUNTRY = "EXTRA_SELECTED_COUNTRY"

class StrigaPresetDataPickerFragment :
    BaseMvpFragment<ContractView, ContractPresenter>(R.layout.fragment_striga_preset_data_picker),
    ContractView {

    companion object {
        fun create(
            selectedCountry: StrigaPickerItem,
            requestKey: String,
            resultKey: String
        ): Fragment = StrigaPresetDataPickerFragment().withArgs(
            EXTRA_SELECTED_COUNTRY to selectedCountry,
            EXTRA_KEY to requestKey,
            EXTRA_RESULT to resultKey
        )
    }

    override val presenter: ContractPresenter by inject {
        parametersOf(selectedItem)
    }
    private val binding: FragmentStrigaPresetDataPickerBinding by viewBinding()

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
            binding.searchView.setBgColor(R.color.bg_smoke)
            binding.recyclerViewPresetData.adapter = adapter
            binding.recyclerViewPresetData.addItemDecoration(
                DividerItemDecorator(
                    context = requireContext(),
                    dividerDrawableRes = R.drawable.list_divider_smoke
                )
            )
        }
    }

    private fun onCountrySelected(selectedItem: Any?) {
        val selectedCountry = selectedItem as? StrigaPickerItem
        setFragmentResult(requestKey, bundleOf(resultKey to selectedCountry))
        popBackStack()
    }

    private fun initSearch() = with(binding.searchView) {
        doAfterTextChanged { searchText ->
            presenter.search(searchText?.toString().orEmpty())
        }

        setStateListener { presenter.search(emptyString()) }
        setTitle(R.string.striga_country)
        openSearch()
        isVisible = true
    }

    override fun showItems(items: List<AnyCellItem>) {
        adapter.items = items
        binding.containerEmptyResult.root.isVisible = items.isEmpty()
    }

    override fun updateSearchTitle(titleResId: Int) {
        binding.toolbar.setTitle(titleResId)
        binding.searchView.setTitle(titleResId)
    }

    override fun setupSearchBar() {
        initSearch()
    }
}
