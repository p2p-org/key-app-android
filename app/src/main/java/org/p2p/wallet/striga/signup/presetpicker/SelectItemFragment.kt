package org.p2p.wallet.striga.signup.presetpicker

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.p2p.uikit.components.finance_block.baseCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.sectionHeaderCellDelegate
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSelectItemBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

interface SelectItemSearchEngine {
    suspend fun search(query: String, items: List<SelectableItem>): List<SelectableItem>
}

class SelectItemFragment :
    BaseMvpFragment<SelectItemContract.View, SelectItemContract.Presenter>(R.layout.fragment_select_item),
    SelectItemContract.View {

    companion object {
        private const val ARG_SELECT_ITEM_TYPE_ID = "ARG_SELECT_ITEM_TYPE_ID"
        private const val ARG_SELECT_ITEM_TYPE = "ARG_SELECT_ITEM_TYPE"

        fun create(
            type: SelectItemViewType
        ): SelectItemFragment =
            SelectItemFragment()
                .withArgs(
                    ARG_SELECT_ITEM_TYPE_ID to type.selectedItemId,
                    ARG_SELECT_ITEM_TYPE to type.providerType
                )
    }

    private val selectedItemId: String? by args(ARG_SELECT_ITEM_TYPE_ID)
    private val selectItemProviderType: SelectItemProviderType by args(ARG_SELECT_ITEM_TYPE)

    private val provider: SelectItemProvider by lazy {
        get(named(selectItemProviderType))
    }

    override val presenter: SelectItemContract.Presenter by lazy {
        get { parametersOf(provider, selectedItemId) }
    }

    private val binding: FragmentSelectItemBinding by viewBinding()

    private val adapter = CommonAnyCellAdapter(
        sectionHeaderCellDelegate(),
        baseCellDelegate(inflateListener = { financeBlock ->
            financeBlock.setOnClickAction { _, item -> presenter.onItemClicked(item.typedPayload()) }
        }),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setTitle(provider.provideItemsName().toolbarTitleRes)
            toolbar.setNavigationOnClickListener { popBackStack() }

            binding.recyclerViewItems.adapter = adapter

            provider.provideItemDecorations().forEach { binding.recyclerViewItems.addItemDecoration(it) }
//            binding.recyclerViewItems.addItemDecoration(
//                StrigaPresetPickItemDecorator(
//                    context = requireContext(),
//                    dividerDrawableRes = R.drawable.list_divider_smoke
//                )
//            )
        }
        if (provider.enableSearch()) {
            initSearch()
        }
    }

    private fun initSearch() = with(binding.searchView) {
        doAfterTextChanged { searchText -> presenter.search(searchText?.toString().orEmpty()) }
        setStateListener { presenter.search(emptyString()) }
        setHint(provider.provideItemsName().getSingularString(resources))
        setBgColor(R.color.bg_smoke)

        isVisible = true
    }

    override fun showItems(items: List<AnyCellItem>) {
        adapter.items = items
        binding.containerEmptyResult.root.isVisible = items.isEmpty()
    }

    override fun closeWithResult(selectedItem: SelectableItem) {
//        setFragmentResult(requestKey, bundleOf(resultKey to selectedItem))
//        popBackStack()
    }
}
