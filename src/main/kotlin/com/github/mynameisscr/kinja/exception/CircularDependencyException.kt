package com.github.mynameisscr.kinja.exception

class CircularDependencyException(chain: String) : RuntimeException("Detected circular dependency: $chain")
