package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.wallet.history.strategy.context.AllTransactionParsingContext
import org.p2p.wallet.history.strategy.context.OrcaSwapParsingContext
import org.p2p.wallet.history.strategy.context.SerumSwapParsingContext
import org.p2p.wallet.history.strategy.context.SolanaParsingContext
import org.p2p.wallet.history.strategy.types.BurnCheckParsingStrategy
import org.p2p.wallet.history.strategy.types.CheckedTransferParsingStrategy
import org.p2p.wallet.history.strategy.types.CloseAccountParsingStrategy
import org.p2p.wallet.history.strategy.types.CreateAccountParsingStrategy
import org.p2p.wallet.history.strategy.types.TransferParsingStrategy
import org.p2p.wallet.history.strategy.types.UnknownParsingStrategy

object HistoryStrategyModule : InjectionModule {

    override fun create(): Module = module {

        factory(named("contexts")) {
            listOf(
                SerumSwapParsingContext(),
                OrcaSwapParsingContext(),
                SolanaParsingContext(get(named("strategies")))
            )
        }

        factory(named("strategies")) {
            listOf(
                BurnCheckParsingStrategy(),
                CreateAccountParsingStrategy(),
                CloseAccountParsingStrategy(),
                TransferParsingStrategy(get(), get()),
                UnknownParsingStrategy(),
                CheckedTransferParsingStrategy(get(), get())
            )
        }

        factory<TransactionParsingContext> {
            AllTransactionParsingContext(get(named("contexts")))
        }
    }
}
