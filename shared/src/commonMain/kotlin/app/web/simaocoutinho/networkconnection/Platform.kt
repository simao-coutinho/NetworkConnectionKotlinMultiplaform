package app.web.simaocoutinho.networkconnection

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform