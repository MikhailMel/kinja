package ru.scratty.kinja.inject

import ru.scratty.kinja.exception.CandidateToInjectNotFoundException
import ru.scratty.kinja.exception.CircularDependencyException
import ru.scratty.kinja.holder.FactoryHolder
import ru.scratty.kinja.holder.Holder
import ru.scratty.kinja.holder.SingleHolder
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSupertypeOf

class InjectionHandler(
    private val injectCandidates: List<InjectCandidate>,
) {

    private val injectedObjects = ArrayList<InjectedObject>(injectCandidates.size)

    fun process(): List<InjectedObject> {
        injectCandidates.forEach(::create)
        return injectedObjects
    }

    private fun create(
        candidate: InjectCandidate,
        dependenciesStack: LinkedHashSet<InjectCandidate> = LinkedHashSet(),
    ): InjectedObject {
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
        paramHolders: List<Holder<Any>>,
    ): Holder<Any> =
        when (candidate.scope) {
            InjectScope.SINGLE -> SingleHolder(constructObj(candidate.func, paramHolders))
            InjectScope.FACTORY -> FactoryHolder(candidate.func, paramHolders)
        }

    @Suppress("SpreadOperator")
    private fun constructObj(func: KFunction<Any>, paramHolders: List<Holder<Any>>): Any =
        func.call(
            *paramHolders.map { it.get() }
                .toTypedArray()
        )

    private fun createInjectedObject(candidate: InjectCandidate, paramHolders: List<Holder<Any>>): InjectedObject =
        InjectedObject(
            candidate = candidate,
            holder = createHolder(candidate, paramHolders),
        ).apply { injectedObjects.add(this) }

    private fun getParamHolders(
        candidate: InjectCandidate,
        dependenciesStack: LinkedHashSet<InjectCandidate>,
    ): List<Holder<Any>> = candidate.params.map { param ->
        findInjectedObjectForParam(param)?.holder
            ?: findCandidateToCreate(param)?.let { create(it, dependenciesStack).holder }
            ?: throw CandidateToInjectNotFoundException(param, candidate)
    }

    private fun findInjectedObjectForParam(param: KParameter): InjectedObject? {
        val foundObjects = injectedObjects
            .filter { param.type.isSupertypeOf(it.type) }
            .map { it }

        return when {
            foundObjects.isEmpty() -> null
            foundObjects.size == 1 -> foundObjects[0]
            else -> foundObjects.find { it.name == param.name }
        }
    }

    private fun findCandidateToCreate(param: KParameter): InjectCandidate? =
        injectCandidates.find { param.type.isSupertypeOf(it.returnType) }

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
