package com.example.suborbital.virtualreality

import com.example.suborbital.physics.CelestialBody
import com.example.suborbital.physics.Tether
import com.example.suborbital.toKotlin
import godot.ARVRController
import godot.MeshInstance
import godot.RayCast
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.extensions.getNodeAs
import kotlin.properties.Delegates.observable

@RegisterClass
class Hand : ARVRController() {
	var tether: Tether? = null

	val rayCast by lazy { getNodeAs<RayCast>("RayCast")!! }
	val aimLine by lazy { getNodeAs<MeshInstance>("AimLine")!! }

	var selectedBody: CelestialBody? by observable(null) { property, oldBody, newBody ->
		if(oldBody != newBody) {
			oldBody?.deselect()
			newBody?.select()
		}
	}

	fun getPosition(spaceScale: Double) = this@Hand.globalTransform.origin.toKotlin() / spaceScale

	@RegisterFunction
	override fun _physicsProcess(delta: Double) {
		val trigger = getJoystickAxis(2L) > 0.5
		aimLine.visible = trigger && tether == null

		if(tether == null) {
			selectedBody = (rayCast.getCollider() as? CelestialBody)
			if (trigger) {
				selectedBody?.apply {
					tether = Tether(
						getPosition(this.space?.spaceScale ?: 1.0),
						this,
						1e17
					)
					selectedBody = null
				}
			}
		} else {
			if(!trigger) {
				tether?.apply {
					queueFree()
					tether = null
				}
			}
		}

		tether?.apply {
			point = getPosition(this.body.space?.spaceScale ?: 1.0)
		}
	}
}
