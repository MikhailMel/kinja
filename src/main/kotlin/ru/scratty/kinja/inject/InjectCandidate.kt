package ru.scratty.kinja.inject

import kotlin.reflect.KFunction

data class InjectCandidate(
    val name: String,
    val func: KFunction<Any>,
    val scope: InjectScope,
) {

    val params = func.parameters
    val returnType = func.returnType
}
