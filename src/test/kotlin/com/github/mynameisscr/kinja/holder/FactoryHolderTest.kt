package com.github.mynameisscr.kinja.holder

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FactoryHolderTest {

    private var counter: Int = 0

    @Test
    fun `success get objects`() {
        val holder = FactoryHolder<Int>(
            func = this::testFun,
            paramHolders = listOf(SingleHolder(1)),
        )

        holder.get() shouldBe 1
        holder.get() shouldBe 2
        holder.get() shouldBe 3
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun testFun(inc: Int): Int {
        counter += inc
        return counter
    }
}
