package app.web.simaocoutinho.networkconnection.network

import io.ktor.client.* // Ktor HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized

// RequestHolder data class (can be inside NetworkRequestManager or separate)
data class RequestHolder<T>(
    val request: ExecutableRequest<T>,
    val deferredResult: CompletableDeferred<Result<Nothing>>
)

class NetworkRequestManager(
    private val httpClient: HttpClient, // Injected Ktor client
    private val applicationScope: CoroutineScope // Injected scope
) {
    private val requestChannel = Channel<RequestHolder<*>>(Channel.UNLIMITED)

    private val _queueSize = MutableStateFlow(0)
    val queueSize: StateFlow<Int> = _queueSize.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Simple counter (consider AtomicInt if high contention expected, needs KMP library like stately-concurrent-collections or kotlinx-atomicfu)
    private var currentQueueSizeInternal = 0

    init {
        startProcessor()
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun startProcessor() {
        applicationScope.launch(Dispatchers.Default.limitedParallelism(1)) { // Use appropriate common dispatcher
            for (holder in requestChannel) {
                _isProcessing.value = true
                _queueSize.value = synchronized(this as SynchronizedObject) { --currentQueueSizeInternal } // Basic synchronization

                val result = try {
                    holder.request.execute() // Executes the Ktor call defined in the request obj
                } catch (e: Exception) {
                    println("NetworkRequestManager Error: ${e.message}") // Log error
                    Result.failure<Any>(e) // Type Any needed for generic holder
                }

                // Complete the deferred for waiting requests
                // Unchecked cast is necessary here due to erasure and generic holder
                (holder.deferredResult as? CompletableDeferred<Result<Any>>)?.complete(result as Result<Any>)


                _isProcessing.value = false
                // Add delay here if needed between requests
                // delay(100)
            }
        }
    }

    /*@OptIn(InternalCoroutinesApi::class)
    suspend fun <T> submit(request: ExecutableRequest<T>) {
        val holder = RequestHolder<T>(request, null)
        _queueSize.value = synchronized(this) { ++currentQueueSizeInternal }
        requestChannel.send(holder as RequestHolder<*>) // Cast needed for channel type
    }

    @OptIn(InternalCoroutinesApi::class)
    suspend fun <T> submitAndWait(request: ExecutableRequest<T>): T {
        val deferred = CompletableDeferred<Result<T>>()
        // Unchecked cast needed when putting into the generic holder
        val holder = RequestHolder(request, deferred as CompletableDeferred<Result<Nothing>>)
        _queueSize.value = synchronized(this) { ++currentQueueSizeInternal }
        requestChannel.send(holder as RequestHolder<*>) // Cast needed for channel type

        val result = deferred.await() // Await the specific result
        return result.getOrThrow() // Throw if the result is a failure
    }*/
}