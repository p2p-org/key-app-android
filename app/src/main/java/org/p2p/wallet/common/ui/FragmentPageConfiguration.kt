package org.p2p.wallet.common.ui

import androidx.fragment.app.Fragment
import android.os.Bundle
import kotlin.reflect.KClass

class FragmentPageConfiguration(
    val kClass: KClass<out Fragment>,
    val bundle: Bundle? = null
)
