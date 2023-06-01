package ru.scratty.kinja.exception

class CircularDependencyException(chain: String) : RuntimeException("Detected circular dependency: $chain")
