[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![JitPack](https://jitpack.io/v/mynameisscr/kinja.svg)](https://jitpack.io/#mynameisscr/kinja)

## Kinja

Kinja is a very simple and lightweight lib for dependency injection.

## Setup

```groovy
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.mynameisscr:kinja:v0.0.2")
}
```

## Usage

```kotlin
fun main() {
    val context = kinja {
        single(::ServiceAImpl)
        single(::ServiceB)
        single(::ServiceC)
    }

    context.getByClass(ServiceC::class)
        ?.print()
}

interface IServiceA {

    val str: String
}

class ServiceAImpl : IServiceA {

    override val str: String = "a"
}

class ServiceB(serviceA: IServiceA) {

    val str = "b ${serviceA.str}"
}

class ServiceC(private val serviceA: ServiceAImpl, private val serviceB: ServiceB) {

    fun print() {
        println("Hello world! ${serviceA.str} ${serviceB.str}")
    }
}
```
