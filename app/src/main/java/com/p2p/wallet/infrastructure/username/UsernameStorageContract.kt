package com.p2p.wallet.infrastructure.username

interface UsernameStorageContract {
    fun saveString(key: String, data: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun contains(key: String): Boolean
    fun clear()
}