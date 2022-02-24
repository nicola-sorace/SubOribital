package com.example.suborbital.physics

import com.example.suborbital.Vector3
import godot.*
import godot.core.Color
import godot.extensions.getNodeAs
import godot.global.GD
import kotlin.math.absoluteValue
import kotlin.random.Random

class Tether(
	var point: Vector3,
	val body: MassBody,
	val stiffness: Double, // Newtons per meter
	val damping: Double, // Newtons per meter per second
) : Spatial(), ForceField {
	val spaceScale get() = body.space.spaceScale
	val restLength = (point - body.position).length
	var oldExpansion = 0.0

	val tetherVisual by lazy {
		(GD.load<PackedScene>("res://objects/Tether.tscn")!!.instance() as Spatial)
			.also {
				addChild(it)
				body.space.addChild(this)
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

	val creaking by lazy { tetherVisual.getNodeAs<AudioStreamPlayer3D>("Creaking")!! }

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

		// Sound effects
		val speedFactor = (expansionVelocity.absoluteValue / 25e6).coerceAtMost(1.0)
		if(speedFactor > 0.05) {
			if(!creaking.playing) {
				creaking.play(Random.nextDouble(0.0, creaking.stream!!.getLength()))
			}
			creaking.unitDb = -100.0 + 100.0 * speedFactor
			creaking.pitchScale = 1.0 + tensionLevel * 0.5
		} else if(creaking.playing) {
			creaking.stop()
		}
	}
}
