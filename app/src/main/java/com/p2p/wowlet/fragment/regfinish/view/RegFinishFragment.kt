package com.p2p.wowlet.fragment.regfinish.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentRegFinishBinding
import com.p2p.wowlet.fragment.regfinish.viewmodel.RegFinishViewModel
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegFinishFragment : BaseFragment(R.layout.fragment_reg_finish) {

    private val viewModel: RegFinishViewModel by viewModel()
    private val binding: FragmentRegFinishBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            btFinish.setOnClickListener {
                val context = requireActivity()
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                context.finish()
            }
        }
    }
}