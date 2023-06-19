package org.p2p.wallet.deeplinks

@Deprecated("Will be removed. The click listener will be handled inside [MainContainerFragment]")
interface CenterActionButtonClickSetter {
    fun setOnCenterActionButtonListener(block: () -> Unit)
}
