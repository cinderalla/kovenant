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
 * THE SOFTWARE.
 */
package nl.mplatvoet.komponents.kovenant

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

public class LinkedRingBuffer<E : Any>() {
    private volatile var enqueue: MutableNode<E?>
    private volatile var dequeue: MutableNode<E?>
    private val size = AtomicInteger(0)

    private val intermediate = Any()

    init {
        val node = MutableNode<E?>()
        enqueue = node
        dequeue = node
    }

    public fun add(value: E) {
        while (true) {
            val head = enqueue
            if (head.value.compareAndSet(null, value)) {
                val next = head.next
                if (next == dequeue) {
                    val nextEnqueue = MutableNode<E?>()
                    nextEnqueue.next = next
                    head.next = nextEnqueue
                    enqueue = nextEnqueue
                } else {
                    enqueue = next
                }
                size.incrementAndGet()
            }
        }
    }

    suppress("UNCHECKED_CAST")
    public fun poll(): E? {
        while (true) {
            val tail = dequeue
            val tailVal = tail.value.get()
            if (tailVal == null && tail == enqueue) {
                return null
            } else {
                if (tail.value.compareAndSet(tailVal, intermediate as E)) {
                    dequeue = tail.next
                    tail.value.set(null)
                    size.decrementAndGet()
                    return tailVal
                }
            }
        }
    }

    public fun size(): Int = size.get()

    private class MutableNode<E>() {
        volatile var next: MutableNode<E> = this
        val value = AtomicReference<E>()
    }
}

