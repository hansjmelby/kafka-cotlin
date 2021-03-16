package com.example.demo
import kotlinx.coroutines.*

// Start main coroutine.
fun main() = runBlocking<Unit> {

    // Launch a child coroutine to do some long-running work in the background.
    // The coroutine uses a thread from the thread pool (i.e. not the "main" thread).
    val job = launch(Dispatchers.Default) {
        MyUtil.display("Coroutine start")
        var runner = Runner()
        runner.startConsumation()
        MyUtil.display("Coroutine end")
    }


    MyUtil.display("Main coroutine, delaying for 2 secs...")
    delay(5_000)

    MyUtil.display("Main coroutine, cancelling child coroutine and waiting for it to terminate...")
    job.cancelAndJoin()

    MyUtil.display("Main coroutine, all done!")
}