package com.p2p.wowlet.appbase.utils

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

fun <Binding : ViewDataBinding> Fragment.dataBinding(layout: Int) =
    DataBindingDelegate<Binding>(this, layout)
