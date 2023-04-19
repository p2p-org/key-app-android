package org.p2p.wallet.restore.ui.derivable

import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTokenSimpleBinding
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val FULL_ALPHA = 1.0f
private const val HALF_ALPHA = 0.5f

private typealias OnAccountClick = (walletIndex: Int) -> Unit

class DerivableAccountsAdapter(
    private val onAccountClick: OnAccountClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<DerivableAccount>()

    fun setItems(new: List<DerivableAccount>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(parent, onAccountClick = onAccountClick)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBind(data[position])
    }

    private class ViewHolder(
        parent: ViewGroup,
        private val onAccountClick: OnAccountClick,
        binding: ItemTokenSimpleBinding = parent.inflateViewBinding(attachToRoot = false)
    ) : RecyclerView.ViewHolder(binding.root) {

        private val root = binding.root
        private val tokenImageView = binding.tokenImageView
        private val startAmountView = binding.startAmountView
        private val endAmountView = binding.endAmountView

        @SuppressLint("SetTextI18n")
        fun onBind(account: DerivableAccount) {
            val total = account.totalInUsd?.scaleShort()
            val tokenTotal = account.totalInSol

            startAmountView.title = SOL_SYMBOL
            tokenImageView.setImageResource(R.drawable.ic_solana_card)
            startAmountView.subtitle = cutAddress(account.account.publicKey.toBase58())
            endAmountView.topValue = total?.let { "$it $" }

            endAmountView.bottomValue = if (tokenTotal.isZero()) {
                null
            } else {
                "${tokenTotal.toPlainString()} $SOL_SYMBOL"
            }

            root.setOnClickListener { onAccountClick(bindingAdapterPosition) }
        }

        @Suppress("MagicNumber")
        fun cutAddress(publicKey: String): String {
            val firstSix = publicKey.take(4)
            val lastFour = publicKey.takeLast(4)
            return "$firstSix...$lastFour"
        }
    }
}
