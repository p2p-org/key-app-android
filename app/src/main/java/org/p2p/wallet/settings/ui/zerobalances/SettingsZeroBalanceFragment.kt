package org.p2p.wallet.settings.ui.zerobalances

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsZeroBalancesBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SettingsZeroBalanceFragment :
    BaseMvpFragment<SettingsZeroBalanceContract.View, SettingsZeroBalanceContract.Presenter>(
        R.layout.fragment_settings_zero_balances
    ),
    SettingsZeroBalanceContract.View,
    RadioGroup.OnCheckedChangeListener {

    companion object {
        fun create() = SettingsZeroBalanceFragment()
    }

    override val presenter: SettingsZeroBalanceContract.Presenter by inject()
    private val binding: FragmentSettingsZeroBalancesBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            radioGroup.setOnCheckedChangeListener(this@SettingsZeroBalanceFragment)
            primaryButton.setOnClickListener {
                popBackStack()
            }
            secondaryButton.setOnClickListener {
                popBackStack()
            }
        }
    }

    override fun showZeroBalances(isHidden: Boolean) {
        val checkedId = if (isHidden) R.id.hiddenButton else R.id.shownButton
        binding.radioGroup.setOnCheckedChangeListener(null)
        binding.radioGroup.check(checkedId)
        binding.radioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        presenter.setZeroBalancesVisibility(isHidden = checkedId != R.id.shownButton)
    }
}