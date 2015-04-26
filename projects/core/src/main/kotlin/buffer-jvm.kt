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
    private val capacity = AtomicInteger(1)

    private object Marker {
        val popping = Any()
        val deleted = Any()
    }

    init {
        val node = MutableNode<E?>()
        enqueue = node
        dequeue = node
    }

    public fun offer(value: E): Boolean {
        while (true) {
            val node = enqueue

            //Retry if the current node is not empty or we fail to set a new value
            if (node.value.get() != null || !node.value.compareAndSet(null, value)) continue


            if (node.identityEquals(dequeue) || node.next.identityEquals(dequeue)) {
                val newNode = MutableNode<E?>()
                newNode.next = node.next
                node.next = newNode
                enqueue = newNode
                capacity.incrementAndGet()
            } else {
                enqueue = node
            }
            size.incrementAndGet()
            return true
        }
    }

    suppress("UNCHECKED_CAST")
    public fun poll(): E? {
        while (true) {
            val tail = dequeue
            val tailVal = tail.value.get()

            if (tailVal == null) {
                //can only happen when empty
                return null
            }

            if (tailVal == Marker.popping) {
                //value being popped by another thread, restart
                continue
            }

            if (tailVal == Marker.deleted) {
                //we came across a deleted marker. Let's clear it if we can.
                if (tail.value.compareAndSet(tailVal, Marker.popping as E)) {
                    //don't go pass the enqueue
                    if (tail != enqueue) {
                        dequeue = tail.next
                    }
                    tail.value.set(null)
                }
                //cleared or not, restart
                continue
            }

            if (tail.value.compareAndSet(tailVal, Marker.popping as E)) {
                //don't go pass the enqueue
                if (tail != enqueue) {
                    dequeue = tail.next
                }
                tail.value.set(null)
                size.decrementAndGet()
                return tailVal
            }
        }
    }

    suppress("UNCHECKED_CAST")
    public fun remove(value: E): Boolean {
        var node = dequeue
        while (true) {
            val nodeVal = node.value.get()
            if (nodeVal == value) {
                if (node.value.compareAndSet(nodeVal, Marker.deleted as E)) {
                    size.decrementAndGet()
                    return true
                }
            }
            if (node == enqueue) {
                return false
            }
            node = node.next
        }
    }

    public fun size(): Int = size.get()
    public fun capacity(): Int = capacity.get()

    private class MutableNode<E>() {
        volatile var next: MutableNode<E> = this
        val value = AtomicReference<E>()
    }
}

