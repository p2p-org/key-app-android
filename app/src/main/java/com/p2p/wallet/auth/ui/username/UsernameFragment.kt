package com.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.utils.edgetoedge.Edge
import com.p2p.wallet.utils.edgetoedge.edgeToEdge
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

import com.p2p.wallet.databinding.FragmentUsernameBinding

class UsernameFragment :
    BaseMvpFragment<UsernameContract.View,
        UsernameContract.Presenter>(R.layout.fragment_username),
    UsernameContract.View {

    companion object {
        fun create() = UsernameFragment()
    }

    override val presenter: UsernameContract.Presenter by inject()

    private val binding: FragmentUsernameBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
        }

        presenter.loadData()
    }

    override fun showName(name: String?) {
        binding.nameTextView.text = name
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    override fun showAddress(address: String?) {
        binding.addressTextView.text = address
    }
}