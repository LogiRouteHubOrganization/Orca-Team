package core.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

data class AppDispatchers(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher
)