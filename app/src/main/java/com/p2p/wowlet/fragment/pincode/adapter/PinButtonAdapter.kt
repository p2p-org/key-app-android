package com.p2p.wowlet.fragment.pincode.adapter

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.p2p.wowlet.R
import com.p2p.wowlet.utils.isFingerPrintSet
import com.p2p.wowlet.entities.enums.PinCodeFragmentType

class PinButtonAdapter(
    var isFingerprintEnabled: Boolean,
    val context: Context,
    val pinCodeFragmentType: PinCodeFragmentType,
    val pinButtonClick: (String) -> Unit,
    val pinFingerPrint: () -> Unit,
    val removeCode: () -> Unit
) : BaseAdapter() {
    val list = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "fingerprint", "0", "remove")

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): String {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return Integer.parseInt(list[position]).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.item_pin_button, parent, false)

        val buttonSize = context.resources.getDimensionPixelSize(R.dimen.dp_75)
        val text = getItem(position)
        val button = view.findViewById<AppCompatTextView>(R.id.button)
        when (text) {
            "fingerprint" -> {
                button.layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
                val ssb = SpannableStringBuilder()
                ssb.append(" ")
                ssb.setSpan(
                    ImageSpan(context, R.drawable.ic_fingerprint),
                    ssb.length - 1,
                    ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssb.append(" ")
                button.text = ssb
                button.setBackgroundResource(android.R.color.transparent)
                button.setPadding(0, 0, 0, 0)
                button.setOnClickListener {
                    pinFingerPrint.invoke()
                }
                if (pinCodeFragmentType == PinCodeFragmentType.CREATE) {
                    button.isClickable = false
                    button.visibility = View.GONE
                } else if (!context.isFingerPrintSet() && pinCodeFragmentType != PinCodeFragmentType.CREATE) {
                    button.isClickable = false
                    button.visibility = View.GONE
                } else if (!isFingerprintEnabled) {
                    button.isClickable = false
                    button.visibility = View.GONE
                }
            }
            "remove" -> {
                button.layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
                val ssb = SpannableStringBuilder()
                ssb.append(" ")
                ssb.setSpan(
                    ImageSpan(context, R.drawable.ic_delete_pin),
                    ssb.length - 1,
                    ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssb.append(" ")
                button.text = ssb
                button.setBackgroundResource(android.R.color.transparent)
                button.setPadding(0, 0, 0, context.resources.getDimensionPixelSize(R.dimen.dp_20))
                button.setOnClickListener {
                    removeCode.invoke()
                }
            }
            else -> {
                button.layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
                button.text = text
                button.setOnClickListener {
                    pinButtonClick.invoke(text)
                }
            }
        }
        return view
    }

    fun updateFingerprintStatus(fingerprintEnabled: Boolean) {
        isFingerprintEnabled = fingerprintEnabled
        notifyDataSetChanged()
    }

}