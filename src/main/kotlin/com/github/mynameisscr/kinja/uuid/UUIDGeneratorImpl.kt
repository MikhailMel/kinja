package com.github.mynameisscr.kinja.uuid

import java.util.UUID

class UUIDGeneratorImpl : UUIDGenerator {

    override fun generate(): UUID = UUID.randomUUID()
}
