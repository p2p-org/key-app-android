package org.p2p.ethereumkit.internal.spv.net.connection

import org.bouncycastle.crypto.digests.KeccakDigest

data class Secrets(var aes: ByteArray,
                   var mac: ByteArray,
                   var token: ByteArray,
                   var egressMac: KeccakDigest,
                   var ingressMac: KeccakDigest)
