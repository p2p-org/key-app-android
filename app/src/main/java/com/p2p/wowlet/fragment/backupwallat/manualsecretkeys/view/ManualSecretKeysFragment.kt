package com.p2p.wowlet.fragment.backupwallat.manualsecretkeys.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentManualSecretKeysBinding
import com.p2p.wowlet.fragment.backupwallat.manualsecretkeys.adapter.RandomKeyAdapter
import com.p2p.wowlet.fragment.backupwallat.manualsecretkeys.adapter.SortKeyAdapter
import com.p2p.wowlet.fragment.backupwallat.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replace
import com.wowlet.entities.enums.PinCodeFragmentType
import com.wowlet.entities.local.SecretKeyItem
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManualSecretKeysFragment :
    FragmentBaseMVVM<ManualSecretKeyViewModel, FragmentManualSecretKeysBinding>() {

    override val viewModel: ManualSecretKeyViewModel by viewModel()
    override val binding: FragmentManualSecretKeysBinding by dataBinding(R.layout.fragment_manual_secret_keys)

    private lateinit var sortAdapter: SortKeyAdapter
    private lateinit var randomAdapter: RandomKeyAdapter
    private val phraseList = mutableListOf<SecretKeyItem>()

    companion object {
        const val MANUAL_SECRET_KEY = "manual_secret_key"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@ManualSecretKeysFragment.viewModel
        }
    }

    override fun initData() {
        val secretKey = arguments?.run { getString(MANUAL_SECRET_KEY, "") }
        val split = secretKey?.split(" ")
        split?.forEach {
            val id = split.indexOf(it)
            phraseList.add(
                SecretKeyItem((id + 1), it, false)
            )
        }
    }

    override fun initView() {
        with(binding) {
            sortAdapter = SortKeyAdapter(this@ManualSecretKeysFragment.viewModel, mutableListOf())
            rvSortSecretKey.adapter = sortAdapter
            if (phraseList.isNotEmpty()) {
                phraseList.shuffle()
                randomAdapter =
                    RandomKeyAdapter(this@ManualSecretKeysFragment.viewModel, mutableListOf())
                rvRandomSecretKey.adapter = randomAdapter
                randomAdapter.updateData(phraseList)
            }
            resetSecretKeys.setOnClickListener {
                vUserId.visibility = View.GONE
                resetSecretKeys.visibility = View.GONE
                randomAdapter.updateData(phraseList)
                sortAdapter.removeAll()
            }
        }
    }

    override fun observes() {
        observe(viewModel.getPhraseData) {
            randomAdapter.hideItem(it)
            sortAdapter.addItem(it)
        }
        observe(viewModel.resultResponseData) {
            if (it) {
                replace(PinCodeFragment.create(
                    openSplashScreen = false,
                    isBackupDialog = false,
                    type = PinCodeFragmentType.CREATE
                ))
            } else {
                binding.vUserId.visibility = View.VISIBLE
                binding.resetSecretKeys.visibility = View.VISIBLE
            }
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> popBackStack()
            is Command.NavigatePinCodeViewCommand -> {
                replace(
                    PinCodeFragment.create(
                        openSplashScreen = false,
                        isBackupDialog = false,
                        type = PinCodeFragmentType.CREATE
                    )
                )
            }
        }
    }
}