package com.p2p.wallet.dashboard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogEnterWalletBinding
import com.p2p.wallet.databinding.ItemEnterWalletPagerBinding
import com.p2p.wallet.dashboard.model.local.EnterWallet
import com.p2p.wallet.utils.bindadapter.imageSource
import com.p2p.wallet.utils.bindadapter.imageSourceBitmap
import com.p2p.wallet.utils.bindadapter.walletFormat
import com.p2p.wallet.utils.copyClipboard
import com.p2p.wallet.utils.shareText

class EnterWalletPagerAdapter(private var list: List<EnterWallet>) :
    RecyclerView.Adapter<EnterWalletPagerAdapter.MyViewHolder>() {

    init {
        list = list.withFakeItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(
            ItemEnterWalletPagerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun List<EnterWallet>.withFakeItems(): List<EnterWallet> {
        val size: Int = this.size
        val mutableListOfEnterWallets = mutableListOf<EnterWallet>()
        for (i in 0..size + 2) {
            mutableListOfEnterWallets.add(this[(i + size - 2) % size])
        }
        return mutableListOfEnterWallets
    }

    fun getInfiniteScrollingOnPageChangeCallback(binding: DialogEnterWalletBinding) =
        object : ViewPager2.OnPageChangeCallback() {
            var currentPosition: Int = 0

            init {
                binding.viewPager.setCurrentItem(2, false)
                binding.enterWalletTitle.text =
                    binding.enterWalletTitle.context.getString(R.string.deposit_to_your_wallet, list[2].name)
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
                    } else if (currentPosition == list.size - 1) {
                        binding.viewPager.setCurrentItem(2, false)
                    }
                } else if (state == ViewPager2.SCROLL_STATE_DRAGGING &&
                    currentPosition == list.size - 1
                ) {
                    binding.viewPager.setCurrentItem(2, false)
                }
            }
        }

    inner class MyViewHolder(
        private val binding: ItemEnterWalletPagerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: EnterWallet) {
            with(binding) {
                walletQrCodeIv.imageSourceBitmap(item.qrCode)
                iconImageView.imageSource(item.icon)
                addressTextView.text = item.walletAddress
                copyWallet.walletFormat(item.walletAddress, 6)
                copyWallet.setOnClickListener {
                    it.context.copyClipboard(item.walletAddress)
                }
                shareWallet.setOnClickListener {
                    it.context.shareText(item.walletAddress)
                }
            }
        }
    }
}