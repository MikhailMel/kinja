package com.github.mynameisscr.kinja.holder

class ListHolder<T>(
    private val holders: List<Holder<T>>,
) : Holder<List<T>> {

    override fun get(): List<T> = holders.map { it.get() }
}
