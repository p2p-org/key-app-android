package org.p2p.wallet.utils

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk

/**
 * Uses mutableMapOf inside instead of Android XML files
 */
class InMemorySharedPreferences : SharedPreferences {
    private val prefsLocalData = mutableMapOf<String, Any>()

    override fun getAll(): MutableMap<String, *> {
        return prefsLocalData
    }

    override fun getString(key: String?, defValue: String?): String? {
        return prefsLocalData.getOrDefault(key, defValue) as String?
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return prefsLocalData.getOrDefault(key, defValues) as MutableSet<String>?
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return prefsLocalData.getOrDefault(key, defValue) as Int
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return prefsLocalData.getOrDefault(key, defValue) as Long
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return prefsLocalData.getOrDefault(key, defValue) as Float
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return prefsLocalData.getOrDefault(key, defValue) as Boolean
    }

    override fun contains(key: String?): Boolean {
        return prefsLocalData.contains(key)
    }

    override fun edit(): SharedPreferences.Editor = mockk {
        val that = this
        every { apply() } returns Unit
        every { putLong(any(), any()) } answers {
            prefsLocalData[firstArg()] = secondArg()
            that
        }
        every { putString(any(), any()) } answers {
            prefsLocalData[firstArg()] = secondArg()
            that
        }
        every { putStringSet(any(), any()) } answers {
            prefsLocalData[firstArg()] = secondArg()
            that
        }
        every { putInt(any(), any()) } answers {
            prefsLocalData[firstArg()] = secondArg()
            that
        }
        every { putFloat(any(), any()) } answers {
            prefsLocalData[firstArg()] = secondArg()
            that
        }
        every { putBoolean(any(), any()) } answers {
            prefsLocalData[firstArg()] = secondArg()
            that
        }
        every { clear() } answers {
            prefsLocalData.clear()
            that
        }
    }

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
    }
}
