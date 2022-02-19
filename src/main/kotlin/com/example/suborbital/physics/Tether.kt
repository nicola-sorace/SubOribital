package com.example.suborbital.physics

import com.example.suborbital.Vector3
import godot.*
import godot.core.Color
import godot.extensions.getNodeAs
import godot.global.GD

class Tether(
	val point: Vector3,
	val body: CelestialBody,
	val stiffness: Double, // Newtons per meter
) : Spatial(), ForceField{
	val spaceScale get() = body.space?.spaceScale ?: 1.0
	val baseLength = (body.position - point).length

	val tetherVisual by lazy {
		(GD.load<PackedScene>("res://objects/Tether.tscn")!!.instance() as Spatial)
			.also { addChild(it) }
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

	override fun applyTo(forces: Forces) {
		val v = point - body.position
		forces[body] = forces[body]!! + v.normalized * stiffness * (v.length - baseLength)

		// Adjust visual
		val l = v.length * spaceScale
		tetherVisual.apply {
			translation = point.toGodot()
			lookAt((body.position * spaceScale).toGodot(), godot.core.Vector3.UP)
		}
		meshInstance.apply {
			translation = godot.core.Vector3(0.0, 0.0, -l/2)
		}
		capsuleMesh.apply {
			midHeight = l
		}
		material.apply {
			val tensionLevel = (v.length / baseLength).coerceIn(0.0, 2.0) - 1.0
			albedoColor = Color(
				1.0 - tensionLevel.coerceAtLeast(0.0),
				1.0 + tensionLevel.coerceAtMost(0.0),
				0.0,
				0.5
			)
		}
	}
}
