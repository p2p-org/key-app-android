// package org.p2p.wallet.swap.orca
//
// import android.content.Context
// import androidx.test.core.app.ApplicationProvider
// import org.p2p.wallet.R
// import org.p2p.wallet.swap.api.InternalWebApi
// import org.p2p.wallet.swap.serum.utils.RetrofitBuilder
// import kotlinx.coroutines.runBlocking
// import org.junit.Assert.assertEquals
// import org.junit.Assert.assertNotEquals
// import org.junit.Before
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.robolectric.RobolectricTestRunner
//
// @RunWith(RobolectricTestRunner::class)
// class OrcaSwapAPIClientTests {
//
//    private lateinit var context: Context
//
//    private lateinit var api: InternalWebApi
//
//    @Before
//    fun setUp() {
//        context = ApplicationProvider.getApplicationContext()
//        api = RetrofitBuilder.getRetrofit(context.getString(R.string.p2pWebBaseUrl)).create(InternalWebApi::class.java)
//    }
//
//    @Test
//    fun testRetrievingTokens() = runBlocking {
//        val tokens = api.loadTokens("mainnet").values
//        assertNotEquals(0, tokens.size)
//    }
//
//    @Test
//    fun testRetrievingAquafarms() = runBlocking {
//        val aquafarms = api.loadAquafarms("mainnet").values
//        assertNotEquals(0, aquafarms.size)
//    }
//
//    @Test
//    fun testRetrievingPools() = runBlocking {
//        val pools = api.loadPools("mainnet").values
//        assertNotEquals(0, pools.size)
//    }
//
//    @Test
//    fun testRetrievingProgramId() = runBlocking {
//        val programId = api.loadProgramId("mainnet")
//        assertEquals("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA", programId.token)
//    }
// }
