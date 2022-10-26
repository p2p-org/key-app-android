package org.p2p.wallet

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper

const val KEY_PREFS_BACKUP = "prefs"

class AppPrefsBackupAgent : BackupAgentHelper() {
    override fun onCreate() {
        val context = this
        val prefsName = "${context.packageName}.account_prefs"
        SharedPreferencesBackupHelper(context, prefsName).also {
            addHelper(KEY_PREFS_BACKUP, it)
        }
    }
}
