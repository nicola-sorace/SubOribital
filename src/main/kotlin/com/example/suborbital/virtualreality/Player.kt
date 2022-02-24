package com.example.suborbital.virtualreality

import com.example.suborbital.physics.MassBody
import com.example.suborbital.physics.Space
import godot.ARVRCamera
import godot.PackedScene
import godot.extensions.instanceAs
import godot.global.GD

class Player(space: Space) : MassBody(space, 70.0, 1.0, emitsGravity = false) {
    val originSingleton = GD.load<PackedScene>("res://objects/virtualReality/ARVROrigin.tscn")!!
    val origin by lazy { originSingleton.instanceAs<ARVROrigin>()!!.also { space.addChild(it) } }
    val camera by lazy { origin.getChildren().filterIsInstance<ARVRCamera>().first() }
    val hands by lazy { origin.getChildren().filterIsInstance<Hand>() }

    init {
        origin
    }

    override fun physicsProcess(delta: Double) {
        super.physicsProcess(delta)
        origin.translation = this.translation
    }
}