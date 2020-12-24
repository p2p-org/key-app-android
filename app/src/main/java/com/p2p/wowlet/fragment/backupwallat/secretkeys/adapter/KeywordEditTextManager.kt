package com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter

import android.graphics.Color
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.wowlet.entities.local.Keyword

class KeywordOnEditorActionListener(
    private val recyclerView: RecyclerView?,
    private val editText: AppCompatEditText,
    private val itemPosition: Int,
    private val keywordList: ArrayList<Keyword>
) : TextView.OnEditorActionListener {

    private val adapter = recyclerView?.adapter as SecretPhraseAdapter

    init {
        if (itemPosition == SecretPhraseAdapter.PHRASE_SIZE - 1) {
            editText.imeOptions = EditorInfo.IME_ACTION_DONE
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (itemPosition == SecretPhraseAdapter.PHRASE_SIZE -1 ) return false
        editText.setText(editText.text.toString())

        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (itemPosition != keywordList.size - 1) return false
            adapter.addItem(Keyword(""))
        }
        return false
    }
}

class KeywordEditTextChangeListener(
    private val recyclerView: RecyclerView?,
    private val editText: AppCompatEditText,
    private val textView: AppCompatTextView,
    private val parent: View,
    private val keywordList: ArrayList<Keyword>,
    private val itemPosition: Int,
    private val phrase: MutableLiveData<String>
) : TextWatcher {


    private val adapter = recyclerView?.adapter as SecretPhraseAdapter

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        keywordList[itemPosition].title = editText.text.toString()

        val spanSize: Int = MeasureHelper(recyclerView, parent).getSpanSize()
        (recyclerView?.layoutManager as MultipleSpanGridLayoutManager)
            .spanSizes[itemPosition] =
            if (spanSize < MultipleSpanGridLayoutManager.SPAN_SIZE) spanSize
            else MultipleSpanGridLayoutManager.SPAN_SIZE


        if (editText.text.toString().isNotEmpty()) {
            phrase.value = getPhrase().trim()
        }

        if (s.toString().isNotEmpty()) {
            if (s.toString().contains(' ')) {
                if (s.toString().endsWith(" ")) {
                    val allText = "${textView.text}${s.toString()}"
                    textView.text = allText
                    editText.visibility = View.GONE
                    adapter.addItem(Keyword(""))
                }
                s?.replace(s.indexOf(" "), s.indexOf(" ") + 1, "")
            }

            textView.setTextColor(Color.BLACK)

        }else {
            textView.setTextColor(ContextCompat.getColor(parent.context, R.color.hintColor))
        }

    }


    private fun getPhrase() : String {
        val str = StringBuilder("")
        keywordList.forEach {
            str.append("${it.title} ")
        }
        return str.toString()
    }


}

class OnFocusChangeListener(private val position: Int) : View.OnFocusChangeListener {
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        val parent = v?.parent as ViewGroup
        val textView = parent.getChildAt(0) as AppCompatTextView
        val editText = v as AppCompatEditText
        if (!hasFocus) {
            parent.setBackgroundResource(R.drawable.bg_secret_keyword)
            textView.setTextColor(Color.WHITE)
            editText.setTextColor(Color.WHITE)
        }
    }
}






















