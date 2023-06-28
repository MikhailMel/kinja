package com.github.mynameisscr.kinja.exception

import kotlin.reflect.KType

class CandidateIsAlreadyExistsException(name: String, returnType: KType) :
    RuntimeException("Candidate with type '$returnType' and name '$name' is already exists")
