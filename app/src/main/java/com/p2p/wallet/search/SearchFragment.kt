package com.p2p.wallet.search

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentSearchBinding
import com.p2p.wallet.utils.viewbinding.viewBinding

class SearchFragment : BaseFragment(R.layout.fragment_search) {

    private val binding: FragmentSearchBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}