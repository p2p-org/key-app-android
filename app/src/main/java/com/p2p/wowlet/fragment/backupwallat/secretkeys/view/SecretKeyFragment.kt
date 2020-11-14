package com.p2p.wowlet.fragment.backupwallat.secretkeys.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentSecretKeyBinding
import com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter.RandomKeyAdapter
import com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter.SortKeyAdapter
import com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wowlet.fragment.reglogin.view.RegLoginFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayList

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
        observes()
    }

    private fun observes() {
        observe(viewModel.getSortSecretData) {
              sortAdapter.updateData(it)
        }
        observe(viewModel.getCurrentCombination) {
            if(it) {
                viewModel.goToCompleteWalletFragment()
            }else{
                Toast.makeText(context, "Incorrect", Toast.LENGTH_SHORT).show()
                viewModel.resetSelectData()
            }
        }
    }

    override fun initView() {
        with(binding) {
            sortAdapter = SortKeyAdapter(this@SecretKeyFragment.viewModel, mutableListOf())
            rvSortSecretKey.adapter = sortAdapter
            rvSortSecretKey.layoutManager = GridLayoutManager(context, 3)
            /* randomAdapter = RandomKeyAdapter(this@SecretKeyFragment.viewModel, listOf())
             rvRandomSecretKey.adapter=randomAdapter
             rvRandomSecretKey.layoutManager = GridLayoutManager(context, 3)*/
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateCompleteBackupViewCommand -> navigateFragment(command.destinationId)
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }

}