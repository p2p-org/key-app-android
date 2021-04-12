package com.p2p.wallet.backupwallat.manualsecretkeys.view

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.pincode.view.PinCodeFragment
import com.p2p.wallet.backupwallat.manualsecretkeys.adapter.RandomKeyAdapter
import com.p2p.wallet.backupwallat.manualsecretkeys.adapter.SortKeyAdapter
import com.p2p.wallet.backupwallat.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentManualSecretKeysBinding
import com.p2p.wallet.auth.model.LaunchMode
import com.p2p.wallet.dashboard.model.local.SecretKeyItem
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManualSecretKeysFragment : BaseFragment(R.layout.fragment_manual_secret_keys) {

    private val viewModel: ManualSecretKeyViewModel by viewModel()
    private val binding: FragmentManualSecretKeysBinding by viewBinding()

    private lateinit var sortAdapter: SortKeyAdapter
    private lateinit var randomAdapter: RandomKeyAdapter
    private val phraseList = mutableListOf<SecretKeyItem>()

    companion object {
        const val MANUAL_SECRET_KEY = "manual_secret_key"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backImageView.setOnClickListener { popBackStack() }

        initData()
        initView()

        observeData()
    }

    private fun initData() {
        val secretKey = arguments?.run { getString(MANUAL_SECRET_KEY, "") }
        val split = secretKey?.split(" ")
        split?.forEach {
            val id = split.indexOf(it)
            phraseList.add(
                SecretKeyItem((id + 1), it, false)
            )
        }
    }

    private fun initView() {
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

    private fun observeData() {
        viewModel.getPhraseData.observe(viewLifecycleOwner) {
            randomAdapter.hideItem(it)
            sortAdapter.addItem(it)
        }
        viewModel.resultResponseData.observe(viewLifecycleOwner) {
            if (it) {
                replaceFragment(
                    PinCodeFragment.create(
                        openSplashScreen = false,
                        isBackupDialog = false,
                        type = LaunchMode.CREATE
                    )
                )
            } else {
                binding.vUserId.visibility = View.VISIBLE
                binding.resetSecretKeys.visibility = View.VISIBLE
            }
        }
    }
}