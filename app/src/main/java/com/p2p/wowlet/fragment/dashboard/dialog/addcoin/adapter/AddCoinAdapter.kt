package com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemAddCoinBinding
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.util.dpToPx
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.Constants
import com.wowlet.entities.local.AddCoinItem

class AddCoinAdapter(
    context: Context,
    private val dashboardViewModel: DashboardViewModel,
    private val btnViewInExplorerClickEvent: (mintAddress: String) -> Unit
) : RecyclerView.Adapter<AddCoinAdapter.MyViewHolder>() {

    private val list: ArrayList<AddCoinItem> = ArrayList()
    private val noScrollLinearLayoutManager = NoScrollLinearLayoutManager(context)

    private var previousExpandedItemPosition = -1
    private var expandedItemPosition = -1
    private var recyclerView: RecyclerView? = null

    private var callbacksEnabled = true


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView.also {
            it.layoutManager = noScrollLinearLayoutManager
        }
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemAddCoinBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_add_coin,
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

    fun updateList(list: List<AddCoinItem>) {
        this.list.apply {
            clear()
            addAll(list)
        }
        notifyDataSetChanged()
    }


    fun disableCallbacks() {
        callbacksEnabled = false
        getLayoutManager().disableScrolling()
    }

    fun enableCallbacks() {
        callbacksEnabled = true
        getLayoutManager().enableScrolling()
    }


    fun getItemAddCoinBinding(mintAddress: String): ItemAddCoinBinding? {
        val position: Int? = getPositionByMintAddress(mintAddress)
        return getBindingByPosition(position)
    }

    fun getBindingByPosition(position: Int?): ItemAddCoinBinding? {
        position ?: return null
        return (recyclerView?.findViewHolderForAdapterPosition(position) as MyViewHolder).itemAddCoinBinding
    }

    fun getExpandedItemPosition() : Int? {
        for (i in 0 until list.size) {
            if (list[i].isShowMindAddress) {
                return i
            }
        }
        return null
    }

    private fun getLayoutManager() : NoScrollLinearLayoutManager {
        return recyclerView?.layoutManager as NoScrollLinearLayoutManager
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
                itemAddCoin = item
                viewModel = dashboardViewModel
                containerMintAddress.isVisible = item.isShowMindAddress
                clItemAddCoin.setOnClickListener(onItemClick(item, position, clItemAddCoin))
                btnViewInExplorer.setOnClickListener {
                    btnViewInExplorerClickEvent.invoke(Constants.EXPLORER_SOLANA_ADD_TOKEN + item.mintAddress)
                }
                txtWillCost.text = txtWillCost.context.getString(R.string.add_coin_cost)
                if (item.isAlreadyAdded) {
                    pbAddCoin.progressDrawable = ContextCompat.getDrawable(pbAddCoin.context, R.drawable.bg_button_progress_bar_disabled)
                    lAddCoin.isEnabled = false
                    txtWillCost.text = txtWillCost.context.getString(R.string.influenced_founds)
                }

            }

        }
    }
}