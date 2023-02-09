package org.p2p.wallet.restore.ui.derivable

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTokenSimpleBinding
import org.p2p.wallet.restore.model.DerivableAccount

private const val FULL_ALPHA = 1.0f
private const val HALF_ALPHA = 0.5f

class DerivableAccountsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<DerivableAccount>()

    fun setItems(new: List<DerivableAccount>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBind(data[position])
    }

    private class ViewHolder(
        binding: ItemTokenSimpleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            binding = ItemTokenSimpleBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

        private val root = binding.root
        private val tokenImageView = binding.tokenImageView
        private val startAmountView = binding.startAmountView
        private val endAmountView = binding.endAmountView

        @SuppressLint("SetTextI18n")
        fun onBind(account: DerivableAccount) {
            val total = account.totalInUsd.scaleShort()
            val tokenTotal = account.total

            startAmountView.title = SOL_SYMBOL
            tokenImageView.setImageResource(R.drawable.ic_solana_card)
            startAmountView.subtitle = cutAddress(account.account.publicKey.toBase58())
            endAmountView.topValue = "$total $"

            endAmountView.bottomValue = if (tokenTotal.isZero()) {
                null
            } else {
                "${tokenTotal.toPlainString()} $SOL_SYMBOL"
            }

            root.alpha = if (total.isZero()) HALF_ALPHA else FULL_ALPHA
        }

        @Suppress("MagicNumber")
        fun cutAddress(publicKey: String): String {
            val firstSix = publicKey.take(4)
            val lastFour = publicKey.takeLast(4)
            return "$firstSix...$lastFour"
        }
    }
}
