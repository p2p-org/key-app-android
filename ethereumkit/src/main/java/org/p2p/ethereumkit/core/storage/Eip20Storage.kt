package org.p2p.ethereumkit.core.storage

import org.p2p.ethereumkit.core.IEip20Storage
import org.p2p.ethereumkit.models.Eip20Event

class Eip20Storage(database: Eip20Database) : IEip20Storage {
    private val erc20EventDao = database.eip20EventDao()

    override fun getLastEvent(): Eip20Event? =
        erc20EventDao.getLastEip20Event()

    override fun save(events: List<Eip20Event>) {
        erc20EventDao.insertEip20Events(events)
    }

    override fun getEvents(): List<Eip20Event> =
        erc20EventDao.getEip20Events()

    override fun getEventsByHashes(hashes: List<ByteArray>): List<Eip20Event> =
        erc20EventDao.getEip20EventsByHashes(hashes)

}
