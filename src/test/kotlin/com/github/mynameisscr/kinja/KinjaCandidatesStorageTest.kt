package com.github.mynameisscr.kinja

import com.github.mynameisscr.kinja.exception.CandidateIsAlreadyExistsException
import com.github.mynameisscr.kinja.exception.CandidateWithoutNameException
import com.github.mynameisscr.kinja.inject.InjectCandidate
import com.github.mynameisscr.kinja.inject.InjectScope
import com.github.mynameisscr.kinja.uuid.UUIDGenerator
import com.github.mynameisscr.kinja.uuid.UUIDGeneratorImpl
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class KinjaCandidatesStorageTest {

    @Test
    fun `success add candidates`() {
        val uuids = generateSequence { }
            .take(8)
            .mapTo(ArrayList()) { UUID.randomUUID() }

        val uuidGenerator = mockk<UUIDGenerator>()
        every { uuidGenerator.generate() } returnsMany uuids

        val candidates = KinjaCandidatesStorage(uuidGenerator).let {
            it.single(::TestA)
            it.single(::TestA) {
                name = "customA"
            }
            it.single(::TestA) {
                name = "testB"
            }
            it.single(::TestA) {
                name = "testC"
            }
            it.factory(::TestB)
            it.factory(::TestB) {
                name = "customB"
            }
            it.single(::TestC)
            it.single(::TestC) {
                name = "customC"
            }
            it.getCandidates()
        }

        val uuidsQueue = uuids.toCollection(ArrayDeque())
        val expected = listOf(
            InjectCandidate(uuidsQueue.removeFirst(), "testA", ::TestA, InjectScope.SINGLE),
            InjectCandidate(uuidsQueue.removeFirst(), "customA", ::TestA, InjectScope.SINGLE),
            InjectCandidate(uuidsQueue.removeFirst(), "testB", ::TestA, InjectScope.SINGLE),
            InjectCandidate(uuidsQueue.removeFirst(), "testC", ::TestA, InjectScope.SINGLE),
            InjectCandidate(uuidsQueue.removeFirst(), "testB", ::TestB, InjectScope.FACTORY),
            InjectCandidate(uuidsQueue.removeFirst(), "customB", ::TestB, InjectScope.FACTORY),
            InjectCandidate(uuidsQueue.removeFirst(), "testC", ::TestC, InjectScope.SINGLE),
            InjectCandidate(uuidsQueue.removeFirst(), "customC", ::TestC, InjectScope.SINGLE),
        )

        candidates shouldContainExactly expected
    }

    @Test
    fun `empty name from properties error`() {
        assertThrows<CandidateWithoutNameException> {
            KinjaCandidatesStorage(UUIDGeneratorImpl())
                .single(::TestA) {
                    name = ""
                }
        } shouldBe CandidateWithoutNameException()
    }

    @Test
    fun `candidate is already exists error`() {
        assertThrows<CandidateIsAlreadyExistsException> {
            KinjaCandidatesStorage(UUIDGeneratorImpl()).let {
                it.single(::TestA)
                it.single(::TestA)
            }
        } shouldBe CandidateIsAlreadyExistsException("testA", ::TestA.returnType)
    }

    @Test
    fun `candidate is already exists with name from props error`() {
        assertThrows<CandidateIsAlreadyExistsException> {
            KinjaCandidatesStorage(UUIDGeneratorImpl()).let {
                it.single(::TestA) {
                    name = "customA"
                }
                it.single(::TestA) {
                    name = "customA"
                }
            }
        } shouldBe CandidateIsAlreadyExistsException("customA", ::TestA.returnType)
    }

    @Test
    fun `candidate is already exists with name from props error 2`() {
        assertThrows<CandidateIsAlreadyExistsException> {
            KinjaCandidatesStorage(UUIDGeneratorImpl()).let {
                it.single(::TestA) {
                    name = "testA"
                }
                it.single(::TestA)
            }
        } shouldBe CandidateIsAlreadyExistsException("testA", ::TestA.returnType)
    }

    internal class TestA

    internal class TestB

    internal class TestC
}
