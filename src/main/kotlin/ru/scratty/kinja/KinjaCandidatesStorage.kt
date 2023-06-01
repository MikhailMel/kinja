package ru.scratty.kinja

import ru.scratty.kinja.exception.CandidateIsAlreadyExistsException
import ru.scratty.kinja.exception.CandidateWithoutNameException
import ru.scratty.kinja.inject.InjectCandidate
import ru.scratty.kinja.inject.InjectScope
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType

class KinjaCandidatesStorage {

    private val candidates = LinkedList<InjectCandidate>()

    fun single(func: KFunction<Any>, block: CandidateProperties.() -> Unit = {}) {
        addCandidate(InjectScope.SINGLE, block, func)
    }

    fun factory(func: KFunction<Any>, block: CandidateProperties.() -> Unit = {}) {
        addCandidate(InjectScope.FACTORY, block, func)
    }

    fun getCandidates(): List<InjectCandidate> =
        candidates.map { it.copy() }

    private fun addCandidate(scope: InjectScope, block: CandidateProperties.() -> Unit, func: KFunction<Any>) {
        val props = CandidateProperties(scope)
        props.block()

        val name = calculateName(props.name, func.returnType)
        validate(name, func.returnType)

        candidates.add(
            InjectCandidate(
                name = name,
                func = func,
                scope = props.scope,
            )
        )
    }

    private fun calculateName(propName: String?, funcReturnType: KType): String =
        propName
            ?: (funcReturnType.classifier as KClass<*>).simpleName
                ?.replaceFirstChar { it.lowercase() }
            ?: throw CandidateWithoutNameException()

    private fun validate(name: String, returnType: KType) {
        if (name.isBlank()) {
            throw CandidateWithoutNameException()
        }
        if (candidates.any { it.returnType == returnType && it.name == name }) {
            throw CandidateIsAlreadyExistsException(name, returnType)
        }
    }

    @Suppress("DataClassShouldBeImmutable")
    data class CandidateProperties(
        val scope: InjectScope,
        var name: String? = null,
    )
}
