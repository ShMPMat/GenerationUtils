package io.tashtabash.utils


fun <E> Collection<E>.without(element: E) = filter { it != element }
