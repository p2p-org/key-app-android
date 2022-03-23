package org.p2p.wallet.debugdrawer

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import io.palaima.debugdrawer.timber.R
import io.palaima.debugdrawer.timber.data.LumberYard
import io.palaima.debugdrawer.timber.data.LumberYard.OnSaveLogListener
import io.palaima.debugdrawer.timber.model.LogEntry
import io.palaima.debugdrawer.timber.ui.LogAdapter
import io.palaima.debugdrawer.timber.util.Intents
import org.p2p.wallet.BuildConfig
import java.io.File

class CustomLogDialog(context: Context) : AlertDialog(context, R.style.Theme_AppCompat) {

    private val adapter: LogAdapter by lazy {
        LogAdapter()
    }

    private val handler = Handler(Looper.getMainLooper())

    private val logListView: ListView = ListView(context)

    private val logs = mutableListOf<LogEntry>()

    init {
        logListView.transcriptMode = ListView.TRANSCRIPT_MODE_NORMAL
        logListView.adapter = adapter

        setTitle("Logs")
        setView(logListView)
        setButton(BUTTON_NEGATIVE, "Close") { _, _ -> /* no-op */ }
        setButton(BUTTON_POSITIVE, "Share") { _, _ -> share() }
    }

    override fun onStart() {
        super.onStart()
        val lumberYard = LumberYard.getInstance(context)
        logs += lumberYard.bufferedLogs()
        adapter.setLogs(logs)
        logListView.setSelection(logs.size - 1)
        lumberYard.setOnLogListener { logEntry -> addLogEntry(logEntry) }
    }

    private fun addLogEntry(logEntry: LogEntry) {
        handler.post { adapter.addLog(logEntry) }
    }

    override fun onStop() {
        super.onStop()
        LumberYard.getInstance(context).setOnLogListener(null)
    }

    private fun share() {
        LumberYard.getInstance(context)
            .save(object : OnSaveLogListener {
                override fun onSave(file: File) {
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.type = "text/plain"
                    val fromFile = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    sendIntent.putExtra(Intent.EXTRA_STREAM, fromFile)
                    Intents.maybeStartActivity(context, sendIntent)
                }

                override fun onError(message: String) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            })
    }
}
