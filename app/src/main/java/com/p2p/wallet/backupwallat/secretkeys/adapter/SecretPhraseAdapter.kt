package com.p2p.wallet.backupwallat.secretkeys.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.backupwallat.secretkeys.utils.KeywordEditOnKeyListener
import com.p2p.wallet.backupwallat.secretkeys.utils.MeasureHelper
import com.p2p.wallet.backupwallat.secretkeys.utils.OnFocusChangeListener
import com.p2p.wallet.backupwallat.secretkeys.utils.hideSoftKeyboard
import com.p2p.wallet.backupwallat.secretkeys.utils.showSoftKeyboard
import com.p2p.wallet.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wallet.databinding.RvItemKeyWordBinding
import com.p2p.wallet.dashboard.model.local.Keyword

class SecretPhraseAdapter(
    private val context: Context,
    private val viewModel: SecretKeyViewModel
) : RecyclerView.Adapter<SecretPhraseAdapter.ViewHolder>() {

    companion object {
        const val PHRASE_SIZE: Int = 12
    }

    private var recyclerView: RecyclerView? = null
    private val keywordList: ArrayList<Keyword> = ArrayList(PHRASE_SIZE)
    private val layoutManager: MultipleSpanGridLayoutManager = MultipleSpanGridLayoutManager(context)
//    private val textWatcher = KeywordEditTextChangeListener(this, keywordList, viewModel)
    private val onFocusChangeListener = OnFocusChangeListener()
    private val onKeyListener = KeywordEditOnKeyListener(this)

    fun clear() {
        var count = 0
        recyclerView?.children?.forEach { root ->
            val parent = root as ViewGroup
            val textView = parent.getChildAt(0) as AppCompatTextView
            val editText = parent.getChildAt(1) as AppCompatEditText
            editText.apply {
//                removeTextChangedListener(textWatcher)
                visibility = View.VISIBLE
                setText("")
            }
            textView.setTextColor(ContextCompat.getColor(context, R.color.hintColor))
            layoutManager.spanSizes[count++] = MultipleSpanGridLayoutManager.SPAN_SIZE
        }
        keywordList.clear()
        notifyDataSetChanged()

//        ????????????????????????????????????????????????????
//        ????????????????????????????????????????????????????
//        ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
//        ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
        // Keyboard is not appearing after Resetting the phrase
        // without this additional operation (idk y)
        recyclerView?.children?.forEach { root ->
            ((root as ViewGroup).getChildAt(1) as AppCompatEditText).apply {
                setText("")
            }
        }
    }
//        ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
//        ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
//        ????????????????????????????????????????????????????
//        ????????????????????????????????????????????????????

    fun addItem(keyword: Keyword) {
        if (keywordList.size >= PHRASE_SIZE) {
            context.hideSoftKeyboard()
            viewModel.phrase.value?.let { viewModel.verifyPhrase(it) }
            return
        }
        keywordList.add(keyword)
        notifyItemInserted(keywordList.size - 1)
    }

    fun removeItemAt(
        position: Int,
        root: ViewGroup,
        txtKeyword: AppCompatTextView,
        edtKeyword: AppCompatEditText,
        currentEdtKeyword: AppCompatEditText
    ) {

        keywordList.removeAt(position)
//        currentEdtKeyword.removeTextChangedListener(textWatcher)
//        textWatcher.apply {
//            _itemPosition = position - 1
//            _root = root
//            _txtKeyword = txtKeyword
//            _edtKeyword = edtKeyword
//        }
        onKeyListener._position = position - 1
        notifyItemRemoved(position)
        edtKeyword.apply {
            isVisible = true
            requestFocus()
        }
        txtKeyword.apply {
            val phrase = text.toString()
            text = phrase.substring(0, phrase.indexOf(".") + 1)
            setTextColor(Color.BLACK)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView.apply {
            layoutManager = this@SecretPhraseAdapter.layoutManager
        }
//        textWatcher._recyclerView = this.recyclerView
        onKeyListener.recyclerView = this.recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvItemKeyWordBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, keywordList[position])
    }

    override fun getItemCount(): Int {
        return keywordList.size
    }

    inner class ViewHolder(val binding: RvItemKeyWordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, item: Keyword) {

            initListeners(position)
            initTxtKeywordNum(position)
            addListeners()
            setFocusToCurrentItem()
            openKeyboardOnFirstItem(position)

//            if (textWatcher.isPhrasePastedFromClipboard) {
//                handlePastedPhrase(position, item)
//            }
        }

        private fun initListeners(position: Int) {
//            textWatcher.apply {
//                _itemPosition = position
//                _root = binding.root as ViewGroup
//                _txtKeyword = binding.txtKeywordNum
//                _edtKeyword = binding.edtKeyword
//            }
            onKeyListener._position = position
        }

        private fun addListeners() {
            binding.edtKeyword.apply {
//                addTextChangedListener(textWatcher)
                onFocusChangeListener = this@SecretPhraseAdapter.onFocusChangeListener
                setOnKeyListener(onKeyListener)
            }
        }

        private fun initTxtKeywordNum(position: Int) {
            layoutManager.spanSizes[position] = MultipleSpanGridLayoutManager.DEFAULT_SPAN_SIZE
            val textNum = "${position + 1}."
            binding.txtKeywordNum.text = textNum
        }

        private fun setFocusToCurrentItem() {
            binding.root.setBackgroundColor(Color.TRANSPARENT)
            binding.edtKeyword.requestFocus()
        }

        private fun openKeyboardOnFirstItem(position: Int) {
            if (position == 0) {
                context.showSoftKeyboard()
            }
        }

        private fun handlePastedPhrase(position: Int, item: Keyword) {
            if (position != keywordList.size - 1) {
                binding.edtKeyword.apply {
//                    removeTextChangedListener(textWatcher)
//                    setText(item.title)
//                    addTextChangedListener(textWatcher)
                    setSelection(binding.edtKeyword.text.toString().length)
                    isVisible = false
                }
                binding.txtKeywordNum.apply {
                    val tag = "${position + 1}.${item.title}"
                    text = tag
                    setTextColor(Color.WHITE)
                }
            } else {
//                textWatcher.isPhrasePastedFromClipboard = false
                binding.edtKeyword.apply {
//                    removeTextChangedListener(textWatcher)
                    setText(item.title)
                    setSelection(binding.edtKeyword.text.toString().length)
//                    addTextChangedListener(textWatcher)
                }
                binding.txtKeywordNum.setTextColor(Color.BLACK)
            }
            val spanSize = MeasureHelper(recyclerView, binding.root).getSpanSize()
            layoutManager.spanSizes[position] = spanSize
        }
    }
}