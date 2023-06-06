package org.p2p.wallet.striga.presetpicker

import androidx.annotation.StringRes
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
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

typealias ContractView = StrigaPresetDataPickerContract.View
typealias ContractPresenter = StrigaPresetDataPickerContract.Presenter

private const val EXTRA_DATA_TO_PICK = "EXTRA_DATA_TO_PICK"

class StrigaPresetDataPickerFragment :
    BaseMvpFragment<ContractView, ContractPresenter>(R.layout.fragment_striga_preset_data_picker),
    ContractView {

    companion object {
        private const val EXTRA_REQUEST_KEY = "REQUEST_KEY"
        private const val EXTRA_RESULT_KEY = "RESULT_KEY"

        fun create(
            requestKey: String,
            resultKey: String,
            dataToPick: StrigaPresetDataToPick
        ): Fragment = StrigaPresetDataPickerFragment()
            .withArgs(
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey,
                EXTRA_DATA_TO_PICK to dataToPick
            )
    }

    override val presenter: ContractPresenter by inject { parametersOf(dataToPick) }
    private val binding: FragmentStrigaPresetDataPickerBinding by viewBinding()

    private val adapter = CommonAnyCellAdapter(
        sectionHeaderCellDelegate(),
        financeBlockCellDelegate(onItemClicked = { presenter.onPresetDataSelected(it.typedPayload()) }),
    )

    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val dataToPick: StrigaPresetDataToPick by args(EXTRA_DATA_TO_PICK)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setTitle(getToolbarTitle(dataToPick))
            toolbar.setNavigationOnClickListener { popBackStack() }

            binding.recyclerViewPresetData.adapter = adapter
            binding.recyclerViewPresetData.addItemDecoration(
                DividerItemDecorator(
                    context = requireContext(),
                    dividerDrawableRes = R.drawable.list_divider_smoke
                )
            )
        }
        initSearch()
    }

    private fun initSearch() = with(binding.searchView) {
        doAfterTextChanged { searchText -> presenter.search(searchText?.toString().orEmpty()) }
        setStateListener { presenter.search(emptyString()) }
        setHint(getSearchHint(dataToPick))
        setBgColor(R.color.bg_smoke)

        isVisible = true
    }

    override fun showItems(items: List<AnyCellItem>) {
        adapter.items = items
        binding.containerEmptyResult.root.isVisible = items.isEmpty()
    }

    @StringRes
    private fun getToolbarTitle(dataToPick: StrigaPresetDataToPick): Int = when (dataToPick) {
        StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY -> R.string.striga_select_your_country
        StrigaPresetDataToPick.COUNTRY_OF_BIRTH -> R.string.striga_select_your_country
        StrigaPresetDataToPick.SOURCE_OF_FUNDS -> R.string.striga_select_your_source
        StrigaPresetDataToPick.OCCUPATION -> R.string.striga_select_your_occupation
    }

    @StringRes
    private fun getSearchHint(dataToPick: StrigaPresetDataToPick): Int = when (dataToPick) {
        StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY -> R.string.striga_preset_data_hint_country
        StrigaPresetDataToPick.COUNTRY_OF_BIRTH -> R.string.striga_preset_data_hint_country
        StrigaPresetDataToPick.SOURCE_OF_FUNDS -> R.string.striga_preset_data_hint_source
        StrigaPresetDataToPick.OCCUPATION -> R.string.striga_preset_data_hint_occupation
    }

    override fun closeWithResult(selectedItem: StrigaPresetDataItem) {
        setFragmentResult(requestKey, bundleOf(resultKey to selectedItem))
        popBackStack()
    }
}
