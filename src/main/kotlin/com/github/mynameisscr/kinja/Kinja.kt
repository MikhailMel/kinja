package com.github.mynameisscr.kinja

import com.github.mynameisscr.kinja.inject.InjectionHandler

fun kinja(
    config: KinjaConfig = KinjaConfig.DEFAULT,
    block: KinjaCandidatesStorage.() -> Unit,
): KinjaContext =
    KinjaContext().apply {
        val candidates = KinjaCandidatesStorage(config.uuidGenerator)
            .let {
                it.block()
                it.getCandidates()
            }

        initialize(InjectionHandler(candidates).process())
    }
