package com.github.mynameisscr.kinja.exception

import kotlin.reflect.KClass

class MultipleObjectsWithTypeException(clazz: KClass<*>) :
    RuntimeException("Found multiple objects by class ${clazz.qualifiedName}")
