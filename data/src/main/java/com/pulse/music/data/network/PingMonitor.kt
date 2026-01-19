package com.pulse.music.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject

class PingMonitor @Inject constructor() {

    fun ping(host: String): Flow<PingResult> = flow {
        try {
            val startTime = System.currentTimeMillis()
            val reachable = withContext(Dispatchers.IO) {
                InetAddress.getByName(host).isReachable(2000)
            }
            val endTime = System.currentTimeMillis()
            
            if (reachable) {
                emit(PingResult.Success(endTime - startTime))
            } else {
                emit(PingResult.Failure("Host unreachable"))
            }
        } catch (e: Exception) {
            emit(PingResult.Failure(e.message ?: "Unknown error"))
        }
    }
}

sealed class PingResult {
    data class Success(val latencyMs: Long) : PingResult()
    data class Failure(val error: String) : PingResult()
}
