package org.p2p.wallet.debug.logs

import android.content.Context
import io.palaima.debugdrawer.timber.model.LogEntry
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Calendar
import java.util.Deque
import java.util.Locale
import timber.log.Timber
import timber.log.Timber.DebugTree

private const val BUFFER_SIZE = 200
private const val LOG_FILE_NAME = "logs.lumberapp.txt"

internal class CustomLumberYard(context: Context) {

    companion object {
        private var sInstance: CustomLumberYard? = null

        fun getInstance(context: Context): CustomLumberYard {
            if (sInstance == null) {
                sInstance = CustomLumberYard(context)
            }
            return sInstance!!
        }
    }

    private val logDatePattern: DateFormat = SimpleDateFormat("MM-dd HH:mm:ss.S", Locale.US)
    private val entries: Deque<LogEntry> = ArrayDeque(BUFFER_SIZE + 1)
    private val logDir = File("${context.cacheDir.path}/nambaone/logs").apply {
        mkdirs()
    }

    var onLogListener: ((LogEntry) -> Unit)? = null

    fun tree(): Timber.Tree = object : DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            addEntry(LogEntry(priority, tag, message, logDatePattern.format(Calendar.getInstance().time)))
        }
    }

    fun getLogEntries() = entries.toList()

    @Synchronized
    private fun addEntry(entry: LogEntry) {
        entries.addLast(entry)
        if (entries.size > BUFFER_SIZE) {
            entries.removeFirst()
        }

        onLogListener?.invoke(entry)
    }

    /**
     * Save the current logs to disk.
     */
    fun save(
        onSuccess: (file: File) -> Unit,
        onError: (message: String?) -> Unit
    ) {
        try {
            val output = File(logDir,
                LOG_FILE_NAME
            )
            output.writeText(entries.joinToString("\n") { it.prettyPrint() })
            onSuccess(output)
        } catch (e: IOException) {
            onError(e.message)
            e.printStackTrace()
        }
    }

    fun cleanUp() {
        logDir.listFiles()?.firstOrNull { it.name == LOG_FILE_NAME }?.deleteOnExit()
    }
}
