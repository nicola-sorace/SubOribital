package com.example.suborbital.physics

import com.example.suborbital.Vector3
import com.example.suborbital.times
import com.example.suborbital.virtualreality.ARVROrigin
import godot.PackedScene
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.extensions.getNodeAs
import godot.global.GD
import kotlin.properties.Delegates

@RegisterClass
class Space: Spatial() {
	// Universal gravitational constant
	val gravitationalField = GravitationalField()
	var celestialBodies: MutableList<CelestialBody> = mutableListOf()

	val origin by lazy { getNodeAs<ARVROrigin>("ARVROrigin")!! }
	val hands by lazy { origin.hands }

	// Scale factor expressed as the displayed length of a real meter
	var spaceScale by Delegates.observable(1.0) { property, oldScale, newScale ->
		celestialBodies.onEach {
			it.setScale(newScale)
		}
	}

	// Scale factor expressed as the displayed length of a real second
	var timeScale = 1.0

	fun addBody(celestialBody: CelestialBody) {
		celestialBodies.add(celestialBody)
		celestialBody.setScale(spaceScale)
		celestialBody.space = this
		addChild(celestialBody)
	}

	fun removeBody(celestialBody: CelestialBody) {
		celestialBodies.remove(celestialBody)
		celestialBody.space = null
		removeChild(celestialBody)
	}

	fun loadCelestialBody(name: String, mass: Double, radius: Double) =
		(GD.load<PackedScene>("res://objects/celestialBodies/$name.tscn")!!.instance() as CelestialBody).apply {
			this.mass = mass
			this.radius = radius
		}

	@RegisterFunction
	override fun _physicsProcess(delta: Double) {
		// Will store the net force on each celestial body
		val forces = celestialBodies.associateWith { Vector3.ZERO }.toMutableMap()

		// Add forces
		gravitationalField.applyTo(forces, delta)
		hands.forEach {
			it.tether?.applyTo(forces, delta)
		}

		// Apply forces as acceleration
		celestialBodies.onEach { body ->
			body.velocity += timeScale * delta * forces[body]!! / body.mass
		}

		// Apply velocities
		celestialBodies.onEach {
			it.position += timeScale * delta * it.velocity
			if(it.angularVelocity.length > 0.0) {
				it.rotate(
					it.angularVelocity.normalized.toGodot(), timeScale * delta * it.angularVelocity.length
				)
			}
		}
	}

	@RegisterFunction
	override fun _ready() {
		//TODO Either fix exported values not coming through or remove them
		val earth = loadCelestialBody("Earth", 5.972e24, 6.371e6)
		val moon = loadCelestialBody("Moon", 7.34767309e22, 1.7374e6)
		val moon2 = loadCelestialBody("Moon", 7.34767309e22, 1.7374e6)
		val radius = earth.radius * 4

		with(gravitationalField) {
			spaceScale = 0.2 / earth.radius
			timeScale = 2000.0
			addBody(earth.apply {
				angularVelocity = Vector3(0.0, 7.2921150e-5, 0.0)
			})
			addBody(moon.apply {
				position = Vector3(radius, 0.0, 0.0)
				velocity = Vector3(0.0, earth.getCircularOrbitVelocity(radius), 0.0)
				name = "Moon"
			})
			addBody(moon2.apply {
				position = Vector3(0.0, radius, 0.0)
				velocity = Vector3(0.0, earth.getCircularOrbitVelocity(radius) / 1.5, 0.0)
			})
		}
	}
}
