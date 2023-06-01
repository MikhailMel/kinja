package ru.scratty.kinja.exception

import ru.scratty.kinja.inject.InjectCandidate
import kotlin.reflect.KParameter

class CandidateToInjectNotFoundException(param: KParameter, candidate: InjectCandidate) :
    RuntimeException("Candidate to inject as $param for ${candidate.name} (${candidate.returnType}) not found")
