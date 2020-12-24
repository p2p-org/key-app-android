package com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter

import android.content.Context
import android.text.Selection
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.RvItemKeyWordBinding
import com.wowlet.entities.local.Keyword

class SecretPhraseAdapter(private val context: Context, private val phrase: MutableLiveData<String>) : RecyclerView.Adapter<SecretPhraseAdapter.ViewHolder>() {


    companion object {
        const val PHRASE_SIZE: Int = 12
    }

    private var recyclerView: RecyclerView? = null
    private val keywordList: ArrayList<Keyword> = ArrayList(PHRASE_SIZE)
    private val layoutManager: MultipleSpanGridLayoutManager = MultipleSpanGridLayoutManager(context)

    init {

    }



    fun clear() {
        keywordList.clear()
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean {
        return keywordList.isEmpty()
    }

    fun addItem(keyword: Keyword) {
        if (keywordList.size >= PHRASE_SIZE) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
            return
        }
        keywordList.add(keyword)
        notifyItemInserted(keywordList.size - 1)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView.apply {
            layoutManager = this@SecretPhraseAdapter.layoutManager
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.rv_item_key_word,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return keywordList.size
    }

    inner class ViewHolder(val binding: RvItemKeyWordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {


            val textNum = "${position + 1}."
            binding.txtKeywordNum.text = textNum

            binding.edtKeyword.requestFocus()
            binding.edtKeyword
            if (position == 0) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
            }


            val textWatcher = KeywordEditTextChangeListener(
                recyclerView, binding.edtKeyword, binding.txtKeywordNum, binding.root, keywordList, position, phrase
            )
            binding.edtKeyword.addTextChangedListener(textWatcher)
            binding.edtKeyword.onFocusChangeListener = OnFocusChangeListener(position)

        }
    }
}