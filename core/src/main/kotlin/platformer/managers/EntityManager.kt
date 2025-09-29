package platformer.managers

open class EntityManager<T> {
    private val items = mutableListOf<T>()

    open fun add(item: T) {
        items.add(item)
    }

    open fun remove(item: T): Boolean {
        return items.remove(item)
    }

    open fun removeAll(predicate: (T) -> Boolean): Boolean {
        return items.removeAll(predicate)
    }

    fun getAll(): List<T> = items.toList() // Return a copy to prevent external modification

    fun getMutableList(): MutableList<T> = items // For internal use only

    fun clear() {
        items.clear()
    }

    fun isEmpty(): Boolean = items.isEmpty()

    fun size(): Int = items.size

    // Safe iteration with removal capability
    fun removeWithIterator(predicate: (T) -> Boolean) {
        val iterator = items.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (predicate(item)) {
                iterator.remove()
            }
        }
    }
}



