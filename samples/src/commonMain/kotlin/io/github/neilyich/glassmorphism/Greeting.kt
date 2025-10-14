package io.github.neilyich.glassmorphism

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return platform.name
    }
}