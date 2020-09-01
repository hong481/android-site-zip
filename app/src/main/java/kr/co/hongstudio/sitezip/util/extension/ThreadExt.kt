package kr.co.hongstudio.sitezip.util.extension

import kotlin.concurrent.thread

fun synchronizedThread(lock: Any, block: () -> Unit): Unit = thread {
    synchronized(lock, block)
}.let { Unit }


fun wait(millis: Long, condition: () -> Boolean = { false }) {
    val startMillis = System.currentTimeMillis()
    while (true) {
        val now = System.currentTimeMillis()
        if (now - startMillis >= millis) {
            break
        }
        if (!condition()) continue
        break
    }
}