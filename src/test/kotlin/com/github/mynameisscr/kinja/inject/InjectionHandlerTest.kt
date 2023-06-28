package com.github.mynameisscr.kinja.inject

import com.github.mynameisscr.kinja.exception.CandidateToInjectNotFoundException
import com.github.mynameisscr.kinja.exception.CircularDependencyException
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.reflect.full.createType
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@Suppress("UseDataClass")
internal class InjectionHandlerTest {

    @Test
    fun `success inject`() {
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "testB", ::TestB, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "testC", ::TestC, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "testD", ::TestD, InjectScope.SINGLE),
        ).shuffled()

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
    fun `success inject factory`() {
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "testB", ::TestB, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "testC", ::TestC, InjectScope.FACTORY),
        ).shuffled()

        val result = InjectionHandler(candidates)
            .process()

        val testBInstance = result.find { it.type == TestB::class.createType() }?.holder?.get()
        assertNotNull(testBInstance)
        assert(testBInstance is TestB)

        val cInstance1 = result.find { it.type == TestC::class.createType() }?.holder?.get()
        assertNotNull(cInstance1)
        assert(cInstance1 is TestC)
        assertEquals(testBInstance, (cInstance1 as TestC).testA)

        val cInstance2 = result.find { it.type == TestC::class.createType() }?.holder?.get()
        assertNotNull(cInstance2)
        assert(cInstance2 is TestC)
        assertEquals(testBInstance, (cInstance2 as TestC).testA)

        assertNotEquals(cInstance1, cInstance2)
    }

    @Test
    fun `success list inject`() {
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "testB", ::TestB, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "customB", ::TestB, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "TestE", ::TestE, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "testF", ::TestF, InjectScope.SINGLE),
        ).shuffled()

        val result = InjectionHandler(candidates)
            .process()

        val testFInstance = result.find { it.type == TestF::class.createType() }?.holder?.get()
        assertNotNull(testFInstance)
        assert(testFInstance is TestF)

        assertEquals(2, (testFInstance as TestF).listA.count { it is TestB })
        assertEquals(1, testFInstance.listA.count { it is TestE })

        assertEquals(2, testFInstance.listB.size)
    }

    @Test
    fun `success inject by name`() {
        val expectedTestA = InjectCandidate(UUID.randomUUID(), "testA", ::TestB, InjectScope.SINGLE)
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "testE", ::TestE, InjectScope.SINGLE),
            expectedTestA,
            InjectCandidate(UUID.randomUUID(), "TestC", ::TestC, InjectScope.SINGLE),
        ).shuffled()

        val result = InjectionHandler(candidates)
            .process()

        val testCInstance = result.find { it.type == TestC::class.createType() }?.holder?.get()
        assertNotNull(testCInstance)
        assert(testCInstance is TestC)
        assert((testCInstance as TestC).testA is TestB)
    }

    @Test
    fun `error circular dependency`() {
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "a", ::CircularA, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "b", ::CircularB, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "c", ::CircularC, InjectScope.SINGLE),
        )

        val chain = """a (com.github.mynameisscr.kinja.inject.InjectionHandlerTest.CircularA)
|
b (com.github.mynameisscr.kinja.inject.InjectionHandlerTest.CircularB)
|
c (com.github.mynameisscr.kinja.inject.InjectionHandlerTest.CircularC)
|
a (com.github.mynameisscr.kinja.inject.InjectionHandlerTest.CircularA)"""
        val expected = CircularDependencyException(chain)
        assertThrows<CircularDependencyException> {
            InjectionHandler(candidates)
                .process()
        } shouldBe expected
    }

    @Test
    fun `error candidate to inject not found for single object`() {
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "testC", ::TestC, InjectScope.SINGLE),
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

    @Test
    fun `error candidate to inject not found by name for single object`() {
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "b1", ::TestB, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "b2", ::TestB, InjectScope.SINGLE),
            InjectCandidate(UUID.randomUUID(), "testC", ::TestC, InjectScope.SINGLE),
        )

        val expected = CandidateToInjectNotFoundException(
            param = ::TestC.parameters[0],
            candidate = candidates[2],
        )
        assertThrows<CandidateToInjectNotFoundException> {
            InjectionHandler(candidates)
                .process()
        } shouldBe expected
    }

    @Test
    fun `error candidate to inject not found for list inject`() {
        val candidates = listOf(
            InjectCandidate(UUID.randomUUID(), "testF", ::TestF, InjectScope.SINGLE),
        )

        val expected = CandidateToInjectNotFoundException(
            param = ::TestF.parameters[0],
            candidate = candidates[0],
        )
        assertThrows<CandidateToInjectNotFoundException> {
            InjectionHandler(candidates)
                .process()
        } shouldBe expected
    }

    interface TestA

    internal class TestB : TestA

    internal class TestC(val testA: TestA)

    internal class TestD(val testA: TestA, val testB: TestB, val c: TestC)

    internal class TestE : TestA

    internal class TestF(val listA: List<TestA>, val listB: List<TestB>)

    @Suppress("UNUSED_PARAMETER")
    internal class CircularA(b: CircularB)

    @Suppress("UNUSED_PARAMETER")
    internal class CircularB(c: CircularC)

    @Suppress("UNUSED_PARAMETER")
    internal class CircularC(a: CircularA)
}
