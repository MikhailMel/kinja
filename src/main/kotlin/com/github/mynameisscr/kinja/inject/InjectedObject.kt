package com.github.mynameisscr.kinja.inject

import com.github.mynameisscr.kinja.holder.Holder
import java.util.UUID
import kotlin.reflect.KType

data class InjectedObject(
    val name: String,
    val type: KType,
    val holder: Holder<Any>,
    val scope: InjectScope,
    val id: UUID,
) {

    constructor(candidate: InjectCandidate, holder: Holder<Any>) : this(
        name = candidate.name,
        type = candidate.func.returnType,
        holder = holder,
        scope = candidate.scope,
        id = candidate.id,
    )
}
