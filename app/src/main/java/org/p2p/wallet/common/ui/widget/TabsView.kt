package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.children
import androidx.core.widget.TextViewCompat
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetTabRadiogroupBinding
import org.p2p.wallet.utils.dip
import kotlin.math.max

private const val ANIMATION_DURATION = 200L

class TabsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : FrameLayout(context, attrs, defStyleAttrs), RadioGroup.OnCheckedChangeListener {

    companion object {
        private const val MARGIN_TAB_DP = 2
    }

    var onTabChanged: ((Int) -> Unit)? = null

    private var switchAnimationDuration: Long = ANIMATION_DURATION

    private var currentTabs = mutableListOf<TabItem>()

    private val binding by lazy {
        WidgetTabRadiogroupBinding.inflate(LayoutInflater.from(context), this).apply {
            radioGroup.setOnCheckedChangeListener(this@TabsView)
        }
    }

    init {
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        binding.radioGroup.children
            .map { it as RadioButton }
            .forEach { it.isChecked = it.id == checkedId }
        animateSelector(checkedId)
        onTabChanged?.invoke(checkedId)
    }

    override fun setEnabled(enabled: Boolean) {
        binding.radioGroup.children.forEach { it.isEnabled = enabled }
        super.setEnabled(enabled)
    }

    fun setTabs(tabs: List<TabItem>, defaultTab: TabItem? = null) {
        if (areSameTabs(tabs)) return

        val defaultIndex = tabs.indexOfFirst { it.tabId == defaultTab?.tabId }

        tabs.forEachIndexed { index, item ->
            val button = RadioButton(context).apply {
                val isDefault = if (defaultIndex != -1) defaultIndex == index else index == 0
                id = item.tabId
                text = item.title
                buttonDrawable = null
                background = null
                gravity = Gravity.CENTER
                isChecked = isDefault

                updateButtonStyleAndLayoutParams()
            }

            binding.radioGroup.addView(button)

            if (button.isChecked) {
                button.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        setupSelectorParams(listOf(button))
                        button.viewTreeObserver.removeOnPreDrawListener(this)
                        return false
                    }
                })
            }
        }
    }

    private fun areSameTabs(tabs: List<TabItem>): Boolean {
        if (currentTabs == tabs) return true
        currentTabs.clear()
        currentTabs.addAll(tabs)
        binding.radioGroup.removeAllViews()
        return false
    }

    private fun setupSelectorParams(buttons: List<RadioButton>) {
        if (buttons.isEmpty()) return
        val button = buttons.find { it.isChecked } ?: return

        binding.selector.layoutParams = LayoutParams(
            button.measuredWidth, button.measuredHeight
        )
        binding.selector.translationX = button.x
        binding.selector.translationY = button.y
    }

    private fun animateSelector(checkedId: Int) {
        val button = binding.radioGroup.children.find { it.id == checkedId } ?: return
        val selector = binding.selector

        selector
            .animate()
            .setDuration(switchAnimationDuration)
            .translationX(button.x)
            .withStartAction {
                resizeSelector(selector, button)
            }
    }

    private fun resizeSelector(selector: FrameLayout, button: View) {
        /*
         * Tabs may have different width, therefore selector is being resized when tab is changed
         * */
        val selectorParams = selector.layoutParams
        val buttonParams = button.layoutParams

        if (buttonParams.width != selectorParams.width) {
            selectorParams.width = button.width
            selector.layoutParams = selectorParams
        }
    }

    private fun RadioButton.updateButtonStyleAndLayoutParams() {
        TextViewCompat.setTextAppearance(this, R.style.WalletTheme_TextAppearance_SemiBold14)
        setTextColor(context.getColor(R.color.systemSuccessMain))
        setBackgroundResource(R.drawable.bg_chart_rounded_stroke)

        measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val unselectedButtonWidth = measuredWidth

        measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val selectedButtonWidth = measuredWidth

        val params = RadioGroup.LayoutParams(max(unselectedButtonWidth, selectedButtonWidth), WRAP_CONTENT, 1f)
        layoutParams = params

        val marginParams = layoutParams as MarginLayoutParams
        marginParams.leftMargin = dip(MARGIN_TAB_DP)
        layoutParams = marginParams
    }
}

data class TabItem(
    val tabId: Int,
    val title: String
)
