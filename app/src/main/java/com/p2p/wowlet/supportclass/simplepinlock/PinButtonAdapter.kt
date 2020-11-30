package com.p2p.wowlet.supportclass.simplepinlock

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.*
import android.widget.BaseAdapter
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.p2p.wowlet.R

/**
 * Adapter class for PinButton GridView
 */
class PinButtonAdapter(
    val context: Context,
    val pinButtonClick: (String) -> Unit,
    val pinFingerPrint: () -> Unit,
    val pinReset: () -> Unit
) : BaseAdapter(), Util {
    val list = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "reset", "0", "fingerprint")

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
            "reset" -> {
                button.layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
                button.text = "RESET"
                button.isClickable=true
                button.textSize= context.resources.getDimensionPixelSize(R.dimen.sp_5).toFloat()
                button.setBackgroundResource(android.R.color.transparent)
                button.setOnClickListener {
                    pinReset.invoke()
                }
            }
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

}