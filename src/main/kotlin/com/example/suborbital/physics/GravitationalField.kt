package com.example.suborbital.physics

import kotlin.math.pow
import kotlin.math.sqrt

class GravitationalField : ForceField {
    val gravitationalConstant = 6.674e-11

    override fun applyTo(forces: Forces) {
		forces.keys.toList().getPairs().forEach { (a, b) ->
			val aToB = b.position - a.position
			val r = aToB.length
			val n = aToB.normalized

			val overlap = (a.radius + b.radius) - r
			if(overlap > 0 && a.solid && b.solid) {
				// Perfectly inelastic collision
				//TODO This should be a separate field
				val mergedVelocity = ((a.velocity * a.mass) + (b.velocity * b.mass)) / (a.mass + b.mass)
				a.velocity = mergedVelocity
				b.velocity = mergedVelocity
			} else {
				// Newtonian gravity
				val forceVector = n * (
					gravitationalConstant * a.mass * b.mass / r.pow(2)
				)
				forces[a] = forces[a]!! + forceVector
				forces[b] = forces[b]!! - forceVector
			}
		}
    }

	fun CelestialBody.getCircularOrbitVelocity(r: Double) =
		sqrt(gravitationalConstant * mass / r)
}