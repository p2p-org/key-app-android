package org.p2p.wallet.home.ui.main

import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentRefreshErrorBinding
import org.p2p.wallet.home.interactor.RefreshErrorInteractor
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val FAKE_LOADING_TIME_MS = 3 * 1000L

class RefreshErrorFragment : BaseFragment(R.layout.fragment_refresh_error) {

    private val refreshErrorInteractor: RefreshErrorInteractor by inject()

    private val binding: FragmentRefreshErrorBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRefresh.setOnClickListener {
            with(binding) {
                buttonRefresh.isEnabled = false
                buttonRefresh.setLoading(true)
                lifecycleScope.launch {
                    delay(FAKE_LOADING_TIME_MS)
                    refreshErrorInteractor.notifyRefreshClicked()
                    buttonRefresh.isEnabled = true
                    buttonRefresh.setLoading(false)
                }
            }
        }
    }
}
