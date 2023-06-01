package ru.scratty.kinja

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.scratty.kinja.exception.ContextIsAlreadyInitializedException
import ru.scratty.kinja.exception.MultipleObjectsWithTypeException
import ru.scratty.kinja.holder.SingleHolder
import ru.scratty.kinja.inject.InjectScope
import ru.scratty.kinja.inject.InjectedObject

class KinjaContextTest {

    @Test
    fun `success initialize`() {
        val injectedObject = InjectedObject(
            name = "testA",
            type = ::TestA.returnType,
            holder = SingleHolder(TestA()),
            scope = InjectScope.SINGLE,
        )

        with(KinjaContext()) {
            initialize(listOf(injectedObject))

            getByClass(TestA::class) shouldBe injectedObject.holder.get()
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
        )
        val injectedObjectB = InjectedObject(
            name = "testB",
            type = ::TestB.returnType,
            holder = SingleHolder(TestB()),
            scope = InjectScope.SINGLE,
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
        )
        val injectedObjectA2 = InjectedObject(
            name = "testA2",
            type = ::TestA.returnType,
            holder = SingleHolder(TestA()),
            scope = InjectScope.SINGLE,
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

    interface ITest

    class TestA : ITest

    class TestB : ITest
}
