package org.p2p.wallet.settings.ui.zerobalances

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsZeroBalancesBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class SettingsZeroBalanceFragment :
    BaseMvpFragment<SettingsZeroBalanceContract.View, SettingsZeroBalanceContract.Presenter>(
        R.layout.fragment_settings_zero_balances
    ),
    SettingsZeroBalanceContract.View,
    RadioGroup.OnCheckedChangeListener {

    companion object {
        fun create(requestKey: String, resultKey: String) = SettingsZeroBalanceFragment().withArgs(
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        )
    }

    override val presenter: SettingsZeroBalanceContract.Presenter by inject()
    private val binding: FragmentSettingsZeroBalancesBinding by viewBinding()
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            radioGroup.setOnCheckedChangeListener(this@SettingsZeroBalanceFragment)
            primaryButton.setOnClickListener {
                presenter.save()
            }
            secondaryButton.setOnClickListener {
                popBackStack()
            }
        }
    }

    override fun showZeroBalances(isVisible: Boolean) {
        val checkedId = if (isVisible) R.id.shownButton else R.id.hiddenButton
        binding.radioGroup.setOnCheckedChangeListener(null)
        binding.radioGroup.check(checkedId)
        binding.radioGroup.setOnCheckedChangeListener(this)
    }

    override fun close(isZeroBalanceVisible: Boolean) {
        setFragmentResult(requestKey, bundleOf(Pair(resultKey, isZeroBalanceVisible)))
        popBackStack()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        val isHidden = checkedId != R.id.shownButton
        presenter.setZeroBalancesVisibility(isHidden)
    }
}
