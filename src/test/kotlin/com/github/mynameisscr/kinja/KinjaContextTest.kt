package com.github.mynameisscr.kinja

import com.github.mynameisscr.kinja.exception.ContextIsAlreadyInitializedException
import com.github.mynameisscr.kinja.exception.ContextIsNotInitializedException
import com.github.mynameisscr.kinja.exception.MultipleObjectsWithTypeException
import com.github.mynameisscr.kinja.holder.SingleHolder
import com.github.mynameisscr.kinja.inject.InjectScope
import com.github.mynameisscr.kinja.inject.InjectedObject
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class KinjaContextTest {

    @Test
    fun `success initialize`() {
        val injectedObject = InjectedObject(
            name = "testA",
            type = ::TestA.returnType,
            holder = SingleHolder(TestA()),
            scope = InjectScope.SINGLE,
            id = UUID.randomUUID(),
        )

        with(KinjaContext()) {
            initialize(listOf(injectedObject))

            getByClass(TestA::class) shouldBe injectedObject.holder.get()
            getByClass(TestB::class) shouldBe null
        }
    }

    @Test
    fun `error context is already initialized`() {
        with(KinjaContext()) {
            initialize(emptyList())

            assertThrows<ContextIsAlreadyInitializedException> {
                initialize(emptyList())
            } shouldBe ContextIsAlreadyInitializedException()
        }
    }

    @Test
    fun `error multiple objects with type exception`() {
        val injectedObjectA = InjectedObject(
            name = "testA",
            type = ::TestA.returnType,
            holder = SingleHolder(TestA()),
            scope = InjectScope.SINGLE,
            id = UUID.randomUUID(),
        )
        val injectedObjectB = InjectedObject(
            name = "testB",
            type = ::TestB.returnType,
            holder = SingleHolder(TestB()),
            scope = InjectScope.SINGLE,
            id = UUID.randomUUID(),
        )
        val injectedObjects = listOf(
            injectedObjectA,
            injectedObjectB,
        )

        with(KinjaContext()) {
            initialize(injectedObjects)

            assertThrows<MultipleObjectsWithTypeException> {
                getByClass(ITest::class)
            } shouldBe MultipleObjectsWithTypeException(ITest::class)
        }
    }

    @Test
    fun `error multiple objects with type exception 2`() {
        val injectedObjectA1 = InjectedObject(
            name = "testA1",
            type = ::TestA.returnType,
            holder = SingleHolder(TestA()),
            scope = InjectScope.SINGLE,
            id = UUID.randomUUID(),
        )
        val injectedObjectA2 = InjectedObject(
            name = "testA2",
            type = ::TestA.returnType,
            holder = SingleHolder(TestA()),
            scope = InjectScope.SINGLE,
            id = UUID.randomUUID(),
        )
        val injectedObjects = listOf(
            injectedObjectA1,
            injectedObjectA2,
        )

        with(KinjaContext()) {
            initialize(injectedObjects)

            assertThrows<MultipleObjectsWithTypeException> {
                getByClass(TestA::class)
            } shouldBe MultipleObjectsWithTypeException(TestA::class)
        }
    }

    @Test
    fun `error not initialized`() {
        assertThrows<ContextIsNotInitializedException> {
            KinjaContext().getByClass(TestA::class)
        } shouldBe ContextIsNotInitializedException()
    }

    internal interface ITest

    internal class TestA : ITest

    internal class TestB : ITest
}
