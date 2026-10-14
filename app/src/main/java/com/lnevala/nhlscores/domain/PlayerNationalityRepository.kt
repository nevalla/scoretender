package com.lnevala.nhlscores.domain

interface PlayerNationalityRepository {
    /** IDs of every NHL player, past or present, of the given nationality, e.g. "FIN". */
    suspend fun playerIds(nationalityCode: String): Set<Long>
}
