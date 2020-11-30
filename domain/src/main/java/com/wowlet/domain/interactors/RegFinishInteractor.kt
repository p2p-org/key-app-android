package com.wowlet.domain.interactors

interface RegFinishInteractor {
    fun finishLoginReg(regFinish: Boolean)
    fun isCurrentLoginReg():Boolean
}