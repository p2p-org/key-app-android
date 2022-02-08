package org.p2p.wallet.auth.ui.done

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentAuthDoneBinding
import org.p2p.wallet.home.HomeFragment
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class AuthDoneFragment() :
    BaseMvpFragment<AuthDoneContract.View, AuthDoneContract.Presenter>(R.layout.fragment_auth_done),
    AuthDoneContract.View {

    companion object {
        fun create() = AuthDoneFragment()
    }

    override val presenter: AuthDoneContract.Presenter by inject()
    private val binding: FragmentAuthDoneBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                finishButton.fitMargin { Edge.BottomArc }
            }

            finishButton.clipToOutline = true
            finishButton.setOnClickListener {
                popAndReplaceFragment(HomeFragment.create(), inclusive = true)
            }
        }

        presenter.load()
    }

    override fun showUsername(name: String?) {
        val title = if (name.isNullOrEmpty()) {
            getString(R.string.auth_welcome_to_p2p)
        } else {
            getString(R.string.auth_welcome_to_p2p_user, name)
        }
        binding.titleTextView.text = title
    }
}