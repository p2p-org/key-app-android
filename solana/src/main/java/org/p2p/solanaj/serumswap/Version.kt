package org.p2p.solanaj.serumswap

object Version {

    private val PROGRAM_LAYOUT_VERSIONS = mapOf(
        "4ckmDgGdxQoPDLUkDT3vHgSAkzA3QRdNq5ywwY4sUSJn" to 1,
        "BJ3jrUzddfuSrZHXSCxMUUQsjKEyLmuuyZebkcaFp2fg" to 1,
        "EUqojwWA2rd19FZrzeBncJsm38Jm1hEhE3zsmX3bRc2o" to 2,
        "9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin" to 3,
    )

    fun getVersion(programId: String) = PROGRAM_LAYOUT_VERSIONS[programId]
}
