package org.p2p.wallet.home.api

import com.google.gson.annotations.SerializedName

data class MultiPriceResponse(
    @SerializedName("SOL")
    val SOL: SinglePriceResponse?,
    @SerializedName("BTC")
    val BTC: SinglePriceResponse?,
    @SerializedName("SRM")
    val SRM: SinglePriceResponse?,
    @SerializedName("MSRM")
    val MSRM: SinglePriceResponse?,
    @SerializedName("ETH")
    val ETH: SinglePriceResponse?,
    @SerializedName("FTT")
    val FTT: SinglePriceResponse?,
    @SerializedName("YFI")
    val YFI: SinglePriceResponse?,
    @SerializedName("LINK")
    val LINK: SinglePriceResponse?,
    @SerializedName("XRP")
    val XRP: SinglePriceResponse?,
    @SerializedName("USDT")
    val USDT: SinglePriceResponse?,
    @SerializedName("USDC")
    val USDC: SinglePriceResponse?,
    @SerializedName("WUSDC")
    val WUSDC: SinglePriceResponse?,
    @SerializedName("SUSHI")
    val SUSHI: SinglePriceResponse?,
    @SerializedName("ALEPH")
    val ALEPH: SinglePriceResponse?,
    @SerializedName("SXP")
    val SXP: SinglePriceResponse?,
    @SerializedName("HGET")
    val HGET: SinglePriceResponse?,
    @SerializedName("CREAM")
    val CREAM: SinglePriceResponse?,
    @SerializedName("UBXT")
    val UBXT: SinglePriceResponse?,
    @SerializedName("HNT")
    val HNT: SinglePriceResponse?,
    @SerializedName("FRONT")
    val FRONT: SinglePriceResponse?,
    @SerializedName("AKRO")
    val AKRO: SinglePriceResponse?,
    @SerializedName("HXRO")
    val HXRO: SinglePriceResponse?,
    @SerializedName("UNI")
    val UNI: SinglePriceResponse?,
    @SerializedName("MATH")
    val MATH: SinglePriceResponse?,
    @SerializedName("TOMO")
    val TOMO: SinglePriceResponse,
    @SerializedName("LUA")
    val LUA: SinglePriceResponse?,
    @SerializedName("KARMA")
    val KARMA: SinglePriceResponse?,
    @SerializedName("KEEP")
    val KEEP: SinglePriceResponse?,
    @SerializedName("SWAG")
    val SWAG: SinglePriceResponse?,
    @SerializedName("CEL")
    val CEL: SinglePriceResponse?,
    @SerializedName("RSR")
    val RSR: SinglePriceResponse?,
    @SerializedName("1INCH")
    val `1INCH`: SinglePriceResponse?,
    @SerializedName("GRT")
    val GRT: SinglePriceResponse?,
    @SerializedName("COMP")
    val COMP: SinglePriceResponse?,
    @SerializedName("PAXG")
    val PAXG: SinglePriceResponse?,
    @SerializedName("STRONG")
    val STRONG: SinglePriceResponse?,
    @SerializedName("FIDA")
    val FIDA: SinglePriceResponse?,
    @SerializedName("KIN")
    val KIN: SinglePriceResponse?,
    @SerializedName("MAPS")
    val MAPS: SinglePriceResponse?,
    @SerializedName("OXY")
    val OXY: SinglePriceResponse?,
    @SerializedName("BRZ")
    val BRZ: SinglePriceResponse?,
    @SerializedName("RAY")
    val RAY: SinglePriceResponse?,
    @SerializedName("PERK")
    val PERK: SinglePriceResponse?,
    @SerializedName("BTSG")
    val BTSG: SinglePriceResponse?,
    @SerializedName("BVOL")
    val BVOL: SinglePriceResponse?,
    @SerializedName("IBVOL")
    val IBVOL: SinglePriceResponse?,
    @SerializedName("AAVE")
    val AAVE: SinglePriceResponse?,
    @SerializedName("SECO")
    val SECO: SinglePriceResponse?,
    @SerializedName("SDOGE")
    val SDOGE: SinglePriceResponse?,
    @SerializedName("SAMO")
    val SAMO: SinglePriceResponse,
    @SerializedName("ISA")
    val ISA: SinglePriceResponse?,
    @SerializedName("RECO")
    val RECO: SinglePriceResponse?,
    @SerializedName("NINJA")
    val NINJA: SinglePriceResponse?,
    @SerializedName("SLIM")
    val SLIM: SinglePriceResponse?,
    @SerializedName("QUEST")
    val QUEST: SinglePriceResponse?,
    @SerializedName("SPD")
    val SPD: SinglePriceResponse?,
    @SerializedName("STEP")
    val STEP: SinglePriceResponse?,
    @SerializedName("MEDIA")
    val MEDIA: SinglePriceResponse?,
    @SerializedName("SLOCK")
    val SLOCK: SinglePriceResponse?,
    @SerializedName("ROPE")
    val ROPE: SinglePriceResponse?,
    @SerializedName("DOCE")
    val DOCE: SinglePriceResponse?,
    @SerializedName("MCAPS")
    val MCAPS: SinglePriceResponse?,
    @SerializedName("COPE")
    val COPE: SinglePriceResponse?,
    @SerializedName("XCOPE")
    val XCOPE: SinglePriceResponse?,
    @SerializedName("AAPE")
    val AAPE: SinglePriceResponse?,
    @SerializedName("oDOP")
    val oDOP: SinglePriceResponse?,
    @SerializedName("RAYPOOL")
    val RAYPOOL: SinglePriceResponse?,
    @SerializedName("PERP")
    val PERP: SinglePriceResponse?,
    @SerializedName("OXYPOOL")
    val OXYPOOL: SinglePriceResponse?,
    @SerializedName("MAPSPOOL")
    val MAPSPOOL: SinglePriceResponse?,
    @SerializedName("LQID")
    val LQID: SinglePriceResponse?,
    @SerializedName("TRYB")
    val TRYB: SinglePriceResponse?,
    @SerializedName("HOLY")
    val HOLY: SinglePriceResponse?,
    @SerializedName("ENTROPPP")
    val ENTROPPP: SinglePriceResponse?,
    @SerializedName("FARM")
    val FARM: SinglePriceResponse?,
    @SerializedName("NOPE")
    val NOPE: SinglePriceResponse?,
    @SerializedName("STNK")
    val STNK: SinglePriceResponse?,
    @SerializedName("MEAL")
    val MEAL: SinglePriceResponse?,
    @SerializedName("SNY")
    val SNY: SinglePriceResponse?,
    @SerializedName("FROG")
    val FROG: SinglePriceResponse?,
    @SerializedName("CRT")
    val CRT: SinglePriceResponse?,
    @SerializedName("SKEM")
    val SKEM: SinglePriceResponse?,
    @SerializedName("SOLAPE")
    val SOLAPE: SinglePriceResponse?,
    @SerializedName("WOOF")
    val WOOF: SinglePriceResponse?,
    @SerializedName("MER")
    val MER: SinglePriceResponse?,
    @SerializedName("ACMN")
    val ACMN: SinglePriceResponse?,
    @SerializedName("MUDLEY")
    val MUDLEY: SinglePriceResponse?,
    @SerializedName("LOTTO")
    val LOTTO: SinglePriceResponse?,
    @SerializedName("BOLE")
    val BOLE: SinglePriceResponse?,
    @SerializedName("mBRZ")
    val mBRZ: SinglePriceResponse?,
    @SerializedName("mPLAT")
    val mPLAT: SinglePriceResponse?,
    @SerializedName("mDIAM")
    val mDIAM: SinglePriceResponse?,
    @SerializedName("APYS")
    val APYS: SinglePriceResponse?,
    @SerializedName("MIT")
    val MIT: SinglePriceResponse?,
    @SerializedName("PAD")
    val PAD: SinglePriceResponse?,
    @SerializedName("SHBL")
    val SHBL: SinglePriceResponse?,
    @SerializedName("AUSS")
    val AUSS: SinglePriceResponse?,
    @SerializedName("TULIP")
    val TULIP: SinglePriceResponse?,
    @SerializedName("JPYC")
    val JPYC: SinglePriceResponse?,
    @SerializedName("TYNA")
    val TYNA: SinglePriceResponse?,
    @SerializedName("ARDX")
    val ARDX: SinglePriceResponse?,
    @SerializedName("SSHIB")
    val SSHIB: SinglePriceResponse?,
    @SerializedName("SGI")
    val SGI: SinglePriceResponse?,
    @SerializedName("SOLT")
    val SOLT: SinglePriceResponse?,
    @SerializedName("KEKW")
    val KEKW: SinglePriceResponse?,
    @SerializedName("LOOP")
    val LOOP: SinglePriceResponse?,
    @SerializedName("BDE")
    val BDE: SinglePriceResponse?,
    @SerializedName("DWT")
    val DWT: SinglePriceResponse?,
    @SerializedName("DOGA")
    val DOGA: SinglePriceResponse?,
    @SerializedName("CHEEMS")
    val CHEEMS: SinglePriceResponse?,
    @SerializedName("SBFC")
    val SBFC: SinglePriceResponse?,
    @SerializedName("ECOP")
    val ECOP: SinglePriceResponse?,
    @SerializedName("CATO")
    val CATO: SinglePriceResponse?,
    @SerializedName("TOM")
    val TOM: SinglePriceResponse?,
    @SerializedName("FABLE")
    val FABLE: SinglePriceResponse?,
    @SerializedName("LZD")
    val LZD: SinglePriceResponse?,
    @SerializedName("FELON")
    val FELON: SinglePriceResponse?,
    @SerializedName("SLNDN")
    val SLNDN: SinglePriceResponse?,
    @SerializedName("SOLA")
    val SOLA: SinglePriceResponse?,
    @SerializedName("MPAD")
    val MPAD: SinglePriceResponse?,
    @SerializedName("SGT")
    val SGT: SinglePriceResponse?,
    @SerializedName("SOLDOG")
    val SOLDOG: SinglePriceResponse?,
    @SerializedName("LLAMA")
    val LLAMA: SinglePriceResponse?,
    @SerializedName("BOP")
    val BOP: SinglePriceResponse?,
    @SerializedName("MOLAMON")
    val MOLAMON: SinglePriceResponse?,
    @SerializedName("STUD")
    val STUD: SinglePriceResponse?,
    @SerializedName("RESP")
    val RESP: SinglePriceResponse?,
    @SerializedName("CHAD")
    val CHAD: SinglePriceResponse?,
    @SerializedName("DXL")
    val DXL: SinglePriceResponse?,
    @SerializedName("FUZ")
    val FUZ: SinglePriceResponse?,
    @SerializedName("STRANGE")
    val STRANGE: SinglePriceResponse?,
    @SerializedName("GRAPE")
    val GRAPE: SinglePriceResponse?,
    @SerializedName("KERMIT")
    val KERMIT: SinglePriceResponse?,
    @SerializedName("PIPANA")
    val PIPANA: SinglePriceResponse?,
    @SerializedName("CKC")
    val CKC: SinglePriceResponse?,
    @SerializedName("CHANGPENGUIN")
    val CHANGPENGUIN: SinglePriceResponse?,
    @SerializedName("KLB")
    val KLB: SinglePriceResponse?
)
