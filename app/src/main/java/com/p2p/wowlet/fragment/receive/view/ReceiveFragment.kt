package com.p2p.wowlet.fragment.receive.view

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentReceiveBinding
import com.p2p.wowlet.fragment.receive.viewmodel.ReceiveViewModel
import com.p2p.wowlet.utils.initChart
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class ReceiveFragment : FragmentBaseMVVM<ReceiveViewModel, FragmentReceiveBinding>() {

    override val viewModel: ReceiveViewModel by viewModel()
    override val binding: FragmentReceiveBinding by dataBinding(R.layout.fragment_receive)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@ReceiveFragment.viewModel
        }
        observes()

    }

    private fun observes() {
        observe(viewModel.getChartData) {
            binding.vChartData.initChart(it)
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(true)
            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }
}