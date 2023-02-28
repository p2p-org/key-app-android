package org.p2p.wallet.swap.ui.jupiter.settings

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapSettingsBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_STATE_MANAGE_KEY = "ARG_STATE_MANAGE_KEY"

class JupiterSwapSettingsFragment :
    BaseMvpFragment<
        JupiterSwapSettingsContract.View,
        JupiterSwapSettingsContract.Presenter>(R.layout.fragment_jupiter_swap_settings),
    JupiterSwapSettingsContract.View {

    companion object {
        fun create(stateManagerKey: String): JupiterSwapSettingsFragment =
            JupiterSwapSettingsFragment()
                .withArgs(
                    ARG_STATE_MANAGE_KEY to stateManagerKey,
                )
    }

    private val binding: FragmentJupiterSwapSettingsBinding by viewBinding()

    private val stateManagerKey: String by args(ARG_STATE_MANAGE_KEY)

    override val presenter: JupiterSwapSettingsContract.Presenter by inject { parametersOf(stateManagerKey) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }
}
