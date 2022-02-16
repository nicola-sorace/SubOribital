package com.example.suborbital

import godot.Spatial
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.global.GD
import kotlin.math.sqrt

/*
Any object with mass and some form of radius.
The Spatial object that this script is applied to should have a radius of 1. This will be adjusted in-game based on the
real radius and the space's scale factor.
 */
@RegisterClass
class CelestialBody : Spatial {
	@Export
	@RegisterProperty
	var massString = "0.0"
	@Export
	@RegisterProperty
	var radiusString = "0.0"

	var mass = 0.0
	var radius = 0.0

	var space: Space? = null
	var position = Vector3.ZERO
	var velocity = Vector3.ZERO

	constructor() {
		mass = massString.toDouble()
		radius = radiusString.toDouble()
	}

	constructor(
		mass: Double,
		radius: Double,
		space: Space?,
	) {
		this.mass = mass
		this.radius = radius
		this.space = space?.apply {
			addBody(this@CelestialBody)
		}
	}

	fun setScale(scale: Double) {
		this.scale = (radius * scale).let { godot.core.Vector3(it, it, it) }
	}

	@RegisterFunction
	override fun _process(delta: Double) {
		translation = (position * (space?.spaceScale ?: 1.0)).toGodot()
	}

	fun getCircularOrbitVelocity(r: Double) =
		sqrt(space?.gravitationalConstant!! * mass / r)
}
