package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetExpandViewBinding
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.emptyString

class ExpandView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    private var isExpanded: Boolean = false
    private var headerTitle: String = emptyString()
    private var headerVisible: Boolean = true
    private var arrowVisible: Boolean = true
    private var contentPadding: Int = dip(16)
    private var contentPaddingBottom: Int = dip(24)
    private var container: LinearLayout? = null
    private var headerLayout: ConstraintLayout? = null
    private var expandLayout: LinearLayout? = null

    private val binding = WidgetExpandViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val attrs = context.obtainStyledAttributes(attributeSet, R.styleable.ExpandView)
        initAttrs(attrs)
        setupHeader()
        setupExpandLayout()
        binding.expandableLayout.setup(isExpanded)
        binding.headerLayoutView.setOnClickListener {
            binding.expandableLayout.toggle(animate = true)
        }
        container = binding.container
        headerLayout = binding.headerLayoutView
        expandLayout = binding.expandableLayout
    }

    private fun initAttrs(attrs: TypedArray) {
        isExpanded = attrs.getBoolean(R.styleable.ExpandView_isExpanded, false)
        headerTitle = attrs.getString(R.styleable.ExpandView_android_text) ?: emptyString()
    }

    private fun setupHeader() {
        with(binding) {
            headerTitleView.text = headerTitle
            headerLayoutView.isVisible = headerVisible
            if (arrowVisible) {
                headerLayoutView.setOnClickListener {
                    expandableLayout.toggle()
                }
            } else {
                arrowIconView.isVisible = arrowVisible
            }
        }
    }

    private fun setupExpandLayout() {
        binding.expandableLayout.onExpansionChangeListener = { rotation, state ->
            setArrowRotation(rotation)
        }
        if (contentPadding == 0) {
            binding.expandableLayout.setPadding(0, 0, 0, contentPaddingBottom)
        } else {
            binding.expandableLayout.setPadding(contentPadding, 0, contentPadding, contentPadding)
        }
    }

    private fun setArrowRotation(@FloatRange(from = .0, to = 1.0) value: Float) {
        binding.arrowIconView.rotation = value * -180
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (container == null || headerLayout == null || expandLayout == null) {
            super.addView(child, index, params)
        } else {
            binding.expandableLayout.addView(child, params)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState() ?: return null
        val myState = ExpandViewState(superState)
        myState.isExpanded = binding.expandableLayout.state == ExpandLinearLayout.State.EXPANDED
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val myState = state as ExpandViewState
        super.onRestoreInstanceState(state)
        post {
            if (myState.isExpanded == true) {
                binding.expandableLayout.expand(animate = false)
            } else {
                binding.expandableLayout.collapse(animate = false)
            }
        }
    }

    internal class ExpandViewState : BaseSavedState {
        var isExpanded: Boolean? = null

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel) : super(source) {
            isExpanded = source.readInt() != 0
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeInt(if (isExpanded == true) 1 else 0)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<ExpandViewState> =
                object : Parcelable.ClassLoaderCreator<ExpandViewState> {

                    override fun createFromParcel(
                        p0: Parcel,
                        p1: ClassLoader?
                    ): ExpandViewState {
                        return ExpandViewState(p0)
                    }

                    override fun createFromParcel(p0: Parcel): ExpandViewState {
                        return ExpandViewState(p0)
                    }

                    override fun newArray(p0: Int): Array<ExpandViewState> = newArray(p0)
                }
        }
    }
}
