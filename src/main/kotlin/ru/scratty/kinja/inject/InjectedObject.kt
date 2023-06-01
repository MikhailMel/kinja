package ru.scratty.kinja.inject

import ru.scratty.kinja.holder.Holder
import kotlin.reflect.KType

data class InjectedObject(
    val name: String,
    val type: KType,
    val holder: Holder<Any>,
    val scope: InjectScope,
) {

    constructor(candidate: InjectCandidate, holder: Holder<Any>) : this(
        name = candidate.name,
        type = candidate.func.returnType,
        holder = holder,
        scope = candidate.scope,
    )
}
