package org.p2p.token.service.database.mapper

import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import org.p2p.core.utils.Constants
import org.p2p.token.service.database.entity.TokenServicePriceEntity
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.utils.assertThat

class TokenServiceDatabaseMapperTest {
    private lateinit var mapper: TokenServiceDatabaseMapper

    @Before
    fun setUp() {
        mapper = TokenServiceDatabaseMapper()
    }

    @Test
    fun `GIVEN valid entity WHEN map THEN return correct domain model`() {
        // GIVEN
        val usdRate = BigDecimal.TEN
        val network = TokenServiceNetwork.SOLANA
        val entity = TokenServicePriceEntity(
            tokenAddress = Constants.SOL_MINT,
            networkChainName = network.networkName,
            usdRate = usdRate
        )
        // WHEN
        val actualDomainModel: TokenServicePrice = mapper.fromEntity(entity)

        // THEN
        val expectedDomainModel = TokenServicePrice(
            address = Constants.SOL_MINT,
            rate = TokenRate(usd = usdRate),
            network = network
        )
        actualDomainModel.assertThat()
            .isDataClassEqualTo(expectedDomainModel)
    }

    @Test
    fun `GIVEN valid entity with unknown network WHEN map THEN return domain model with solana`() {
        // GIVEN
        val entity = TokenServicePriceEntity(
            tokenAddress = Constants.SOL_MINT,
            networkChainName = "unknown chain name",
            usdRate = BigDecimal.TEN
        )
        // WHEN
        val actualDomainModel: TokenServicePrice = mapper.fromEntity(entity)

        // THEN
        actualDomainModel.assertThat()
            .prop(TokenServicePrice::network)
            .isEqualTo(TokenServiceNetwork.SOLANA)
    }

    @Test
    fun `GIVEN valid domain with rate WHEN map THEN return correct entity`() {
        // GIVEN
        val usdRate = BigDecimal.TEN
        val network = TokenServiceNetwork.SOLANA
        val domain = TokenServicePrice(
            address = Constants.SOL_MINT,
            network = network,
            rate = TokenRate(usd = usdRate)
        )
        // WHEN
        val actualEntityModel: TokenServicePriceEntity? = mapper.toEntity(domain)

        // THEN
        val expectedEntityModel = TokenServicePriceEntity(
            tokenAddress = Constants.SOL_MINT,
            usdRate = usdRate,
            networkChainName = network.networkName
        )
        actualEntityModel.assertThat()
            .isNotNull()
            .isDataClassEqualTo(expectedEntityModel)
    }

    @Test
    fun `GIVEN valid domain no rate WHEN map THEN return null`() {
        // GIVEN
        val network = TokenServiceNetwork.SOLANA
        val domain = TokenServicePrice(
            address = Constants.SOL_MINT,
            network = network,
            rate = TokenRate(usd = null)
        )
        // WHEN
        val actualEntityModel: TokenServicePriceEntity? = mapper.toEntity(domain)

        // THEN
        actualEntityModel.assertThat()
            .isNull()
    }
}
