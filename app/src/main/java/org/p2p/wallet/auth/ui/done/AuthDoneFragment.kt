package org.p2p.wallet.auth.ui.done

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentAuthDoneBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class AuthDoneFragment :
    BaseMvpFragment<AuthDoneContract.View, AuthDoneContract.Presenter>(R.layout.fragment_auth_done),
    AuthDoneContract.View {

    companion object {
        fun create() = AuthDoneFragment()
    }

    override val presenter: AuthDoneContract.Presenter by inject()
    private val binding: FragmentAuthDoneBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.WELCOME_NEW)
        with(binding) {

            finishButton.clipToOutline = true
            finishButton.setOnClickListener {
                popAndReplaceFragment(MainFragment.create(), inclusive = true)
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
