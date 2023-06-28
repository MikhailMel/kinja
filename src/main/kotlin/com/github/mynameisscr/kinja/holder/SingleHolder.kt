package com.github.mynameisscr.kinja.holder

class SingleHolder<T>(private val obj: T) : Holder<T> {

    override fun get(): T = obj
}
