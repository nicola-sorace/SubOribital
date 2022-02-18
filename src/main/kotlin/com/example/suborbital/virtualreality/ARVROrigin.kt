package com.example.suborbital.virtualreality

import godot.ARVRCamera
import godot.ARVROrigin
import godot.ARVRServer
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Dictionary
import godot.extensions.getNodeAs
import godot.global.GD

fun listAvailableInterfaces() {
	ARVRServer.getInterfaces().forEach { dict ->
		dict as Dictionary<*, *>
		dict.forEach {
			GD.print("KEY:", it.key)
			GD.print("VALUE:", it.value)
		}
	}
}

@RegisterClass
class ARVROrigin : ARVROrigin() {
	val camera by lazy { getNodeAs<ARVRCamera>("ARVRCamera")!! }
//	val leftController by lazy { getNodeAs<ARVRController>("LeftController")!! }
//	val rightController by lazy { getNodeAs<ARVRController>("RightController")!! }
	val movementSpeed = 2.0

	@RegisterFunction
	override fun _ready() {
//		val arvrInterface = ARVRServer.findInterface("OpenXR")
//
//		if(arvrInterface?.initialize() == true) {
//			getViewport()!!.arvr = true
//		}
	}

//	@RegisterFunction
//	override fun _physicsProcess(delta: Double) {
//		val headDirection = camera.globalTransform.basis.x
//			.let { Vector2(it.x, it.z).angle() }
//		translate(
//			(leftController.joystickVector * movementSpeed * delta)
//				.rotated(headDirection)
//				.let { Vector3(it.x, 0.0, it.y) }
//		)
//	}
}
