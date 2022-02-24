package com.example.suborbital.physics

import com.example.suborbital.Highlightable
import godot.*
import godot.extensions.getNodeAs
import godot.extensions.instanceAs
import godot.global.GD
import kotlin.math.log10

class CelestialBody(space: Space, visual: Spatial, mass: Double, radius: Double) : MassBody(space, mass, radius), Highlightable {
	val areaSingleton = GD.load<PackedScene>("res://objects/celestialBodies/CelestialBody.tscn")!!
	val area by lazy { areaSingleton.instanceAs<Area>()!!.also { addChild(it) } }

	// Highlightable interface
	override val meshInstance by lazy { getNodeAs<MeshInstance>("Surface")!! }
	override var outlineMeshInstance: MeshInstance? = null
	override var highlightCounter: Int = 0

	val hum by lazy { area.getNodeAs<AudioStreamPlayer3D>("Hum")!! }

	init {
		area
		addChild(visual)
	}

	override fun physicsProcess(delta: Double) {
		super.physicsProcess(delta)

		// Sound effects
		val speedFactor = (log10(velocity.length) / 6).coerceAtMost(1.0)
		val massFactor = (log10(mass) / 30).coerceAtMost(1.0)
		if(velocity.length > 0.05) {
			if(!hum.playing) {
				hum.play(0.0)
			}
			hum.unitDb = -100 + 100 * speedFactor * massFactor
			hum.pitchScale = 2 - 1.5 * massFactor
		} else if(hum.playing) {
			hum.stop()
		}
	}
}
