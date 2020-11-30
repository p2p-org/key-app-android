package com.p2p.wowlet.fragment.backupwallat.secretkeys.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentSecretKeyBinding
import com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter.RandomKeyAdapter
import com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter.SortKeyAdapter
import com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecretKeyFragment : FragmentBaseMVVM<SecretKeyViewModel, FragmentSecretKeyBinding>() {
    override val viewModel: SecretKeyViewModel by viewModel()
    override val binding: FragmentSecretKeyBinding by dataBinding(R.layout.fragment_secret_key)

    private lateinit var sortAdapter: SortKeyAdapter
    private lateinit var randomAdapter: RandomKeyAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@SecretKeyFragment.viewModel
        }

    }

    override fun observes() {
        observe(viewModel.isCurrentCombination) {
            if (it) {
                viewModel.goToPinCodeFragment()
            } else {
                Toast.makeText(context, "Incorrect", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun initView() {
        with(binding) {
            /*  sortAdapter = SortKeyAdapter(this@SecretKeyFragment.viewModel, mutableListOf())
              rvSortSecretKey.adapter = sortAdapter
              rvSortSecretKey.layoutManager = GridLayoutManager(context, 3)*/
            /* randomAdapter = RandomKeyAdapter(this@SecretKeyFragment.viewModel, listOf())
             rvRandomSecretKey.adapter=randomAdapter
             rvRandomSecretKey.layoutManager = GridLayoutManager(context, 3)*/
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateCompleteBackupViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigatePinCodeViewCommand -> navigateFragment(
                command.destinationId,
                command.bundle
            )


        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }

}