package com.example.suborbital.physics

import com.example.suborbital.Vector3
import com.example.suborbital.Highlightable
import godot.Area
import godot.MeshInstance
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.extensions.getNodeAs
import kotlin.math.pow

/*
Any object with mass and some form of radius.
The Spatial object that this script is applied to should have a radius of 1. This will be adjusted in-game based on the
real radius and the space's scale factor.
 */
@RegisterClass
class CelestialBody : Area, Highlightable {
	@Export
	@RegisterProperty
	var massString = "0.0"
	@Export
	@RegisterProperty
	var radiusString = "0.0"

	var mass = 0.0
	var angularMass = 0.0 // AKA moment of inertia
	var radius = 0.0
	var solid = true

	var space: Space? = null
	var position = Vector3.ZERO
	var velocity = Vector3.ZERO
	var angularVelocity = Vector3.ZERO

	override val meshInstance by lazy { getNodeAs<MeshInstance>("Surface")!! }
	override var outlineMeshInstance: MeshInstance? = null
	override var highlightCounter: Int = 0

	constructor() {
		mass = massString.toDouble()
		radius = radiusString.toDouble()
	}

	constructor(
		mass: Double,
		radius: Double,
		space: Space?,
		angularMass: Double = 2/5 * mass * radius.pow(2),
		solid: Boolean = true,
	) {
		this.mass = mass
		this.radius = radius
		this.space = space?.apply {
			addBody(this@CelestialBody)
		}
		this.angularMass = angularMass
		this.solid = solid
	}

	fun setScale(scale: Double) {
		this.scale = (radius * scale).let { godot.core.Vector3(it, it, it) }
	}

	@RegisterFunction
	override fun _process(delta: Double) {
		translation = (position * (space?.spaceScale ?: 1.0)).toGodot()
	}
}
