package com.example.suborbital.physics

import com.example.suborbital.Vector3
import godot.*
import godot.core.Color
import godot.extensions.getNodeAs
import godot.global.GD

class Tether(
	var point: Vector3,
	val body: CelestialBody,
	val stiffness: Double, // Newtons per meter
	val damping: Double, // Newtons per meter per second
) : Spatial(), ForceField {
	val spaceScale get() = body.space?.spaceScale ?: 1.0
	val restLength = (point - body.position).length
	var oldExpansion = 0.0

	val tetherVisual by lazy {
		(GD.load<PackedScene>("res://objects/Tether.tscn")!!.instance() as Spatial)
			.also {
				addChild(it)
				body.space?.addChild(this)
			}
	}

	val meshInstance by lazy {
		tetherVisual.getNodeAs<MeshInstance>("MeshInstance")!!
			.apply {
				mesh = mesh!!.duplicate() as CapsuleMesh
				setSurfaceMaterial(0, getSurfaceMaterial(0)!!.duplicate() as SpatialMaterial)
			}
	}
	val capsuleMesh by lazy { meshInstance.mesh as CapsuleMesh }
	val material by lazy { meshInstance.getSurfaceMaterial(0) as SpatialMaterial }

	override fun applyTo(forces: Forces, delta: Double) {
		val line = point - body.position
		val expansion = line.length - restLength
		val expansionVelocity = (expansion - oldExpansion) / delta
		val force = line.normalized * (
			stiffness * expansion + damping * expansionVelocity
		)
		forces[body] = forces[body]!! + force
		oldExpansion = expansion

		// Adjust visual
		val length = line.length * spaceScale
		val tensionLevel = (line.length / restLength).coerceIn(0.0, 2.0) - 1.0
		tetherVisual.apply {
			translation = point.toGodot() * spaceScale
			lookAt((body.position * spaceScale).toGodot(), godot.core.Vector3.UP)
		}
		meshInstance.apply {
			translation = godot.core.Vector3(0.0, 0.0, -length/2)
		}
		capsuleMesh.apply {
			midHeight = length
			radius = 0.01 - 0.009 * tensionLevel
		}
		material.apply {
			albedoColor = Color(
				1.0 - tensionLevel.coerceAtLeast(0.0),
				1.0 + tensionLevel.coerceAtMost(0.0),
				0.0,
				0.5
			)
		}
	}
}
