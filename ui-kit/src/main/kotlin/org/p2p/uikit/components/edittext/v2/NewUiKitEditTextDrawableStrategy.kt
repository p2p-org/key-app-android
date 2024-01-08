package org.p2p.uikit.components.edittext.v2

/**
 * Different behavior for end drawable for UiKitEditText
 */
enum class NewUiKitEditTextDrawableStrategy {
    /**
     * nothing special, you can control icon visibility like always
     */
    NONE,

    /**
     * Show drawable when edit text is not empty and hide it when it's empty
     */
    SHOW_ON_TEXT
}
