package com.example.suborbital

import godot.PackedScene
import godot.Spatial
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.global.GD
import kotlin.math.pow
import kotlin.properties.Delegates

@RegisterClass
class Space: Spatial() {
	// Universal gravitational constant
	val gravitationalConstant = 6.674e-11

	private var celestialBodies: MutableList<CelestialBody> = mutableListOf()

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
		val forces = celestialBodies.map { Vector3.ZERO }.toMutableList()

		// Body interaction
		celestialBodies.forEachIndexed { i, a ->
			(i+1 until celestialBodies.count()).forEach { j ->
				val b = celestialBodies[j]
				val aToB = b.position - a.position
				val r = aToB.length
				val n = aToB.normalized

				val overlap = (a.radius + b.radius) - r
				if(overlap > 0 && a.solid && b.solid) {
					// Perfectly inelastic collision
					val mergedVelocity = ((a.velocity * a.mass) + (b.velocity * b.mass)) / (a.mass + b.mass)
					a.velocity = mergedVelocity
					b.velocity = mergedVelocity
				} else {
					// Newtonian gravity
					val forceVector = n * (
						gravitationalConstant * a.mass * b.mass / r.pow(2)
					)
					forces[i] += forceVector
					forces[j] -= forceVector
				}
			}
		}

		// Apply forces as acceleration
		celestialBodies.onEachIndexed { i, it ->
			it.velocity += timeScale * delta * forces[i] / it.mass
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

		spaceScale = 0.5 / earth.radius
		timeScale = 1000.0
		addBody(earth)
		addBody(moon.apply {
			val radius = earth.radius * 2
			position = Vector3(radius, 0.0, 0.0)
			velocity = Vector3(0.0, 0.0, earth.getCircularOrbitVelocity(radius))
		})

//		spaceScale = 0.03 / earth.radius
//		timeScale = 50000.0
//		val orbitRadius = 3.48e8
//		val orbitVelocity = 1.022e3
//		addBody(earth)
//		addBody(moon.apply {
//			position = Vector3(orbitRadius, 0.0, 0.0)
//			velocity = Vector3(0.0, orbitVelocity, 0.0)
//		})
	}
}
