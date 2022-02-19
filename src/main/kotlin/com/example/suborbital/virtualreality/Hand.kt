package com.example.suborbital.virtualreality

import com.example.suborbital.physics.CelestialBody
import com.example.suborbital.physics.Space
import com.example.suborbital.physics.Tether
import com.example.suborbital.toKotlin
import godot.ARVRController
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.extensions.getNodeAs

@RegisterClass
class Hand : ARVRController() {
	var tether: Tether? = null
	val position get() = this@Hand.globalTransform.origin.toKotlin() / space.spaceScale

	val space by lazy { getNodeAs<Space>("/root/Space")!! }
	val moon by lazy { space.getNodeAs<CelestialBody>("Moon")!! }

	@RegisterFunction
	override fun _physicsProcess(delta: Double) {
		val trigger = getJoystickAxis(2L)
		if(trigger > 0.5) {
			tether ?: run {
				tether = Tether(
					position,
					moon,
					2e17
				)
			}
		} else {
			tether?.apply {
				queueFree()
				tether = null
			}
		}

		tether?.apply {
			point = position
		}
	}
}
