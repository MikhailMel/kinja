package ru.scratty.kinja.holder

import kotlin.reflect.KFunction

class FactoryHolder<T>(
    private val func: KFunction<Any>,
    private val paramHolders: List<Holder<Any>>,
) : Holder<T> {

    @Suppress("UNCHECKED_CAST", "SpreadOperator")
    override fun get(): T =
        func.call(*paramHolders.map { it.get() }.toTypedArray()) as T
}
