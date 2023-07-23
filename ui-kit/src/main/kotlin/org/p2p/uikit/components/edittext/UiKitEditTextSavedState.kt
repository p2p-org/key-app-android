package org.p2p.uikit.components.edittext

import android.os.Parcel
import android.os.Parcelable
import android.view.View

// Custom state class to hold the state of the custom EditText
internal class UiKitEditTextSavedState : View.BaseSavedState {
    var text: String? = null

    constructor(superState: Parcelable?, text: String?) : super(superState) {
        this.text = text
    }

    constructor(source: Parcel) : super(source) {
        text = source.readString()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeString(text)
    }

    companion object CREATOR : Parcelable.Creator<UiKitEditTextSavedState> {
        override fun createFromParcel(source: Parcel): UiKitEditTextSavedState {
            return UiKitEditTextSavedState(source)
        }

        override fun newArray(size: Int): Array<UiKitEditTextSavedState?> {
            return arrayOfNulls(size)
        }
    }
}
