/*
A replacement of Godot's native Vector3 class.
Godot's Vector3 class uses Floats, and generally we cannot be sure of behaviour at extreme values. This replacement
class uses Double, implements any useful math, and allows easy conversion to and from Godot's native type.
 */

package com.example.suborbital

import kotlin.math.sqrt
import kotlin.math.pow

data class Vector3(var x: Double, var y: Double, var z: Double) {
    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)
    }

    fun toGodot() = godot.core.Vector3(x, y, z)

    operator fun plus(v: Vector3) = Vector3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector3) = Vector3(x - v.x, y - v.y, z - v.z)
    operator fun unaryMinus() = Vector3(-x, -y, -z)

    operator fun times(s: Double) = Vector3(x * s, y * s, z * s)
    operator fun div(s: Double) = Vector3(x / s, y / s, z / s)

    fun length() = sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    fun normalized() = this / length()
    fun dot(v: Vector3) = x * v.x + y * v.y + z * v.z
}

fun godot.core.Vector3.toKotlin() = Vector3(x, y, z)
operator fun Double.times(v: Vector3) = v * this
