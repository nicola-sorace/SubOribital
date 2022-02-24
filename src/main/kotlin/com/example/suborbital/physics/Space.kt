package com.example.suborbital.physics

import com.example.suborbital.Vector3
import com.example.suborbital.times
import com.example.suborbital.virtualreality.Player
import godot.PackedScene
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.global.GD
import kotlin.properties.Delegates

@RegisterClass
class Space: Spatial() {
	var massBodies = MassBodyCollection(
		onAdd = {
			it.setScale(spaceScale)
			it.getParent()?.apply{ removeChild(it) }
			addChild(it)
		},
		onRemove = { removeChild(it) }
	)

	val player by lazy { Player(this) }

	private val gravitationalField = GravitationalField()

	// Scale factor expressed as the displayed length of a real meter
	var spaceScale: Double by Delegates.observable(1.0) { property, oldScale, newScale ->
		massBodies.onEach {
			it.setScale(newScale)
		}
	}

	// Scale factor expressed as the displayed length of a real second
	var timeScale = 1.0

	fun loadCelestialBody(name: String, mass: Double, radius: Double) =
		CelestialBody(
			this,
			GD.load<PackedScene>("res://objects/celestialBodies/$name.tscn")!!.instance() as Spatial,
			mass,
			radius
		)

	@RegisterFunction
	override fun _physicsProcess(delta: Double) {
		// Will store the net force on each celestial body
		val forces = massBodies.associateWith { Vector3.ZERO }.toMutableMap()

		// Add forces
		gravitationalField.applyTo(forces, delta)
		player.hands.forEach {
			it.tether?.applyTo(forces, delta)
		}

		// Apply forces as acceleration
		massBodies.onEach { body ->
			body.velocity += timeScale * delta * forces[body]!! / body.mass
		}

		// Apply velocities
		massBodies.onEach {
			it.position += timeScale * delta * it.velocity
			if(it.angularVelocity.length > 0.0) {
				it.rotate(
					it.angularVelocity.normalized.toGodot(), timeScale * delta * it.angularVelocity.length
				)
			}
		}

		// Run each body's physics process
		massBodies.forEach { it.physicsProcess(delta) }
	}

	@RegisterFunction
	override fun _ready() {
		with(gravitationalField) {
			val earth = loadCelestialBody("Earth", 5.972e24, 6.371e6).apply {
				angularVelocity = Vector3(0.0, 7.2921150e-5, 0.0)
			}
			val radius = earth.radius * 4
			val moon = loadCelestialBody("Moon", 7.34767309e22, 1.7374e6).apply {
				position = Vector3(radius, 0.0, 0.0)
				velocity = Vector3(0.0, earth.getCircularOrbitVelocity(radius), 0.0)
				name = "Moon"
			}
			val moon2 = loadCelestialBody("Moon", 7.34767309e22, 1.7374e6).apply {
				position = Vector3(0.0, radius, 0.0)
				velocity = Vector3(0.0, earth.getCircularOrbitVelocity(radius) / 1.5, 0.0)
			}

			player.apply {
				position = Vector3(0.0, 0.0, radius * 2)
			}

			spaceScale = 0.2 / earth.radius
			timeScale = 2000.0
		}
	}
}
