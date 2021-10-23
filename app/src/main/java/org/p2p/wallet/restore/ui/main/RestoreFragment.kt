package org.p2p.wallet.restore.ui.main

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentRestoreBinding
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class RestoreFragment : BaseFragment(R.layout.fragment_restore) {

    companion object {
        fun create() = RestoreFragment()
    }

    private val binding: FragmentRestoreBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            edgeToEdge {
                backImageView.fitMargin { Edge.TopArc }
                restoreButton.fitMargin { Edge.BottomArc }
            }
            backImageView.setOnClickListener { popBackStack() }
            restoreButton.setOnClickListener {
                requireContext().copyToClipBoard(
                    listOf(
                        "oval",
                        "you",
                        "token",
                        "plug",
                        "copper",
                        "visa",
                        "employ",
                        "link",
                        "sell",
                        "asset",
                        "kick",
                        "sausage"
                    ).joinToString(" ")

//                    listOf(
//                        "miracle",
//                        "pizza",
//                        "supply",
//                        "useful",
//                        "steak",
//                        "border",
//                        "same",
//                        "again",
//                        "youth",
//                        "silver",
//                        "access",
//                        "hundred",
//                    ).joinToString(" ")
                )
                replaceFragment(SecretKeyFragment.create())
            }
        }
    }
}