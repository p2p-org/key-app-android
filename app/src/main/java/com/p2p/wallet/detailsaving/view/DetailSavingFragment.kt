package com.p2p.wallet.detailsaving.view

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentDetailSavingBinding
import com.p2p.wallet.detailsaving.adapter.ActivityDetailAdapter
import com.p2p.wallet.detailsaving.viewmodel.DetailSavingViewModel
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailSavingFragment : BaseFragment(R.layout.fragment_detail_saving) {

    private val viewModel: DetailSavingViewModel by viewModel()
    private val binding: FragmentDetailSavingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            vIcBack.setOnClickListener { popBackStack() }

            with(vRvActivity) {
                this.layoutManager = LinearLayoutManager(context)
            }

            viewModel.getActivityData.observe(viewLifecycleOwner) {
                binding.vRvActivity.adapter = ActivityDetailAdapter(viewModel, it)
            }

            binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<TextView>(R.id.tab)?.apply {
                        setTypeface(typeface, Typeface.BOLD)
                        background =
                            ContextCompat.getDrawable(context, R.drawable.bg_detail_selected_tab)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<TextView>(R.id.tab)?.apply {
                        background =
                            ContextCompat.getDrawable(context, R.drawable.bg_detail_unselected_tab)
                        setTypeface(typeface, Typeface.NORMAL)
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    tab?.customView?.findViewById<TextView>(R.id.tab)?.apply {
                        setTypeface(typeface, Typeface.BOLD)
                        background =
                            ContextCompat.getDrawable(context, R.drawable.bg_detail_selected_tab)
                    }
                }
            })
        }
        context?.run {
            binding.tabs.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tab)?.apply {
                background =
                    ContextCompat.getDrawable(this@run, R.drawable.bg_detail_selected_tab)
                text =
                    getString(R.string.market)
                setTypeface(typeface, Typeface.BOLD)
            }
            binding.tabs.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tab)?.text =
                getString(R.string.activity)
            binding.tabs.getTabAt(2)?.customView?.findViewById<TextView>(R.id.tab)?.text =
                getString(R.string.news)
            binding.tabs.getTabAt(3)?.customView?.findViewById<TextView>(R.id.tab)?.text =
                getString(R.string.transactions)
        }
    }
}