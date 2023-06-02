package ru.scratty.kinja.inject

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.scratty.kinja.exception.CandidateToInjectNotFoundException
import ru.scratty.kinja.exception.CircularDependencyException
import kotlin.reflect.full.createType
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("UseDataClass")
internal class InjectionHandlerTest {

    @Test
    fun `test inject`() {
        val candidates = listOf(
            InjectCandidate("testB", ::TestB, InjectScope.SINGLE),
            InjectCandidate("testC", ::TestC, InjectScope.SINGLE),
            InjectCandidate("testD", ::TestD, InjectScope.SINGLE),
        )

        val result = InjectionHandler(candidates)
            .process()

        val testBInstance = result.find { it.type == TestB::class.createType() }?.holder?.get()
        assertNotNull(testBInstance)
        assert(testBInstance is TestB)

        val cInstance = result.find { it.type == TestC::class.createType() }?.holder?.get()
        assertNotNull(cInstance)
        assert(cInstance is TestC)
        assertEquals(testBInstance, (cInstance as TestC).testA)

        val dInstance = result.find { it.type == TestD::class.createType() }?.holder?.get()
        assertNotNull(dInstance)
        assert(dInstance is TestD)
        assertEquals(testBInstance, (dInstance as TestD).testA)
        assertEquals(testBInstance, dInstance.testB)
        assertEquals(cInstance, dInstance.c)
    }

    @Test
    fun `error circular dependency`() {
        val candidates = listOf(
            InjectCandidate("a", ::CircularA, InjectScope.SINGLE),
            InjectCandidate("b", ::CircularB, InjectScope.SINGLE),
            InjectCandidate("c", ::CircularC, InjectScope.SINGLE),
        )

        val chain = """a (ru.scratty.kinja.inject.InjectionHandlerTest.CircularA)
|
b (ru.scratty.kinja.inject.InjectionHandlerTest.CircularB)
|
c (ru.scratty.kinja.inject.InjectionHandlerTest.CircularC)
|
a (ru.scratty.kinja.inject.InjectionHandlerTest.CircularA)"""
        val expected = CircularDependencyException(chain)
        assertThrows<CircularDependencyException> {
            InjectionHandler(candidates)
                .process()
        } shouldBe expected
    }

    @Test
    fun `error candidate to inject not found`() {
        val candidates = listOf(
            InjectCandidate("testC", ::TestC, InjectScope.SINGLE),
        )

        val expected = CandidateToInjectNotFoundException(
            param = ::TestC.parameters[0],
            candidate = candidates[0],
        )
        assertThrows<CandidateToInjectNotFoundException> {
            InjectionHandler(candidates)
                .process()
        } shouldBe expected
    }

    interface TestA

    class TestB : TestA

    class TestC(val testA: TestA)

    class TestD(val testA: TestA, val testB: TestB, val c: TestC)

    @Suppress("UNUSED_PARAMETER")
    class CircularA(b: CircularB)

    @Suppress("UNUSED_PARAMETER")
    class CircularB(c: CircularC)

    @Suppress("UNUSED_PARAMETER")
    class CircularC(a: CircularA)
}
