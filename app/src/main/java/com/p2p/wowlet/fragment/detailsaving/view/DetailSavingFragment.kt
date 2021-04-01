package com.p2p.wowlet.fragment.detailsaving.view

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentDetailSavingBinding
import com.p2p.wowlet.fragment.detailsaving.viewmodel.DetailSavingViewModel
import com.p2p.wowlet.utils.popBackStack
import kotlinx.android.synthetic.main.fragment_detail_saving.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class DetailSavingFragment :
    FragmentBaseMVVM<DetailSavingViewModel, FragmentDetailSavingBinding>() {

    override val viewModel: DetailSavingViewModel by viewModel()
    override val binding: FragmentDetailSavingBinding by dataBinding(R.layout.fragment_detail_saving)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@DetailSavingFragment.viewModel
        }
        context?.run {
            tabs.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tab)?.apply {
                background =
                    ContextCompat.getDrawable(this@run, R.drawable.bg_detail_selected_tab)
                text =
                    getString(R.string.market)
                setTypeface(typeface, Typeface.BOLD)
            }
            tabs.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tab)?.text =
                getString(R.string.activity)
            tabs.getTabAt(2)?.customView?.findViewById<TextView>(R.id.tab)?.text =
                getString(R.string.news)
            tabs.getTabAt(3)?.customView?.findViewById<TextView>(R.id.tab)?.text =
                getString(R.string.transactions)
        }

    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> {
                popBackStack()
            }
        }
    }
}