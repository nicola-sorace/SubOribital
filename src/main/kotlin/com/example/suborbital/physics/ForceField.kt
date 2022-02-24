package com.example.suborbital.physics

import com.example.suborbital.Vector3

typealias Forces = MutableMap<MassBody, Vector3>

interface ForceField {
    fun applyTo(forces: Forces, delta: Double)
}

fun List<MassBody>.getPairs() =
/*
Get all possible pairings of these celestial objects
 */
    flatMapIndexed { i, a ->
        (i+1 until count()).map { j ->
            val b = this[j]
            Pair(a, b)
        }
    }

