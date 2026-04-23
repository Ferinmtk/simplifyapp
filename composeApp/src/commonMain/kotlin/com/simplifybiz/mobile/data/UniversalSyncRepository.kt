package com.simplifybiz.mobile.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class UniversalSyncRepository(
    private val client: HttpClient,
    internal val strategyDao: StrategyDao,
    internal val leadershipDao: LeadershipDao,
    internal val marketingDao: MarketingDao,
    internal val salesDao: SalesDao,
    internal val operationsDao: OperationsDao,
    internal val peopleDao: PeopleDao,
    internal val objectiveDao: ObjectiveDao,
    internal val researchAndDevelopmentDao: ResearchAndDevelopmentDao,
    internal val riskDao: RiskDao,
    internal val moneyDao: MoneyDao,
    internal val linkDao: LinkDao,

    ) {

    /**
     * Serializes concurrent sync operations.
     *
     * Without this, multiple callers (screen init, debounced persist, submit tap,
     * pull-to-refresh) can fire syncAll() in parallel. When two syncs race on the
     * push phase of the same module, both see the same local dirty state — neither
     * has a remote_id for newly-added child rows yet — so the server receives two
     * identical POST /sync bodies ~10ms apart and creates two rows for every new
     * child (systems_used, target_markets, action_steps, etc).
     *
     * Mutex.withLock serializes all sync operations app-wide on a single-flight
     * basis. Callers wait their turn rather than racing. The second caller's push
     * phase sees the remote_id assigned by the first and sends an update instead
     * of an insert.
     */
    private val syncMutex = Mutex()

    suspend fun syncAll(forcePull: Boolean = false) = syncMutex.withLock {
        syncStrategy(forcePull)
        syncLeadership(forcePull)
        syncMarketing(forcePull)
        syncSales(forcePull)
        syncOperations(forcePull)
        syncPeople(forcePull)
        syncMoney(forcePull)
        syncObjectives(forcePull)
        syncResearchAndDevelopment(forcePull)
        syncRisk(forcePull)
        syncLinks()        // pull-only, no forcePull needed
    }

    suspend fun pushLocalChanges() { syncAll(false) }
    suspend fun fetchRemoteChanges(force: Boolean = false) { syncAll(force) }

    private suspend fun syncStrategy(forcePull: Boolean) =
        syncModule<StrategyEntity>("strategy", strategyDao, forcePull)

    private suspend fun syncLeadership(forcePull: Boolean) =
        syncModule<LeadershipEntity>("leadership", leadershipDao, forcePull)

    private suspend fun syncMarketing(forcePull: Boolean) =
        syncModule<MarketingEntity>("marketing", marketingDao, forcePull)

    private suspend fun syncSales(forcePull: Boolean) =
        syncModule<SalesEntity>("sales", salesDao, forcePull)

    private suspend fun syncOperations(forcePull: Boolean) =
        syncModule<OperationsEntity>("operations", operationsDao, forcePull)

    private suspend fun syncPeople(forcePull: Boolean) =
        syncModule<PeopleEntity>("people", peopleDao, forcePull)

    private suspend fun syncMoney(forcePull: Boolean) =
        syncModule<MoneyEntity>("money", moneyDao, forcePull)

    private suspend fun syncObjectives(forcePull: Boolean) =
        syncModule<ObjectiveEntity>("objective", objectiveDao, forcePull)

    private suspend fun syncResearchAndDevelopment(forcePull: Boolean) =
        syncModule<ResearchAndDevelopmentEntity>("research_and_development", researchAndDevelopmentDao, forcePull)

    private suspend fun syncRisk(forcePull: Boolean) =
        syncModule<RiskEntity>("risk", riskDao, forcePull)


    /**
     * Links are pull-only — just fetch remote and store locally.
     * Users manage links on the web app only.
     */
    private suspend fun syncLinks() = withContext(Dispatchers.IO) {
        val remote: LinkEntity? = runCatching {
            val wrapped: ApiResponse<LinkEntity> = client.get("links").body()
            if (wrapped.success) wrapped.data else null
        }.getOrNull() ?: runCatching {
            client.get("links").body<LinkEntity>()
        }.getOrNull()

        if (remote != null) {
            linkDao.insertEntity(remote)
        }
    }

    /**
     * Standardized Synchronization Logic
     * 1. Pushes local "Dirty" changes to the server via POST /sync
     * 2. Pulls the latest remote data via GET /{type}
     * 3. Updates local DB only if local is clean (unless forced)
     */
    private suspend inline fun <reified T> syncModule(
        type: String,
        dao: BaseDao<T>,
        forcePull: Boolean
    ) where T : Any, T : Syncable = withContext(Dispatchers.IO) {

        val local = dao.getEntity()

        // Phase 1: Push local dirty data
        var justPushed = false
        if (local != null && local.isDirty) {
            val payload = local.toSyncPayloads()
            if (payload.isNotEmpty()) {
                runCatching {
                    val response: ApiResponse<List<SyncResponseItem>> = client.post("sync") {
                        setBody(payload)
                    }.body()

                    if (response.success) {
                        var updated: T = local
                        response.data.forEach { receipt ->
                            @Suppress("UNCHECKED_CAST")
                            updated = updated.updateWithSyncReceipt(receipt) as T
                        }
                        dao.insertEntity(updated)
                        justPushed = true
                    }
                }
            }
        }

        // Phase 2: Pull latest authoritative state.
        //
        // Always pulls after a successful push. POST /sync's response is a
        // slim SyncResponseItem (id, remote_id, status) that does NOT carry
        // server-assigned IDs for child rows like systems_used. If we skipped
        // the pull after push, Room's systems list would keep remoteId=null
        // for mobile-created systems, and a subsequent delete wouldn't know
        // what to put in deleted_system_remote_ids — the server's
        // preservation loop then keeps the "deleted" row alive and it comes
        // back on the next pull.
        //
        // GET /{type} returns the full entity from to_mobile_array() with
        // correct child remote_ids. Writing that to Room gives future
        // deletions the IDs they need.
        if (justPushed || forcePull || local?.isDirty != true) {
            val remote: T? = runCatching {
                val wrapped: ApiResponse<T> = client.get(type).body()
                if (wrapped.success) wrapped.data else null
            }.getOrNull() ?: runCatching {
                client.get(type).body<T>()
            }.getOrNull()

            // Phase 3: Hydrate local DB.
            //
            // Never overwrite a dirty local row with remote — not even on
            // forcePull. forcePull's purpose is "fetch server state when
            // nothing is pending", not "wipe user edits". After a successful
            // push, Phase 1 wrote isDirty=false to Room, so currentLocal
            // reads clean here and we correctly adopt the server state.
            if (remote != null) {
                val currentLocal = dao.getEntity()
                if (currentLocal == null || !currentLocal.isDirty) {
                    dao.insertEntity(remote)
                }
            }
        }
    }
}