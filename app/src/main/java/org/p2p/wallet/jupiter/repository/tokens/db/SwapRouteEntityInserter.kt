package org.p2p.wallet.jupiter.repository.tokens.db

import com.google.gson.stream.JsonReader
import timber.log.Timber
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class SwapRouteEntityInserter(
    private val dao: SwapTokensDao,
) {
    suspend fun insertRoutes(routesObjects: JsonReader) {
        routesObjects.beginObject()
        var totalSizeInserted = 0L
        while (routesObjects.hasNext()) {
            val indexA = routesObjects.nextName().toInt()
            totalSizeInserted += insertRoutesForTokenA(indexA = indexA, routeArray = routesObjects)
        }
        routesObjects.endObject()

        Timber.i("Total inserted $totalSizeInserted")
    }

    private suspend fun insertRoutesForTokenA(indexA: Int, routeArray: JsonReader): Long = supervisorScope {
        routeArray.beginArray()

        var routesInserted: Long = 0
        val routeEntities = ArrayList<SwapTokenRouteCrossRef>(1300)
//        val db = GlobalContext.get().get<SwapDatabase>().openHelper.writableDatabase

        while (routeArray.hasNext()) {
            routeEntities += SwapTokenRouteCrossRef(
                indexOfSourceToken = indexA,
                swappableTokenIndex = routeArray.nextInt()
            )
            // for 10.01.2024 we insert around 5_153_580 rows
            // 1200 is a magic number that optimizes this process a little bit
            if (routeEntities.size >= 1200) {
                routeEntities.chunked(600)
                    .map { async { dao.insertTokenRoutes(it) } }
                    .awaitAll()
                routesInserted += routeEntities.size
                routeEntities.clear()
            }
        }
        if (routeEntities.isNotEmpty()) {
            dao.insertTokenRoutes(routeEntities)
            routesInserted += routeEntities.size
            routeEntities.clear()
        }

        routeArray.endArray()
        routesInserted
    }
}
