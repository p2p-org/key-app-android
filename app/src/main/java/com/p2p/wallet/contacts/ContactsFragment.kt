package com.p2p.wallet.contacts

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentContactsBinding
import com.p2p.wallet.utils.viewbinding.viewBinding

class ContactsFragment : BaseFragment(R.layout.fragment_contacts) {
    private val binding: FragmentContactsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}