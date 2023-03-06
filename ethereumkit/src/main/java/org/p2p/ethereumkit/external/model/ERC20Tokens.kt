package org.p2p.ethereumkit.external.model

import org.p2p.ethereumkit.internal.models.EthAddress

enum class ERC20Tokens(val contractAddress: String, val mintAddress: String) {
    USDC(
        contractAddress = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
        mintAddress = "A9mUU4qviSctJVPJdBJWkb28deg915LYJKrzQ19ji3FM"
    ),
    USDT(
        contractAddress = "0xdAC17F958D2ee523a2206206994597C13D831ec7",
        mintAddress = "Dn4noZ5jgGfkntzcQSUZ8czkreiZ1ForXYoV2H8Dm7S1"
    ),
    ETH(
        contractAddress = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
        mintAddress = "7vfCXTUXx5WJV5JADk17DUJ4ksgau7utNKj4b963voxs"
    ),
    DAI(
        contractAddress = "0x6B175474E89094C44Da98b954EedeAC495271d0F",
        mintAddress = "EjmyN6qEC1Tf1JxiG1ae7UTJhUxSwk1TCWNWqxWV4J6o"
    ),
    wBTC(
        contractAddress = "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599",
        mintAddress = "3NZ9JMVBmGAqocybic2c7LQCJScmgsAZ6vQqTDzcqmJh"
    ),
    USDK(
        contractAddress = "0x1c48f86ae57291F7686349F12601910BD8D470bb",
        mintAddress = "43m2ewFV5nDepieFjT9EmAQnc1HRtAF247RBpLGFem5F"
    ),
    FRAX(
        contractAddress = "0x853d955aCEf822Db058eb8505911ED77F175b99e",
        mintAddress = "FR87nWEUxVgerFGhZM8Y4AggKGLnaXswr1Pd8wZ4kZcp"
    ),
    SHIB(
        contractAddress = "0x95aD61b0a150d79219dCF64E1E6Cc01f0B64C4cE",
        mintAddress = "CiKu4eHsVrc1eueVQeHn7qhXTcVu95gSQmBpX4utjL9z"
    ),
    SUSHI(
        contractAddress = "0x6B3595068778DD592e39A122f4f5a5cF09C90fE2",
        mintAddress = "ChVzxWRmrTeSgwd3Ui3UumcN8KX7VK3WaD4KGeSKpypj"
    ),
    DYDX(
        contractAddress = "0x92D6C1e31e14520e676a687F0a93788B716BEff5",
        mintAddress = "4Hx6Bj56eGyw8EJrrheM6LBQAvVYRikYCWsALeTrwyRU"
    ),
    MANA(
        contractAddress = "0x0F5D2fB29fb7d3CFeE444a200298f468908cC942",
        mintAddress = "7dgHoN8wBZCc5wbnQ2C47TDnBMAxG4Q5L3KjP67z8kNi"
    ),
    SAND(
        contractAddress = "0x3845badAde8e6dFF049820680d1F14bD3903a5d0",
        mintAddress = "49c7WuCZkQgc3M4qH8WuEUNXfgwupZf1xqWkDQ7gjRGt"
    ),
    UNI(
        contractAddress = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984",
        mintAddress = "8FU95xFJhUUkyyCLU13HSzDLs7oC4QZdXQHL6SCeab36"
    ),
    LDO(
        contractAddress = "0x5A98FcBEA516Cf06857215779Fd812CA3beF1B32",
        mintAddress = "HZRCwxP2Vq9PCpPXooayhJ2bxTpo5xfpQrwB1svh332p"
    ),
    HXRO(
        contractAddress = "0x4bD70556ae3F8a6eC6C4080A0C327B24325438f3",
        mintAddress = "HxhWkVpk5NS4Ltg5nij2G671CKXFRKPK8vy271Ub4uEK"
    );

    companion object {
        fun findToken(contractAddress: EthAddress): ERC20Tokens {
            return values().first { contractAddress.hex.equals(it.contractAddress, ignoreCase = true) }
        }
    }
}
