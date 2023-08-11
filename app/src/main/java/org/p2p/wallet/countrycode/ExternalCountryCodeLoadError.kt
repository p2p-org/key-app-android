package org.p2p.wallet.countrycode

class ExternalCountryCodeLoadError(
    cause: Throwable,
    message: String? = cause.message,
) : Exception(message, cause)
