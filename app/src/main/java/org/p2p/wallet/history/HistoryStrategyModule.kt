package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.wallet.history.strategy.context.AllTransactionParsingContext
import org.p2p.wallet.history.strategy.context.OrcaSwapParsingContext
import org.p2p.wallet.history.strategy.context.FeeRelayerSwapParsingContext
import org.p2p.wallet.history.strategy.context.SerumSwapParsingContext
import org.p2p.wallet.history.strategy.context.SolanaParsingContext
import org.p2p.wallet.history.strategy.types.BurnCheckParsingStrategy
import org.p2p.wallet.history.strategy.types.CheckedTransferParsingStrategy
import org.p2p.wallet.history.strategy.types.CloseAccountParsingStrategy
import org.p2p.wallet.history.strategy.types.CreateAccountParsingStrategy
import org.p2p.wallet.history.strategy.types.TransferParsingStrategy
import org.p2p.wallet.history.strategy.types.UnknownParsingStrategy

object HistoryStrategyModule : InjectionModule {

    private const val STRATEGY_CONTEXTS_QUALIFIER = "STRATEGY_CONTEXTS_QUALIFIER"
    private const val STRATEGY_PARSERS_QUALIFIER = "STRATEGY_PARSERS_QUALIFIER"
    override fun create(): Module = module {

        factory(named(STRATEGY_CONTEXTS_QUALIFIER)) {
            listOf(
                SerumSwapParsingContext(),
                FeeRelayerSwapParsingContext(),
                OrcaSwapParsingContext(),
                SolanaParsingContext(get(named(STRATEGY_PARSERS_QUALIFIER)), get()),
            )
        }

        factory(named(STRATEGY_PARSERS_QUALIFIER)) {
            listOf(
                BurnCheckParsingStrategy(),
                CreateAccountParsingStrategy(),
                CloseAccountParsingStrategy(),
                TransferParsingStrategy(get(), get(), get()),
                UnknownParsingStrategy(),
                CheckedTransferParsingStrategy(get(), get(), get())
            )
        }

        factory<TransactionParsingContext> {
            AllTransactionParsingContext(get(named(STRATEGY_CONTEXTS_QUALIFIER)))
        }
    }
}
