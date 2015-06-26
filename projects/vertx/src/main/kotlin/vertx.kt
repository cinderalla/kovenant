/*
 * Copyright (c) 2015 Mark Platvoet<mplatvoet@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.komponents.kovenant.vertx

import io.vertx.core.impl.currentVertxContext
import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.DispatcherContext
import nl.komponents.kovenant.Kovenant
import io.vertx.core.impl.ContextImpl as VertxContext

/*
 PROOF OF CONCEPT implementation

 This has not been tested nor run. It just an
 implementation of an idea.
 */
fun main(args: Array<String>) {
    //configure once
    Kovenant.context = VertxKovenantContext()
}


class VertxKovenantContext() : Context {
    override val callbackContext: DispatcherContext
        get() = VertxCallbackDispatcherContext(currentVertxContext()!!)
    override val workerContext: DispatcherContext
        get() = VertxWorkerDispatcherContext(currentVertxContext()!!)




    override val multipleCompletion: (Any, Any) -> Unit
        get() = { curVal: Any, newVal: Any -> throw IllegalStateException("Value[$curVal] is set, can't override with new value[$newVal]") }
}

class VertxCallbackDispatcherContext(private val ctx: VertxContext) : DispatcherContext {
    override val dispatcher: Dispatcher
        get() = VertxCallbackDispatcher(ctx)
    override val errorHandler: (Exception) -> Unit
        get() = throw UnsupportedOperationException()

}

class VertxCallbackDispatcher(private val ctx: VertxContext) : BasicDispatcher() {
    override fun offer(task: () -> Unit): Boolean {
        ctx.runOnContext {
            task()
        }
        return true
    }

}

class VertxWorkerDispatcherContext(private val ctx: VertxContext) : DispatcherContext {
    override val dispatcher: Dispatcher
        get() = VertxWorkerDispatcher(ctx)
    override val errorHandler: (Exception) -> Unit
        get() = throw UnsupportedOperationException()

}

class VertxWorkerDispatcher(private val ctx: VertxContext) : BasicDispatcher() {
    override fun offer(task: () -> Unit): Boolean {
        ctx.executeBlocking( {
            task()
        }, null)
        return true
    }
}

abstract class BasicDispatcher : Dispatcher {
    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> = throw UnsupportedOperationException()
    override fun tryCancel(task: () -> Unit): Boolean = false
    override val terminated: Boolean
        get() = throw UnsupportedOperationException()
    override val stopped: Boolean
        get() = throw UnsupportedOperationException()
}
