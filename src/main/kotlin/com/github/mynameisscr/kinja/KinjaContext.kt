package com.github.mynameisscr.kinja

import com.github.mynameisscr.kinja.exception.ContextIsAlreadyInitializedException
import com.github.mynameisscr.kinja.exception.ContextIsNotInitializedException
import com.github.mynameisscr.kinja.exception.MultipleObjectsWithTypeException
import com.github.mynameisscr.kinja.inject.InjectedObject
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

class KinjaContext {

    private lateinit var injectedObjects: List<InjectedObject>

    fun initialize(injectedObjects: List<InjectedObject>) {
        if (::injectedObjects.isInitialized) {
            throw ContextIsAlreadyInitializedException()
        }
        this.injectedObjects = injectedObjects
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getByClass(clazz: KClass<T>): T? {
        checkInit()
        val type = clazz.createType()

        val foundObjects = injectedObjects
            .filter { type.isSupertypeOf(it.type) }
            .map { it }

        return when {
            foundObjects.isEmpty() -> null
            foundObjects.size == 1 -> foundObjects[0].holder.get() as T
            else -> throw MultipleObjectsWithTypeException(clazz)
        }
    }

    private fun checkInit() {
        if (!::injectedObjects.isInitialized) {
            throw ContextIsNotInitializedException()
        }
    }
}
