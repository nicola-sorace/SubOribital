package com.example.suborbital.physics

class MassBodyCollection(
    val onAdd: (MassBody) -> Unit,
    val onRemove: (MassBody) -> Unit,
) : MutableSet<MassBody> {
    private val allBodies = mutableSetOf<MassBody>()
    override val size get() = allBodies.size

    private val _solidBodies = mutableSetOf<MassBody>()
    val solidBodies get() = _solidBodies.toSet()
    private val _emitGravityBodies = mutableSetOf<MassBody>()
    val emitGravityBodies get() = _emitGravityBodies.toSet()
    private val _receiveGravityBodies = mutableSetOf<MassBody>()
    val receiveGravityBodies get() = _receiveGravityBodies.toSet()

    override fun add(element: MassBody): Boolean {
        if(allBodies.contains(element))
            return false
        allBodies.add(element)
        if(element.solid)
            _solidBodies.add(element)
        if(element.emitsGravity)
            _emitGravityBodies.add(element)
        if(element.receivesGravity)
            _receiveGravityBodies.add(element)
        onAdd(element)
        return true
    }

    override fun addAll(elements: Collection<MassBody>): Boolean =
        elements.map { add(it) }.any()

    override fun clear() {
        allBodies.clear()
        _solidBodies.clear()
        _emitGravityBodies.clear()
        _receiveGravityBodies.clear()
    }

    override fun remove(element: MassBody): Boolean {
        _solidBodies.remove(element)
        _emitGravityBodies.remove(element)
        _receiveGravityBodies.remove(element)
        return allBodies.remove(element)
    }

    override fun removeAll(elements: Collection<MassBody>): Boolean =
        elements.map { remove(it) }.any { it }

    override fun retainAll(elements: Collection<MassBody>): Boolean =
        allBodies.filterNot { elements.contains(it) }.map { remove(it) }.any { it }

    override fun contains(element: MassBody): Boolean =
        allBodies.contains(element)

    override fun containsAll(elements: Collection<MassBody>): Boolean =
        allBodies.containsAll(elements)

    override fun isEmpty(): Boolean =
        allBodies.isEmpty()

    override fun iterator(): MutableIterator<MassBody> =
        allBodies.iterator()
}