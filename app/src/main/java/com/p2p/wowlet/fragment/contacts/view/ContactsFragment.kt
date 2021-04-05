package com.p2p.wowlet.fragment.contacts.view

import android.os.Bundle
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentContactsBinding
import com.p2p.wowlet.utils.viewbinding.viewBinding

class ContactsFragment : BaseFragment(R.layout.fragment_contacts) {
    private val binding: FragmentContactsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}