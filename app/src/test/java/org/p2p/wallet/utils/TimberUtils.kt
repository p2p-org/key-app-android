package org.p2p.wallet.utils

import android.util.Log
import timber.log.Timber
import java.io.PrintStream

private fun Int.toPrinter(): PrintStream = when {
    this == Log.WARN || this == Log.ERROR || this == Log.ASSERT -> System.err
    else -> System.out
}

fun plantTimberToStdout(
    defaultTag: String,
    excludeMessages: List<String> = emptyList(),
    excludeStacktraceForMessages: List<String> = emptyList()
) {
    Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (excludeMessages.any { message.startsWith(it) }) {
                return
            }

            val printer = priority.toPrinter()

            val sb = StringBuilder()
            when (priority) {
                Log.VERBOSE -> sb.append("V")
                Log.DEBUG -> sb.append("D")
                Log.INFO -> sb.append("I")
                Log.WARN -> sb.append("W")
                Log.ERROR -> sb.append("E")
                Log.ASSERT -> sb.append("A")
            }
            sb.append("[${(tag ?: defaultTag)}]")

            if (t == null) {
                sb.append(message)
                printer.println(sb.toString())
            } else {
                // exclude from message that in exclusion list
                if (excludeStacktraceForMessages.none { message.startsWith(it) || t.message?.startsWith(it) == true }) {
                    printer.println("[$priority] $tag: ${t.stackTraceToString()}")
                } else {
                    // there's case, when we getting exception with message == null but message itself contains full stacktrace
                    val firstStackTraceLine = t.stackTraceToString()
                        .split("\n")
                        .firstOrNull()
                        ?: message.split("\n")
                            .firstOrNull()

                    if (firstStackTraceLine != null) {
                        printer.println(" -- > ${t::class.simpleName}: $firstStackTraceLine")
                    }
                }
            }
        }
    })
}
