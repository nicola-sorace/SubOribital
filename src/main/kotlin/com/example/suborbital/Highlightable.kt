package com.example.suborbital

import godot.*
import godot.global.GD

val material by lazy { GD.load<Resource>("res://materials/Highlight.tres")!! as Material }

interface Highlightable {
    val meshInstance : MeshInstance
    var outlineMeshInstance : MeshInstance?
    var highlightCounter: Int

    fun genOutlineMeshInstance() = MeshInstance().apply {
        mesh = this@Highlightable.meshInstance.mesh!!.createOutline(0.1)
        setSurfaceMaterial(0, material)
    }

    fun select() {
        outlineMeshInstance = outlineMeshInstance ?: genOutlineMeshInstance().apply {
            this@Highlightable.meshInstance.addChild(this)
        }
        highlightCounter += 1
    }

    fun deselect() {
        highlightCounter -= 1
        if(highlightCounter == 0) {
            outlineMeshInstance?.queueFree()
            outlineMeshInstance = null
        }
    }
}