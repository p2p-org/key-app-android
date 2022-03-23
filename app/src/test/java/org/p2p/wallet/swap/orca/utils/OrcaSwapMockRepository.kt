package org.p2p.wallet.swap.orca.utils

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import java.math.BigInteger

class OrcaSwapMockRepository : OrcaSwapRepository {

    override suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance {
        // BTC/ETH
        if (publicKey.toBase58() == "81w3VGbnszMKpUwh9EzAF9LpRzkKxc5XYCW64fuYk1jH") {
            return AccountBalance(
                account = PublicKey("81w3VGbnszMKpUwh9EzAF9LpRzkKxc5XYCW64fuYk1jH"),
                amount = BigInteger.valueOf(1014L),
                decimals = 6
            )
        }
        if (publicKey.toBase58() == "6r14WvGMaR1xGMnaU8JKeuDK38RvUNxJfoXtycUKtC7Z") {
            return AccountBalance(
                account = PublicKey("6r14WvGMaR1xGMnaU8JKeuDK38RvUNxJfoXtycUKtC7Z"),
                amount = BigInteger.valueOf(16914L),
                decimals = 6
            )
        }

        // BTC/SOL[aquafarm]
        if (publicKey.toBase58() == "9G5TBPbEUg2iaFxJ29uVAT8ZzxY77esRshyHiLYZKRh8") {
            return AccountBalance(
                account = PublicKey("9G5TBPbEUg2iaFxJ29uVAT8ZzxY77esRshyHiLYZKRh8"),
                amount = BigInteger.valueOf(18448748L),
                decimals = 6
            )
        }
        if (publicKey.toBase58() == "5eqcnUasgU2NRrEAeWxvFVRTTYWJWfAJhsdffvc6nJc2") {
            return AccountBalance(
                account = PublicKey("5eqcnUasgU2NRrEAeWxvFVRTTYWJWfAJhsdffvc6nJc2"),
                amount = BigInteger.valueOf(7218011507888L),
                decimals = 9
            )
        }

        // ETH/SOL
        if (publicKey.toBase58() == "FidGus13X2HPzd3cuBEFSq32UcBQkF68niwvP6bM4fs2") {
            return AccountBalance(
                account = PublicKey("FidGus13X2HPzd3cuBEFSq32UcBQkF68niwvP6bM4fs2"),
                amount = BigInteger.valueOf(574220L),
                decimals = 6
            )
        }
        if (publicKey.toBase58() == "5x1amFuGMfUVzy49Y4Pc3HyCVD2usjLaofnzB3d8h7rv") {
            return AccountBalance(
                account = PublicKey("5x1amFuGMfUVzy49Y4Pc3HyCVD2usjLaofnzB3d8h7rv"),
                amount = BigInteger.valueOf(13997148152L),
                decimals = 9
            )
        }

        // ETH/SOL[aquafarm]
        if (publicKey.toBase58() == "7F2cLdio3i6CCJaypj9VfNDPW2DwT3vkDmZJDEfmxu6A") {
            return AccountBalance(
                account = PublicKey("7F2cLdio3i6CCJaypj9VfNDPW2DwT3vkDmZJDEfmxu6A"),
                amount = BigInteger.valueOf(4252752761L),
                decimals = 6
            )
        }
        if (publicKey.toBase58() == "5pUTGvN2AA2BEzBDU4CNDh3LHER15WS6J8oJf5XeZFD8") {
            return AccountBalance(
                account = PublicKey("5pUTGvN2AA2BEzBDU4CNDh3LHER15WS6J8oJf5XeZFD8"),
                amount = BigInteger.valueOf(103486885774058L),
                decimals = 9
            )
        }

        // SOCN/SOL
        if (publicKey.toBase58() == "C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV") {
            return AccountBalance(
                account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
                amount = BigInteger.valueOf(20097450122295L),
                decimals = 9
            )
        }

        if (publicKey.toBase58() == "DzdxH5qJ68PiM1p5o6PbPLPpDj8m1ZshcaMFATcxDZix") {
            return AccountBalance(
                account = PublicKey("DzdxH5qJ68PiM1p5o6PbPLPpDj8m1ZshcaMFATcxDZix"),
                amount = BigInteger.valueOf(27474561069286L),
                decimals = 9
            )
        }

        throw IllegalStateException("No balance found for ${publicKey.toBase58()}")
    }

    override suspend fun loadTokenBalances(publicKeys: List<String>): List<Pair<String, AccountBalance>> {
        return emptyList()
    }

    override suspend fun sendAndWait(serializedTransaction: String) {
    }
}
