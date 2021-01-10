package com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemAddCoinBinding
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.util.dpToPx
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.local.AddCoinItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class AddCoinAdapter(
    private val dashboardViewModel: DashboardViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<AddCoinAdapter.MyViewHolder>() {

    private val list: ArrayList<AddCoinItem> = ArrayList()

    private var previousExpandedItemPosition = -1
    private var expandedItemPosition = -1
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
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


    private fun onItemClick(item: AddCoinItem, position: Int, view: ViewGroup) =
        View.OnClickListener {
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

    inner class MyViewHolder(
        private val itemAddCoinBinding: ItemAddCoinBinding
    ) : RecyclerView.ViewHolder(itemAddCoinBinding.root) {
        fun bind(position: Int) {
            val item = list[position]
            itemAddCoinBinding.apply {
                itemAddCoin = item
                viewModel = dashboardViewModel
                containerMintAddress.isVisible = item.isShowMindAddress
                clItemAddCoin.setOnClickListener(onItemClick(item, position, clItemAddCoin))
                txtWillCost.text = txtWillCost.context.getString(R.string.add_coin_cost)
                if (item.isAlreadyAdded) {
                    pbAddCoin.progressDrawable = ContextCompat.getDrawable(pbAddCoin.context, R.drawable.bg_button_progress_bar_disabled)
                    lAddCoin.setOnClickListener {
                        println("debug: no click event")
                    }
                    txtWillCost.text = txtWillCost.context.getString(R.string.influenced_founds)
                }
                dashboardViewModel.progressData.observe(viewLifecycleOwner, {
                    if (it == 100) return@observe
                    pbAddCoin.progress = it
                    txtWillCost.isVisible = false
                    txtAddToken.text = txtAddToken.context.getString(R.string.adding_token_to_your_wallet)
                })


                dashboardViewModel.coinNoAddedError.observe(viewLifecycleOwner, {
                    if (it.isEmpty()) {
                        txtErrorMessage.text = it
                        return@observe
                    }else {
                        if (!item.isShowMindAddress) return@observe
                        txtErrorMessage.text = txtErrorMessage.context.getString(R.string.we_couldn_t_add_the_coin_error_message)
                        txtWillCost.text = txtWillCost.context.getString(R.string.add_coin_cost)
                        txtWillCost.isVisible = true
                    }
                })
            }

        }
    }
}