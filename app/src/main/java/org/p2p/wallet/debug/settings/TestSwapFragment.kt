package org.p2p.wallet.debug.settings

import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentTestSwapBinding
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.jupiter.domain.JupiterSwapInteractor
import org.p2p.wallet.swap.jupiter.repository.JupiterTokensRepository
import org.p2p.wallet.swap.jupiter.repository.SwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwap
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

class TestSwapFragment : BaseFragment(R.layout.fragment_test_swap) {

    private val binding: FragmentTestSwapBinding by viewBinding()

    private val interactor: JupiterSwapInteractor by inject()
    private val repository: SwapRoutesRepository by inject()
    private val jupiterRepository: JupiterTokensRepository by inject()
    private val tokenKeyProvider: TokenKeyProvider by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            val tokens = jupiterRepository.getTokens()
            binding.fromInput.setText(tokens.first { it.symbol == "USDT" }.address.base58Value)
            binding.fromInputLayout.helperText = "USDT"

            binding.toInput.setText(tokens.first { it.symbol == "SOL" }.address.base58Value)
            binding.toInputLayout.helperText = "SOL"

            binding.getRoute.setOnClickListener {
                val from = binding.fromInput.text?.toString() ?: ""
                val to = binding.toInput.text?.toString() ?: ""
                val amount = binding.amount.text?.toString() ?: ""

                lifecycleScope.launchWhenResumed {
                    val route = loadRoute(from, to, amount) ?: return@launchWhenResumed

                    binding.swap.isEnabled = true
                    binding.swap.setOnClickListener {
                        lifecycleScope.launchWhenResumed {
                            when (val result = interactor.swapTokens(route)) {
                                is JupiterSwapInteractor.JupiterSwapTokensResult.Success -> {
                                    showUiKitSnackBar(message = "success")
                                }
                                is JupiterSwapInteractor.JupiterSwapTokensResult.Failure -> {
                                    Timber.e(result)
                                    showUiKitSnackBar(message = "failure")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadRoute(
        from: String,
        to: String,
        amount: String
    ): SwapRoute? = try {
        repository.getSwapRoutes(
            jupiterSwap = JupiterSwap(
                inputMint = from.toBase58Instance(),
                outputMint = to.toBase58Instance(),
                amountInLamports = amount.toBigInteger(),
            ),
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        ).first()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
