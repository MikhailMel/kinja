package ru.scratty.kinja

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.scratty.kinja.exception.CandidateIsAlreadyExistsException
import ru.scratty.kinja.exception.CandidateWithoutNameException
import ru.scratty.kinja.inject.InjectCandidate
import ru.scratty.kinja.inject.InjectScope

class KinjaCandidatesStorageTest {

    @Test
    fun `success add candidates`() {
        val candidates = KinjaCandidatesStorage().let {
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

        val expected = listOf(
            InjectCandidate("testA", ::TestA, InjectScope.SINGLE),
            InjectCandidate("customA", ::TestA, InjectScope.SINGLE),
            InjectCandidate("testB", ::TestA, InjectScope.SINGLE),
            InjectCandidate("testC", ::TestA, InjectScope.SINGLE),
            InjectCandidate("testB", ::TestB, InjectScope.FACTORY),
            InjectCandidate("customB", ::TestB, InjectScope.FACTORY),
            InjectCandidate("testC", ::TestC, InjectScope.SINGLE),
            InjectCandidate("customC", ::TestC, InjectScope.SINGLE),
        )

        candidates shouldContainExactly expected
    }

    @Test
    fun `empty name from properties error`() {
        assertThrows<CandidateWithoutNameException> {
            KinjaCandidatesStorage().single(::TestA) {
                name = ""
            }
        } shouldBe CandidateWithoutNameException()
    }

    @Test
    fun `candidate is already exists error`() {
        assertThrows<CandidateIsAlreadyExistsException> {
            KinjaCandidatesStorage().let {
                it.single(::TestA)
                it.single(::TestA)
            }
        } shouldBe CandidateIsAlreadyExistsException("testA", ::TestA.returnType)
    }

    @Test
    fun `candidate is already exists with name from props error`() {
        assertThrows<CandidateIsAlreadyExistsException> {
            KinjaCandidatesStorage().let {
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
            KinjaCandidatesStorage().let {
                it.single(::TestA) {
                    name = "testA"
                }
                it.single(::TestA)
            }
        } shouldBe CandidateIsAlreadyExistsException("testA", ::TestA.returnType)
    }

    class TestA

    class TestB

    class TestC
}
