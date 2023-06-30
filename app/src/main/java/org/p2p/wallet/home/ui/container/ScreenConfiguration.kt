package org.p2p.wallet.home.ui.container

import androidx.fragment.app.Fragment
import android.os.Bundle
import kotlin.reflect.KClass
import org.p2p.uikit.components.ScreenTab

class ScreenConfiguration(
    val screen: ScreenTab,
    val kClass: KClass<out Fragment>,
    val bundle: Bundle? = null
)
