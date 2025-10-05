package io.github.neilyich.glassmorphism

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform