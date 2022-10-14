package org.p2p.wallet.solana

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.rpc.model.RecentPerformanceSample
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.feature_toggles.toggles.remote.NetworkObservationFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NetworkObservationFrequencyFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NetworkObservationPercentFeatureToggle
import org.p2p.wallet.solana.model.NetworkStatusFrequency
import org.p2p.wallet.solana.model.SolanaNetworkState
import org.p2p.wallet.solana.model.SolanaNetworkState.Idle
import org.p2p.wallet.solana.model.SolanaNetworkState.Offline
import org.p2p.wallet.solana.model.SolanaNetworkState.Online
import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val SAMPLE_COUNT = 3
private const val TAG = "NETWORK_OBSERVER"
private const val REQUEST_REPEAT_TIME_IN_MS = 10000L

private const val KEY_MESSAGE_HIDDEN = "KEY_MESSAGE_HIDDEN"

class SolanaNetworkObserver(
    private val observationFeatureToggle: NetworkObservationFeatureToggle,
    private val percentFeatureToggle: NetworkObservationPercentFeatureToggle,
    private val errorFrequencyFeatureToggle: NetworkObservationFrequencyFeatureToggle,
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val sharedPreferences: SharedPreferences,
    private val appScope: AppScope
) {

    private val state = MutableStateFlow<SolanaNetworkState>(Idle)

    private fun updateState(newState: SolanaNetworkState) {
        Timber.tag(TAG).d("Updating the network state: $newState")
        this.state.value = newState
    }

    fun getStateFlow(): Flow<SolanaNetworkState> = state

    fun start() {
        if (!observationFeatureToggle.isFeatureEnabled) {
            Timber.tag(TAG).i("Solana network observation is disabled by feature toggle. Skipping the launch")
            return
        }

        appScope.launch {
            while (isActive) {
                try {
                    val samples = rpcSolanaRepository.getRecentPerformanceSamples(SAMPLE_COUNT)
                    handleSamples(samples)
                } catch (e: CancellationException) {
                    Timber.w("Fetching recent performance samples cancelled")
                } catch (e: Throwable) {
                    Timber.e(e, "Error loading recent samples")
                    showError()
                }

                /*
                * Checking the network state every 10 seconds
                * */
                delay(REQUEST_REPEAT_TIME_IN_MS)
            }
        }
    }

    private fun handleSamples(samples: List<RecentPerformanceSample>) {
        val transactions = samples.sumOf { it.numberOfTransactions }
        val periods = samples.sumOf { it.samplePeriodInSeconds }
        val currentAverageTps = transactions / periods
        Timber.tag(TAG).i("The average tps is: $currentAverageTps. Saving the value")

        when (val oldState = state.value) {
            is Idle,
            is Offline -> updateState(Online(currentAverageTps))
            is Online -> calculateNegativePercent(oldState, currentAverageTps)
        }
    }

    private fun calculateNegativePercent(
        oldState: Online,
        currentAverageTps: Int
    ) {
        val oldAverageTps = oldState.averageTps
        val minAllowedTpsInPercent = percentFeatureToggle.value

        /*
         * Converting percent in actual TPS (Transactions Per Second) value
         * */
        val minAllowedTps = oldAverageTps * minAllowedTpsInPercent / 100

        /**
         * If the TPS less than allowed then we are showing error snackbar
         * @example
         * 2900 -> OLD TPS
         * 1200 -> NEW TPS
         * 70 percent == 2030 TPS
         *
         * Then 2030 TPS at LEAST allowed. If the [currentAverageTps] is less than that value - showing error
         * */
        if (currentAverageTps < minAllowedTps) {
            showError()
        } else {
            updateState(Online(currentAverageTps))
        }
    }

    /*
    * Checking if user already saw the error message and we should show the message again or not
    * according to the frequency value from RemoteConfig
    * */
    private fun showError() {
        when (errorFrequencyFeatureToggle.frequency) {
            NetworkStatusFrequency.ONCE -> {
                val isMessageHidden = sharedPreferences.getBoolean(KEY_MESSAGE_HIDDEN, false)
                val state = if (isMessageHidden) Idle else Offline
                updateState(state)
            }
            NetworkStatusFrequency.MORE_THAN_ONCE -> {
                updateState(Offline)
            }
        }
    }

    fun setSnackbarHidden(isHidden: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_MESSAGE_HIDDEN, isHidden) }
    }
}
