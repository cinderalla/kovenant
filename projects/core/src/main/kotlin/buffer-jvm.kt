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

import java.util.Queue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

public class LinkedRingBuffer<E : Any>() : Queue<E> {
    private volatile var enqueue: MutableNode<E?>
    private volatile var dequeue: MutableNode<E?>

    private val size = AtomicInteger(0)
    private val capacity = AtomicInteger(1)

    private object Marker {
        val popping = Any()
        val deleted = Any()
        val injecting = Any()
    }

    init {
        val node = MutableNode<E?>()
        enqueue = node
        dequeue = node
    }

    suppress("UNCHECKED_CAST")
    public override fun offer(e: E): Boolean {
        while (true) {
            val node = enqueue

            //Retry if the current node is not empty or we fail to set a new value
            //if not empty dequeue is clearing
            if (node.value != null || !node.casValue(null, Marker.injecting as E)) continue


            if (node.next identityEquals dequeue) {
                val newNode = MutableNode<E?>()
                newNode.next = node.next
                node.next = newNode
                enqueue = newNode
                capacity.incrementAndGet()
            } else {
                enqueue = node.next
            }
            node.setValue(e)
            size.incrementAndGet()
            return true
        }
    }

    suppress("UNCHECKED_CAST")
    public override fun poll(): E? {
        while (true) {
            val tail = dequeue
            val tailVal = tail.value

            if (tailVal identityEquals Marker.popping) {
                //value being popped by another thread, restart
                continue
            }
            if (tailVal identityEquals tailVal identityEquals Marker.injecting) {
                //yes we know it going to be filled soon but operation hasn't completed yet.
                //report empty.
                return null
            }

            if (tailVal == null ) {
                //null value, this can either by that to entire queue is empty
                //or that after reading the dequeue the value has been popped
                //so only report empty when this is still the dequeue node
                //and also the enqueue node
                if (tail identityEquals enqueue && tail identityEquals dequeue) {
                    return null
                }
                //otherwise restart
                continue
            }


            val deleted = tailVal identityEquals Marker.deleted
            if (tail.casValue(tailVal, Marker.popping as E)) {
                if (!tail.identityEquals(dequeue)) {
                    //we are not the dequeue need to prevent this
                    //after the cas we should be the only thread mutating
                    println("not the dequeue")
                    continue
                }

                dequeue = tail.next
                tail.setValue(null)


                if (deleted) {
                    continue
                }
                size.decrementAndGet()
                return tailVal
            }
        }
    }

    suppress("UNCHECKED_CAST")
    public override fun remove(o: Any?): Boolean {
        if (o == null) return false

        var node = dequeue
        while (true) {
            val nodeVal = node.value
            if (nodeVal == o) {
                if (node.casValue(nodeVal, Marker.deleted as E)) {
                    size.decrementAndGet()
                    return true
                }
            }
            if (node identityEquals enqueue) {
                return false
            }
            node = node.next
        }
    }

    public override fun size(): Int = size.get()
    public fun capacity(): Int = capacity.get()


    override fun add(e: E?): Boolean = if (e == null) false else offer(e)

    //TODO verify
    override fun equals(other: Any?): Boolean = other != null && this identityEquals (other)

    override fun hashCode(): Int = super.hashCode()


    override fun isEmpty(): Boolean = size.get() == 0

    override fun contains(o: Any?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsAll(c: Collection<Any?>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun iterator(): MutableIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun addAll(c: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeAll(c: Collection<Any?>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun element(): E? {
        throw UnsupportedOperationException()
    }

    override fun retainAll(c: Collection<Any?>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun remove(): E? {
        throw UnsupportedOperationException()
    }

    override fun peek(): E? {
        throw UnsupportedOperationException()
    }

    private class MutableNode<E>() {
        volatile var next: MutableNode<E> = this
        private val valueRef = AtomicReference<E>()

        val value: E get() = valueRef.get()

        fun casValue(expect: E, update: E) = valueRef.compareAndSet(expect, update)
        fun setValue(update: E) = valueRef.set(update)
    }
}

