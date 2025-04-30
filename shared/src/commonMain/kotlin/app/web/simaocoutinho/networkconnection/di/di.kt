package app.web.simaocoutinho.networkconnection.di

import app.web.simaocoutinho.networkconnection.network.HttpClientFactory
import app.web.simaocoutinho.networkconnection.network.NetworkRequestManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val networkConnectionModule = module {
    // Coroutine Scope (Singleton for application lifecycle)
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single { HttpClientFactory.create(get()) }

    // Network Request Manager (Singleton)
    single { NetworkRequestManager(get(), get()) } // Injects HttpClient, CoroutineScope
}