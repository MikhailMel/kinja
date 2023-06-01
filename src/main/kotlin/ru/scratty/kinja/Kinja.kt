package ru.scratty.kinja

import ru.scratty.kinja.inject.InjectionHandler

fun kinja(block: KinjaCandidatesStorage.() -> Unit): KinjaContext =
    KinjaContext().apply {
        val candidates = KinjaCandidatesStorage()
            .let {
                it.block()
                it.getCandidates()
            }

        initialize(InjectionHandler(candidates).process())
    }
