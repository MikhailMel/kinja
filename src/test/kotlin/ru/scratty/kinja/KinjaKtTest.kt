package ru.scratty.kinja

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

    interface IServiceA {

        fun str(): String
    }

    class ServiceAImpl : IServiceA {

        override fun str(): String = "a"
    }

    class ServiceB(private val serviceA: IServiceA) {

        fun str(): String = "b ${serviceA.str()}"
    }

    class ServiceC(private val serviceA: ServiceAImpl, private val serviceB: ServiceB) {

        fun str(): String = "c ${serviceA.str()} ${serviceB.str()}"
    }
}
