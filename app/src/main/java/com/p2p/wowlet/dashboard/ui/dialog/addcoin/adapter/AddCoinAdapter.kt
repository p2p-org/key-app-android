package com.p2p.wowlet.dashboard.ui.dialog.addcoin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.ui.dialog.addcoin.util.dpToPx
import com.p2p.wowlet.dashboard.ui.viewmodel.DashboardViewModel
import com.p2p.wowlet.databinding.ItemAddCoinBinding
import com.p2p.wowlet.common.network.Constants
import com.p2p.wowlet.dashboard.model.local.AddCoinItem
import com.p2p.wowlet.utils.bindadapter.imageSource

class AddCoinAdapter(
    context: Context,
    private val dashboardViewModel: DashboardViewModel,
    private val btnViewInExplorerClickEvent: (mintAddress: String) -> Unit
) : RecyclerView.Adapter<AddCoinAdapter.MyViewHolder>() {

    private val list: ArrayList<AddCoinItem> = ArrayList()
    private val layoutManager = LinearLayoutManager(context)

    private var previousExpandedItemPosition = -1
    private var expandedItemPosition = -1
    private var recyclerView: RecyclerView? = null

    private var callbacksEnabled = true

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView.also {
            it.layoutManager = layoutManager
        }
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind = ItemAddCoinBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(bind)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getAddCoinItemList() = list

    fun updateList(list: List<AddCoinItem>) {
        this.list.apply {
            clear()
            addAll(list.toMutableList().removeAddedTokens())
        }
        notifyDataSetChanged()
    }

    private fun MutableList<AddCoinItem>.removeAddedTokens(): MutableList<AddCoinItem> {
        val list = mutableListOf<AddCoinItem>()
        for (item in this) {
            if (item.isAlreadyAdded) {
                continue
            }
            list.add(item)
        }
        return list
    }

    fun disableCallbacks() {
        callbacksEnabled = false
    }

    fun enableCallbacks() {
        callbacksEnabled = true
    }

    fun getItemAddCoinBinding(mintAddress: String): ItemAddCoinBinding? {
        val position: Int? = getPositionByMintAddress(mintAddress)
        return getBindingByPosition(position)
    }

    fun getBindingByPosition(position: Int?): ItemAddCoinBinding? {
        position ?: return null
        return (recyclerView?.findViewHolderForAdapterPosition(position) as MyViewHolder).itemAddCoinBinding
    }

    fun getExpandedItemPosition(): Int? {
        for (i in 0 until list.size) {
            if (list[i].isShowMindAddress) {
                return i
            }
        }
        return null
    }

    private fun onItemClick(item: AddCoinItem, position: Int, view: ViewGroup) =
        View.OnClickListener {
            if (!callbacksEnabled) return@OnClickListener
            previousExpandedItemPosition = expandedItemPosition
            expandedItemPosition = if (item.isShowMindAddress) -1 else position
            item.isShowMindAddress = !item.isShowMindAddress
            if (previousExpandedItemPosition != -1) {
                list[previousExpandedItemPosition].isShowMindAddress = false
            }
            TransitionManager.beginDelayedTransition(view)
            (recyclerView?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                position,
                (100f).dpToPx().toInt()
            )
            notifyItemChanged(position)
            if (previousExpandedItemPosition == -1) return@OnClickListener
            notifyItemChanged(previousExpandedItemPosition)
        }

    private fun getPositionByMintAddress(mintAddress: String): Int? {
        for (item in list) {
            if (item.mintAddress == mintAddress) {
                return list.indexOf(item)
            }
        }
        return null
    }

    inner class MyViewHolder(
        val itemAddCoinBinding: ItemAddCoinBinding
    ) : RecyclerView.ViewHolder(itemAddCoinBinding.root) {

        fun bind(position: Int) {
            val item = list[position]
            itemAddCoinBinding.apply {

                val color = if (item.isShowMindAddress) R.color.gray_300 else R.color.white
                clItemAddCoin.setBackgroundColor(ContextCompat.getColor(itemView.context, color))
                txtErrorMessage.text = ""
                containerMintAddress.isVisible = item.isShowMindAddress
                clItemAddCoin.setOnClickListener(onItemClick(item, position, clItemAddCoin))
                btnViewInExplorer.setOnClickListener {
                    btnViewInExplorerClickEvent.invoke(Constants.EXPLORER_SOLANA_ADD_TOKEN + item.mintAddress)
                }
                txtWillCost.text = txtWillCost.context.getString(R.string.add_coin_cost)
                pbAddCoin.progressDrawable =
                    ContextCompat.getDrawable(pbAddCoin.context, R.drawable.bg_button_progress_bar)
                lAddCoin.isEnabled = true
                vName.text = item.tokenSymbol
                priceTextView.text = item.tokenName
                currencyTextView.text = itemView.context.getString(R.string.usd_symbol_2, item.currency)
                val result = if (item.isChange24hPercentagesPositive) {
                    itemView.context.getString(R.string.for_24h_positive, item.change24hPercentages)
                } else {
                    itemView.context.getString(R.string.for_24h, item.change24hPercentages)
                }
                percentTextView.text = result
                mintAddressInfo.text = itemView.context.getString(R.string.mint_address, item.tokenSymbol)
                txtMintAddress.text = item.mintAddress
                lAddCoin.setOnClickListener {
                    dashboardViewModel.addCoin(item)
                }
                txtWillCost.text = itemView.context.getString(R.string.add_coin_cost, item.minBalance)
                vCoin.imageSource(item.icon)
            }
        }
    }
}