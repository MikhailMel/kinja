package com.github.mynameisscr.kinja

import com.github.mynameisscr.kinja.uuid.UUIDGenerator
import com.github.mynameisscr.kinja.uuid.UUIDGeneratorImpl

data class KinjaConfig(
    val uuidGenerator: UUIDGenerator,
) {

    companion object {
        val DEFAULT = KinjaConfig(
            uuidGenerator = UUIDGeneratorImpl(),
        )
    }
}
