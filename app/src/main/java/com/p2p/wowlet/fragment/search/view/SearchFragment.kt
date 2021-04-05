package com.p2p.wowlet.fragment.search.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentSearchBinding
import com.p2p.wowlet.fragment.search.viewmodel.SearchViewModel
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : BaseFragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()
    private val binding: FragmentSearchBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}