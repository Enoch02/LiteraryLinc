package com.enoch02.resources.extensions

import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Adds [item] to the list if it is unique
 */
fun <T> SnapshotStateList<T>.uniqueAdd(item: T): Boolean {
    return if (item !in this) {
        add(item)
        true
    } else {
        false
    }
}

/**
 * Add [items] to this list if they are all unique
 */
fun <T> SnapshotStateList<T>.uniqueAddAll(items: Collection<T>): Boolean {
    var modified = false
    items.forEach { item ->
        if (uniqueAdd(item)) {
            modified = true
        }
    }

    return modified
}