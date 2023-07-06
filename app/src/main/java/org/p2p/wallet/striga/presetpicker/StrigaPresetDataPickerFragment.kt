package org.p2p.wallet.striga.presetpicker

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.components.finance_block.baseCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.sectionHeaderCellDelegate
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaPresetDataPickerBinding
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

typealias ContractView = StrigaPresetDataPickerContract.View
typealias ContractPresenter = StrigaPresetDataPickerContract.Presenter

private const val EXTRA_SELECTED_ITEM = "EXTRA_SELECTED_ITEM"

class StrigaPresetDataPickerFragment :
    BaseMvpFragment<ContractView, ContractPresenter>(R.layout.fragment_striga_preset_data_picker),
    ContractView {

    companion object {
        private const val EXTRA_REQUEST_KEY = "REQUEST_KEY"
        private const val EXTRA_RESULT_KEY = "RESULT_KEY"

        fun create(
            requestKey: String,
            resultKey: String,
            dataToPick: StrigaPresetDataItem
        ): Fragment = StrigaPresetDataPickerFragment()
            .withArgs(
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey,
                EXTRA_SELECTED_ITEM to dataToPick
            )
    }

    override val presenter: ContractPresenter by inject { parametersOf(dataToPick) }
    private val binding: FragmentStrigaPresetDataPickerBinding by viewBinding()

    private val adapter = CommonAnyCellAdapter(
        sectionHeaderCellDelegate(),
        baseCellDelegate(inflateListener = { financeBlock ->
            financeBlock.setOnClickAction { _, item -> presenter.onPresetDataSelected(item.typedPayload()) }
        }),
    )

    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val dataToPick: StrigaPresetDataItem by args(EXTRA_SELECTED_ITEM)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setTitle(dataToPick.toolbarTitleId)
            toolbar.setNavigationOnClickListener { popBackStack() }

            binding.recyclerViewPresetData.adapter = adapter
            binding.recyclerViewPresetData.addItemDecoration(
                StrigaPresetPickItemDecorator(
                    context = requireContext(),
                    dividerDrawableRes = R.drawable.list_divider_smoke
                )
            )
        }
        if (dataToPick is StrigaPresetDataItem.Country) {
            initSearch()
        }
    }

    private fun initSearch() = with(binding.searchView) {
        doAfterTextChanged { searchText -> presenter.search(searchText?.toString().orEmpty()) }
        setStateListener { presenter.search(emptyString()) }
        setHint(dataToPick.searchTitleId)
        setBgColor(R.color.bg_smoke)

        isVisible = true
    }

    override fun showItems(items: List<AnyCellItem>) {
        adapter.items = items
        binding.containerEmptyResult.root.isVisible = items.isEmpty()
    }

    override fun closeWithResult(selectedItem: StrigaPresetDataItem) {
        setFragmentResult(requestKey, bundleOf(resultKey to selectedItem))
        popBackStack()
    }
}
