package org.p2p.wallet.utils

import android.util.Log
import org.junit.rules.ExternalResource
import timber.log.Timber
import java.io.PrintStream

private fun Int.toPrinter(): PrintStream = when {
    this == Log.WARN || this == Log.ERROR || this == Log.ASSERT -> System.err
    else -> System.out
}

//companion object {
//    @ClassRule
//    @JvmField
//    val timber = TimberUnitTestInstance("Swap:BackPress")
//}
/**
 * ^ example of usage ^
 * should be PUBLIC, JVMFIELD and STATIC
 * plants and removes printing to STDOUT and STDERR
 */
class TimberUnitTestInstance(
    private val defaultTag: String,
    private val excludeMessages: List<String> = emptyList(),
    private val excludeStacktraceForMessages: List<String> = emptyList()
) : ExternalResource() {
    override fun before() = plantTimberToStdout(defaultTag, excludeMessages, excludeStacktraceForMessages)
    override fun after() = Timber.uprootAll()
}

fun plantStubTimber() {
    Timber.plant(object : Timber.DebugTree() {})
}

private fun plantTimberToStdout(
    defaultTag: String,
    excludeMessages: List<String> = emptyList(),
    excludeStacktraceForMessages: List<String> = emptyList()
) {
    Timber.plant(object : Timber.DebugTree() {
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
            sb.append("[${(tag ?: defaultTag)}] ")

            if (t == null) {
                sb.append(message)
                printer.println(sb.toString())
            } else {
                val isMessageNotExcluded = excludeStacktraceForMessages.none {
                    message.startsWith(it) ||
                        t.message?.startsWith(it) == true ||
                        t.cause?.message?.startsWith(it) == true
                }
                // exclude from message that in exclusion list
                if (isMessageNotExcluded) {
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
