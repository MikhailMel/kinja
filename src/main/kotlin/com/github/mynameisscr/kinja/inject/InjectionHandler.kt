package com.github.mynameisscr.kinja.inject

import com.github.mynameisscr.kinja.exception.CandidateToInjectNotFoundException
import com.github.mynameisscr.kinja.exception.CircularDependencyException
import com.github.mynameisscr.kinja.holder.FactoryHolder
import com.github.mynameisscr.kinja.holder.Holder
import com.github.mynameisscr.kinja.holder.ListHolder
import com.github.mynameisscr.kinja.holder.SingleHolder
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.jvm.jvmErasure

class InjectionHandler(
    private val injectCandidates: List<InjectCandidate>,
) {

    private val actualInjectCandidates = injectCandidates.toMutableList()
    private val injectedObjects = ArrayList<InjectedObject>(injectCandidates.size)

    fun process(): List<InjectedObject> {
        injectCandidates.forEach(::create)
        return injectedObjects
    }

    private fun create(
        candidate: InjectCandidate,
        dependenciesStack: LinkedHashSet<InjectCandidate> = LinkedHashSet(),
    ): InjectedObject {
        injectedObjects.find { it.id == candidate.id }
            ?.let { return it }

        if (candidate.params.isEmpty()) {
            return createInjectedObject(candidate, emptyList())
        }

        detectCircularDependency(candidate, dependenciesStack)
        dependenciesStack.add(candidate)

        val paramHolders = getParamHolders(candidate, dependenciesStack)
        return createInjectedObject(candidate, paramHolders)
    }

    private fun createHolder(
        candidate: InjectCandidate,
        paramHolders: List<Holder<out Any>>,
    ): Holder<Any> =
        when (candidate.scope) {
            InjectScope.SINGLE -> SingleHolder(constructObj(candidate.func, paramHolders))
            InjectScope.FACTORY -> FactoryHolder(candidate.func, paramHolders)
        }

    @Suppress("SpreadOperator")
    private fun constructObj(func: KFunction<Any>, paramHolders: List<Holder<out Any>>): Any =
        func.call(
            *paramHolders.map { it.get() }
                .toTypedArray()
        )

    private fun createInjectedObject(candidate: InjectCandidate, paramHolders: List<Holder<out Any>>): InjectedObject =
        InjectedObject(
            candidate = candidate,
            holder = createHolder(candidate, paramHolders),
        ).apply {
            injectedObjects.add(this)
            actualInjectCandidates.remove(candidate)
        }

    private fun getParamHolders(
        candidate: InjectCandidate,
        dependenciesStack: LinkedHashSet<InjectCandidate>,
    ): List<Holder<out Any>> = candidate.params.map { param ->
        if (param.type.jvmErasure == List::class) {
            getListHolder(param, dependenciesStack, candidate)
        } else {
            getSingleObjectHolder(param, dependenciesStack, candidate)
        }
    }

    private fun getListHolder(
        param: KParameter,
        dependenciesStack: LinkedHashSet<InjectCandidate>,
        candidate: InjectCandidate,
    ): ListHolder<Any> {
        val genericType = param.type.arguments.first().type!!

        val objectHolders = findInjectedObjectsByType(genericType, dependenciesStack)
            .map { it.holder }
            .takeIf { it.isNotEmpty() }
            ?: throw CandidateToInjectNotFoundException(param, candidate)

        return ListHolder(objectHolders)
    }

    private fun getSingleObjectHolder(
        param: KParameter,
        dependenciesStack: LinkedHashSet<InjectCandidate>,
        candidate: InjectCandidate,
    ): Holder<Any> {
        val injectedObjects = findInjectedObjectsByType(param.type, dependenciesStack)

        return when {
            injectedObjects.isEmpty() -> throw CandidateToInjectNotFoundException(param, candidate)
            injectedObjects.size == 1 -> injectedObjects.first()
            else -> injectedObjects.find { it.name == param.name }
                ?: throw CandidateToInjectNotFoundException(param, candidate)
        }.holder
    }

    private fun findInjectedObjectsByType(
        type: KType,
        dependenciesStack: LinkedHashSet<InjectCandidate>,
    ): List<InjectedObject> {
        val injectedObjects = injectedObjects
            .filter { type.isSupertypeOf(it.type) }
            .map { it }
        val newObjects = actualInjectCandidates
            .filter { type.isSupertypeOf(it.returnType) }
            .map { create(it, dependenciesStack) }

        return injectedObjects + newObjects
    }

    private fun detectCircularDependency(
        current: InjectCandidate,
        dependenciesStack: LinkedHashSet<InjectCandidate>,
    ) {
        if (dependenciesStack.contains(current)) {
            val chain = dependenciesStack.dropWhile { it == current }
                .joinToString(
                    separator = "\n|\n",
                    prefix = "${current.name} (${current.returnType})\n|\n",
                    postfix = "\n|\n${current.name} (${current.returnType})",
                ) { "${it.name} (${it.returnType})" }
            throw CircularDependencyException(chain)
        }
    }
}
