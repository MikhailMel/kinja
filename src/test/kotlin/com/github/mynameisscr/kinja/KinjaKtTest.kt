package com.github.mynameisscr.kinja

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KinjaKtTest {

    @Test
    fun `success create context and inject dependencies`() {
        val context = kinja {
            single(::ServiceAImpl)
            single(::ServiceB)
            single(::ServiceC)
        }

        context.getByClass(ServiceC::class)?.str() shouldBe "c a b a"
    }

    internal interface IServiceA {

        fun str(): String
    }

    internal class ServiceAImpl : IServiceA {

        override fun str(): String = "a"
    }

    internal class ServiceB(private val serviceA: IServiceA) {

        fun str(): String = "b ${serviceA.str()}"
    }

    internal class ServiceC(private val serviceA: ServiceAImpl, private val serviceB: ServiceB) {

        fun str(): String = "c ${serviceA.str()} ${serviceB.str()}"
    }
}
