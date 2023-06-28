package com.github.mynameisscr.kinja.exception

import com.github.mynameisscr.kinja.inject.InjectCandidate
import kotlin.reflect.KParameter

class CandidateToInjectNotFoundException(param: KParameter, candidate: InjectCandidate) :
    RuntimeException("Candidate to inject as $param for ${candidate.name} (${candidate.returnType}) not found")
