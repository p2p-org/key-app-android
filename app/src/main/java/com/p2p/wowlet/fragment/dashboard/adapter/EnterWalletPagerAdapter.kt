package com.p2p.wowlet.fragment.dashboard.adapter

import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogEnterWalletBinding
import com.p2p.wowlet.databinding.ItemEnterWalletPagerBinding
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.inflate
import com.p2p.wowlet.utils.shareText
import com.wowlet.entities.local.EnterWallet

class EnterWalletPagerAdapter(private var list: List<EnterWallet>) :
    RecyclerView.Adapter<EnterWalletPagerAdapter.MyViewHolder>() {

    init {
        list = list.withFakeItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(parent.inflate(R.layout.item_enter_wallet_pager))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemEnterWalletPagerBinding.apply {
            itemModel = list[position]

            copyWallet.setOnClickListener {
                it.context.copyClipboard(list[position].walletAddress)
            }
            shareWallet.setOnClickListener {
                it.context.shareText(list[position].walletAddress)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    private fun List<EnterWallet>.withFakeItems() : List<EnterWallet> {
        val size: Int = this.size
        val mutableListOfEnterWallets = mutableListOf<EnterWallet>()
        for (i in 0..size + 2) {
            mutableListOfEnterWallets.add(this[(i + size - 2) % size])
        }
        return mutableListOfEnterWallets
    }

    fun getInfiniteScrollingOnPageChangeCallback(binding: DialogEnterWalletBinding) = object : ViewPager2.OnPageChangeCallback() {
        var currentPosition: Int = 0

        init {
            binding.viewPager.setCurrentItem(2, false)
            binding.enterWalletTitle.text = binding.enterWalletTitle.context.getString(R.string.deposit_to_your_wallet, list[2].name)
            if (list.size <= 4) {
                binding.viewPager.isUserInputEnabled = false
            }
        }


        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentPosition = position
            val context = binding.enterWalletTitle.context
            val enterWalletText = context.getString(R.string.deposit_to_your_wallet, list[position].name)
            binding.enterWalletTitle.text = enterWalletText
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                if (currentPosition == 0) {
                    binding.viewPager.setCurrentItem(list.size - 3, false)
                }else if (currentPosition == list.size - 1) {
                    binding.viewPager.setCurrentItem(2, false)
                }
            }else if (state == ViewPager2.SCROLL_STATE_DRAGGING
                && currentPosition == list.size - 1) {
                binding.viewPager.setCurrentItem(2, false)
            }
        }
    }

    inner class MyViewHolder(
        val itemEnterWalletPagerBinding: ItemEnterWalletPagerBinding
    ) : RecyclerView.ViewHolder(itemEnterWalletPagerBinding.root)

}