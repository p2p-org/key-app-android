package org.p2p.wallet.newsend.smartselection.strategy

import timber.log.Timber
import org.p2p.wallet.newsend.model.FeePayerState

/**
 * A simple class which executes the strategies in the order they are set.
 * Each strategy will return any type of [FeePayerState] which is passed to the UI after.
 * */
class StrategyExecutor {

    companion object {
        private const val TAG = "FeePayerStrategyExecutor"
    }

    private val strategies = linkedSetOf<FeePayerSelectionStrategy>()

    fun setStrategies(newStrategies: LinkedHashSet<FeePayerSelectionStrategy>) {
        strategies.clear()
        strategies += newStrategies
    }

    fun execute(): FeePayerState {
        if (strategies.isEmpty()) {
            Timber.tag(TAG).d("No strategies are set")
            return FeePayerState.NoStrategiesFound
        }

        val strategy = strategies.firstOrNull { it.isPayable() }
        return strategy?.execute() ?: FeePayerState.NoStrategiesFound
    }
}
