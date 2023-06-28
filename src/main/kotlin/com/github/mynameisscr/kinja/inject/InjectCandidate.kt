package com.github.mynameisscr.kinja.inject

import java.util.UUID
import kotlin.reflect.KFunction

data class InjectCandidate(
    val id: UUID,
    val name: String,
    val func: KFunction<Any>,
    val scope: InjectScope,
) {

    val params = func.parameters
    val returnType = func.returnType
}
