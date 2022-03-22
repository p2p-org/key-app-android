package org.p2p.wallet.swap.orca.utils

import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.solanaj.core.PublicKey
import java.math.BigInteger

object OrcaSwapData {

    val socnSOLStableAquafarmsPool = OrcaPool(
        account = PublicKey("2q6UMko5kTnv866W9MTeAFau94pLpsdeNjDdSYSgZUXr"),
        authority = PublicKey("Gyd77CwV23qq937x9UDa4TDkxEeQF9tp8ifotYxqW3Kd"),
        nonce = 255,
        poolTokenMint = PublicKey("APNpzQvR91v1THbsAyG3HHrUEwvexWYeNCFLQuVnxgMc"),
        tokenAccountA = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
        tokenAccountB = PublicKey("DzdxH5qJ68PiM1p5o6PbPLPpDj8m1ZshcaMFATcxDZix"),
        feeAccount = PublicKey("42Xzazs9EvjtidvEDrj3JXbDtf6fpTq5XHh96mPctvBV"),
        feeNumerator = BigInteger.valueOf(6L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(1L),
        ownerTradeFeeDenominator = BigInteger.valueOf(10000L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "SOCN",
        tokenBName = "SOL",
        curveType = "Stable",
        amp = BigInteger.valueOf(100L),
        programVersion = 2,
        deprecated = false,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(19645113670860L),
            decimals = 9
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(27266410382891),
            decimals = 9
        ),
    ).also { it.isStable = true }

    val solNinjaAquafarmsPool = OrcaPool(
        account = PublicKey("3ECUtPokme1nimJfuAkMtcm7QYjDEfXRQzmGC16LuYnz"),
        authority = PublicKey("H8f9n2PfehUc73gRWSRTPXvqUhUHVywdAxcfEtYmmyAo"),
        nonce = 255,
        poolTokenMint = PublicKey("4X1oYoFWYtLebk51zuh889r1WFLe8Z9qWApj87hQMfML"),
        tokenAccountA = PublicKey("9SxzphwrrDVDkwkyvmtag9NLgpjSkTw35cRwg9rLMYWk"),
        tokenAccountB = PublicKey("6Y9VyEYHgxVahiixzphNh4HAywpab9zVoD4S8q1sfuL8"),
        feeAccount = PublicKey("43ViAbUVujnYtJyzGP4AhabMYCbLsExenT3WKsZjqJ7N"),
        feeNumerator = BigInteger.valueOf(25L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(5L),
        ownerTradeFeeDenominator = BigInteger.valueOf(10000L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "SOL",
        tokenBName = "NINJA",
        curveType = "ConstantProduct",
        amp = null,
        programVersion = 2,
        deprecated = false,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(19449398641374),
            decimals = 9
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(1796762444462),
            decimals = 6
        )
    )

    val socnUSDCAquafarmsPool = OrcaPool(
        account = PublicKey("6Gh36sNXrGWYiWr999d9iZtqgnipJbWuBohyHBN1cJpS"),
        authority = PublicKey("GXWEpRURaQZ9E62Q23EreTUfBy4hfemXgWFUWcg7YFgv"),
        nonce = 255,
        poolTokenMint = PublicKey("Dkr8B675PGnNwEr9vTKXznjjHke5454EQdz3iaSbparB"),
        tokenAccountA = PublicKey("7xs9QsrxQDVoWQ8LQ8VsVjfPKBrPGjvg8ZhaLnU1i2VR"),
        tokenAccountB = PublicKey("FZFJK64Fk1t619zmVPqCx8Uy29zJ3WuvjWitCQuxXRo3"),
        feeAccount = PublicKey("HsC1Jo38jK3EpoNAkxfoUJhQVPa28anewZpLfeouUNk7"),
        feeNumerator = BigInteger.valueOf(25L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(5L),
        ownerTradeFeeDenominator = BigInteger.valueOf(10000L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "SOCN",
        tokenBName = "USDC",
        curveType = "ConstantProduct",
        amp = null,
        programVersion = 2,
        deprecated = false,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(3477492966425),
            decimals = 9
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(554749837968),
            decimals = 6
        )
    )

    val usdcMNGOAquafarmsPool = OrcaPool(
        account = PublicKey("Hk9ZCvmqVT1FHNkWJMrtMkkVnH1WqssWPAvmio5Vs3se"),
        authority = PublicKey("5RyiYaHFDVupwnwxzKCRk7JY1CKhsczZXefMs3UUmx4Z"),
        nonce = 254,
        poolTokenMint = PublicKey("H9yC7jDng974WwcU4kTGs7BKf7nBNswpdsP5bzbdXjib"),
        tokenAccountA = PublicKey("5yMoAhjfFaCPwEwKM2VeFFh2iBs5mHWLTJ4LuqZifsgN"),

        tokenAccountB = PublicKey("J8bQnhcNyixFGBskQoJ2aSPXPWjvSzaaxF4YPs96XHDJ"),
        feeAccount = PublicKey("FWKcKaMfaVezLRFr946MdgmpTZHG4A2GgqehAxjTyDAB"),
        feeNumerator = BigInteger.valueOf(25L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(5L),
        ownerTradeFeeDenominator = BigInteger.valueOf(10000L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "USDC",
        tokenBName = "MNGO",
        curveType = "ConstantProduct",
        amp = null,
        programVersion = 2,
        deprecated = false,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(455018515099),
            decimals = 6
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(1772765337297),
            decimals = 6
        )
    )

    val solUSDCAquafarmsPool = OrcaPool(
        account = PublicKey("EGZ7tiLeH62TPV1gL8WwbXGzEPa9zmcpVnnkPKKnrE2U"),
        authority = PublicKey("JU8kmKzDHF9sXWsnoznaFDFezLsE5uomX2JkRMbmsQP"),
        nonce = 252,
        poolTokenMint = PublicKey("APDFRM3HMr8CAGXwKHiu2f5ePSpaiEJhaURwhsRrUUt9"),
        tokenAccountA = PublicKey("ANP74VNsHwSrq9uUSjiSNyNWvf6ZPrKTmE4gHoNd13Lg"),
        tokenAccountB = PublicKey("75HgnSvXbWKZBpZHveX68ZzAhDqMzNDS29X6BGLtxMo1"),
        feeAccount = PublicKey("8JnSiuvQq3BVuCU3n4DrSTw9chBSPvEMswrhtifVkr1o"),
        feeNumerator = BigInteger.valueOf(25L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(5L),
        ownerTradeFeeDenominator = BigInteger.valueOf(10000L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "SOL",
        tokenBName = "USDC",
        curveType = "ConstantProduct",
        amp = null,
        programVersion = 2,
        deprecated = false,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(229589261208922),
            decimals = 9
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(36310284213312),
            decimals = 6
        )
    )

    val usdcSLIMAquafarmsPool = OrcaPool(
        account = PublicKey("8JPid6GtND2tU3A7x7GDfPPEWwS36rMtzF7YoHU44UoA"),
        authority = PublicKey("749y4fXb9SzqmrLEetQdui5iDucnNiMgCJ2uzc3y7cou"),
        nonce = 255,
        poolTokenMint = PublicKey("BVWwyiHVHZQMPHsiW7dZH7bnBVKmbxdeEjWqVRciHCyo"),
        tokenAccountA = PublicKey("EFYW6YEiCGpavuMPS1zoXhgfNkPisWkQ3bQz1b4UfKek"),
        tokenAccountB = PublicKey("ErcxwkPgLdyoVL6j2SsekZ5iysPZEDRGfAggh282kQb8"),
        feeAccount = PublicKey("E6aTzkZKdCECgpDtBZtVpqiGjxRDSAFh1SC9CdSoVK7a"),
        feeNumerator = BigInteger.valueOf(25L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(5L),
        ownerTradeFeeDenominator = BigInteger.valueOf(10000L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "USDC",
        tokenBName = "SLIM",
        curveType = "ConstantProduct",
        amp = null,
        programVersion = 2,
        deprecated = false,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(423867437266),
            decimals = 6
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(200571837175),
            decimals = 6
        )
    )

    val solUSDCPool = OrcaPool(
        account = PublicKey("6fTRDD7sYxCN7oyoSQaN1AWC3P2m8A6gVZzGrpej9DvL"),
        authority = PublicKey("B52XRdfTsh8iUGbGEBJLHyDMjhaTW8cAFCmpASGJtnNK"),
        nonce = 253,
        poolTokenMint = PublicKey("ECFcUGwHHMaZynAQpqRHkYeTBnS5GnPWZywM8aggcs3A"),
        tokenAccountA = PublicKey("FdiTt7XQ94fGkgorywN1GuXqQzmURHCDgYtUutWRcy4q"),
        tokenAccountB = PublicKey("7VcwKUtdKnvcgNhZt5BQHsbPrXLxhdVomsgrr7k2N5P5"),
        feeAccount = PublicKey("4pdzKqAGd1WbXn1L4UpY4r58irTfjFYMYNudBrqbQaYJ"),
        feeNumerator = BigInteger.valueOf(30L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(0L),
        ownerTradeFeeDenominator = BigInteger.valueOf(0L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "SOL",
        tokenBName = "USDC",
        curveType = "ConstantProduct",
        amp = null,
        programVersion = null,
        deprecated = true,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(367286039883),
            decimals = 9
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(57698207990),
            decimals = 6
        )
    )

    val usdcKUROAquafarmsPool = OrcaPool(
        account = PublicKey("HdeYs4bpJKN2oTb7PHxbqq4kzKiLr772A5N2gWjY57ZT"),
        authority = PublicKey("2KRcBDQJWEPygxcMMFMvR6dMTVtMkJV6kbxr5e9Kdj5Q"),
        nonce = 250,
        poolTokenMint = PublicKey("DRknxb4ZFxXUTG6UJ5HupNHG1SmvBSCPzsZ1o9gAhyBi"),
        tokenAccountA = PublicKey("B252w7ZkUX4WyLUJKLEymEpRkYMqJhgv2PSj2Z2LWH34"),
        tokenAccountB = PublicKey("DBckbD9CoRBFE8WdbbnFLDz6WdDDSZ7ReEeqdjL62fpG"),
        feeAccount = PublicKey("5XuLrZqpX9gW3pJw7274EYwAft1ciTXndU4on96ERi9J"),
        feeNumerator = BigInteger.valueOf(25L),
        feeDenominator = BigInteger.valueOf(10000L),
        ownerTradeFeeNumerator = BigInteger.valueOf(5L),
        ownerTradeFeeDenominator = BigInteger.valueOf(10000L),
        ownerWithdrawFeeNumerator = BigInteger.valueOf(0L),
        ownerWithdrawFeeDenominator = BigInteger.valueOf(0L),
        hostFeeNumerator = BigInteger.valueOf(0L),
        hostFeeDenominator = BigInteger.valueOf(0L),
        tokenAName = "USDC",
        tokenBName = "KURO",
        curveType = "ConstantProduct",
        amp = null,
        programVersion = 2,
        deprecated = false,
        tokenABalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(437928692012),
            decimals = 9
        ),
        tokenBBalance = AccountBalance(
            account = PublicKey("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV"),
            amount = BigInteger.valueOf(3184945666107),
            decimals = 6
        )
    )
}
