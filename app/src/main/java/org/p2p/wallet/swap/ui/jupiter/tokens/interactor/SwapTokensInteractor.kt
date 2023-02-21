package org.p2p.wallet.swap.ui.jupiter.tokens.interactor

import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.user.repository.UserLocalRepository

class SwapTokensInteractor(
    private val userLocalRepository: UserLocalRepository,
    private val jupiterSwapTokensRepository: JupiterSwapTokensRepository,
)
