package com.example.suborbital.physics

import com.example.suborbital.Vector3
import godot.Spatial
import kotlin.math.pow

/*
Any object with mass and some form of radius.
The Spatial object that this script is applied to should have a radius of 1. This will be adjusted in-game based on the
real radius and the space's scale factor.
 */
open class MassBody(
	var space: Space,
	val mass: Double,
	val radius: Double,
	val angularMass: Double = 2 / 5 * mass * radius.pow(2), // AKA moment of inertia
	val solid: Boolean = true,
	val emitsGravity: Boolean = true,
	val receivesGravity: Boolean = true,
) : Spatial() {
	var position = Vector3.ZERO
	var velocity = Vector3.ZERO
	var angularVelocity = Vector3.ZERO

	init {
		space.massBodies.add(this)
	}

	fun setScale(scale: Double) {
		this.scale = (radius * scale).let { godot.core.Vector3(it, it, it) }
	}

	open fun physicsProcess(delta: Double) {
		translation = (position * space.spaceScale).toGodot()
	}
}
