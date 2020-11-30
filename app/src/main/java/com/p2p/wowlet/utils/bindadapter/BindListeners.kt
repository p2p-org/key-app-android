package com.p2p.wowlet.utils.bindadapter

import androidx.appcompat.widget.AppCompatCheckBox

interface OnUserCheckedChangeListener {
    fun onUserCheckChange(view: AppCompatCheckBox, isChecked:Boolean)
}