package org.p2p.wallet.solend.ui

import android.os.Bundle
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.EarnWidgetState
import org.p2p.wallet.databinding.FragmentSolendEarnBinding
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.math.BigDecimal

class SolendEarnFragment :
    BaseMvpFragment<SolendEarnContract.View, SolendEarnContract.Presenter>(R.layout.fragment_solend_earn),
    SolendEarnContract.View {

    companion object {
        fun create() = SolendEarnFragment()
    }

    override val presenter: SolendEarnContract.Presenter by inject()

    private val binding: FragmentSolendEarnBinding by viewBinding()

    private var timerJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            // Update after real integration
            viewEarnWidget.setWidgetState(EarnWidgetState.LearnMore)
            viewEarnWidget.setOnButtonClickListener {
                timerJob?.cancel()
                when (val state = viewEarnWidget.currentState) {
                    is EarnWidgetState.Balance -> {
                        viewEarnWidget.setWidgetState(
                            EarnWidgetState.Depositing(R.string.earn_widget_deposit_withdrawing)
                        )
                    }
                    is EarnWidgetState.Depositing -> {
                        if (state.buttonTextRes == R.string.earn_widget_deposit_withdrawing) {
                            viewEarnWidget.setWidgetState(EarnWidgetState.DepositFoundsFailed)
                        } else {
                            val timer = (1..Int.MAX_VALUE)
                                .asSequence()
                                .asFlow()
                                .onEach { delay(1_000) }

                            val job = Job()
                            val uiScope = CoroutineScope(Dispatchers.Main + job)
                            timerJob = uiScope.launch() {
                                var balance = 89.5708912487
                                timer.collect {
                                    balance += (it.toFloat() / 100000000000) // + Random.nextInt(1, 9)
                                    viewEarnWidget.setWidgetState(EarnWidgetState.Balance(BigDecimal(balance)))
                                }
                            }
                        }
                    }
                    EarnWidgetState.LearnMore -> {
                        viewEarnWidget.setWidgetState(
                            EarnWidgetState.Depositing(R.string.earn_widget_deposit_sending)
                        )
                    }
                    EarnWidgetState.DepositFoundsFailed -> {
                        viewEarnWidget.setWidgetState(
                            EarnWidgetState.Error(
                                R.string.earn_widget_error_message_withdrawal,
                                R.string.earn_widget_error_button_ok
                            )
                        )
                    }
                    is EarnWidgetState.Error -> {
                        viewEarnWidget.setWidgetState(EarnWidgetState.LearnMore)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        timerJob?.cancel()
        super.onDestroyView()
    }
}
