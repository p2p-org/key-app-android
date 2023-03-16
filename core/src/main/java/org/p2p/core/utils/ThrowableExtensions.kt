package org.p2p.core.utils

import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.isConnectionError(): Boolean = this is UnknownHostException || this is SocketTimeoutException
