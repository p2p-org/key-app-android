package org.p2p.wallet.auth.ui.phone

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentAddNumberBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class AddNumberFragment() :
    BaseMvpFragment<AddNumberContract.View, AddNumberContract.Presenter>(R.layout.fragment_add_number) {

    companion object {
        fun create() = AddNumberFragment()
    }

    override val presenter: AddNumberContract.Presenter by inject()
    private val binding: FragmentAddNumberBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
    }
}
