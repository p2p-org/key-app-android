package com.p2p.wallet.restore.ui.secretkeys.utils

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.main.model.Keyword
import com.p2p.wallet.restore.ui.secretkeys.adapter.MultipleSpanGridLayoutManager
import com.p2p.wallet.restore.ui.secretkeys.adapter.SecretPhraseAdapter
import com.p2p.wallet.restore.ui.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wallet.utils.resFromTheme

@Deprecated("Too complex logic, should be simplified")
class KeywordEditTextChangeListener(
    private val adapter: SecretPhraseAdapter,
    private val keywordList: ArrayList<Keyword>,
    private val viewModel: SecretKeyViewModel,
    var _recyclerView: RecyclerView? = null,
    var _txtKeyword: AppCompatTextView? = null,
    var _edtKeyword: AppCompatEditText? = null,
    var _root: ViewGroup? = null,
    var _itemPosition: Int? = null
) : TextWatcher {

    var isPhrasePastedFromClipboard = false
    private var isPastedPhraseBiggerThanAllowed = false
    private var containsNonEnglishChars = false
    private var textLengthBefore: Int = 0
    private var textLengthAfter: Int = 0

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        textLengthBefore = s.toString().length
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {

        // This case is expected to never happen
        if (_txtKeyword == null || _edtKeyword == null ||
            _root == null || _itemPosition == null || _recyclerView == null
        ) return

        val txtKeyword = _txtKeyword!!
        val edtKeyword = _edtKeyword!!
        val root = _root!!
        val itemPosition = _itemPosition!!
        val recyclerView = _recyclerView!!

        textLengthAfter = s.toString().length

        // Saving the value of edit text for the secret phrase
        keywordList[itemPosition].title = edtKeyword.text.toString()

        // Calculating the span size for the view
        val spanSize: Int = MeasureHelper(recyclerView, root).getSpanSize()
        (recyclerView.layoutManager as MultipleSpanGridLayoutManager)
            .spanSizes[itemPosition] =
            if (spanSize < MultipleSpanGridLayoutManager.SPAN_SIZE) spanSize
            else MultipleSpanGridLayoutManager.SPAN_SIZE

        // Saving current word in the secret phrase
        if (edtKeyword.text.toString().isNotEmpty()) {
            viewModel.phrase.value = getPhrase().trim()
        }

        val splitList = viewModel.phrase.value?.split(" ")
        isPhrasePastedFromClipboard = (textLengthAfter - textLengthBefore) > 1 && (splitList?.size!! <= 12)
        isPastedPhraseBiggerThanAllowed = (textLengthAfter - textLengthBefore) > 1 && (splitList?.size!! > 12)

        for (char in s.toString()) {
            val isEnglishLowerCase = char.toInt() in 97..122 || char.toInt() == 32

            if (!isEnglishLowerCase) {
                containsNonEnglishChars = true
                break
            } else {
                containsNonEnglishChars = false
            }
        }

        if (containsNonEnglishChars) {
            viewModel.postInvadedPhrase("Secret phrase can not contain non English characters")
        } else {
            viewModel.postInvadedPhrase("")
        }

        if (isPhrasePastedFromClipboard) {
            // User pasted the phrase from clipboard
            adapter.clear()
            splitList?.forEach {
                adapter.addItem(Keyword(it))
            }
            return
        }
        if (isPastedPhraseBiggerThanAllowed) {
            viewModel.postInvadedPhrase(root.context.getString(R.string.auth_seed_phrase_limit))
            viewModel.resetPhrase()
            return
        }
        println("debug: $isPastedPhraseBiggerThanAllowed")
        // Checking the case when the user inputs space
        // at the end of the word
        if (s.toString().isNotEmpty() && !containsNonEnglishChars) {
            if (s.toString().contains(' ')) {
                if (s.toString().endsWith(" ") && s.toString().length > 1) {

                    // When this case triggered, the word definitely is not empty,
                    // and user clicked the space, so we pass the current text
                    // from editText to textView in rv_item_key_word.xml
                    val allText = "${txtKeyword.text}$s"
                    txtKeyword.text = allText
                    txtKeyword.setTextColor(Color.BLACK)
                    edtKeyword.visibility = View.GONE
                    adapter.addItem(Keyword(""))
                }
                s?.replace(s.indexOf(" "), s.indexOf(" ") + 1, "")
            }
        }

        // To create an illusion for the user, the color of textView
        // is set to the same as the color if hint in editText
        if (s.toString().isEmpty()) {
            txtKeyword.setTextColor(txtKeyword.resFromTheme(R.attr.colorElementSecondary))
        }
    }

    private fun getPhrase(): String {
        val str = StringBuilder("")
        keywordList.forEach {
            str.append("${it.title} ")
        }
        return str.toString()
    }
}