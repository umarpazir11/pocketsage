package com.umer.pocketsage.data.embedding

import kotlin.math.sqrt

internal fun meanPool(tokenEmbeddings: Array<FloatArray>, mask: IntArray): FloatArray {
    val size = tokenEmbeddings[0].size
    val result = FloatArray(size)
    var count = 0
    for (i in mask.indices) {
        if (mask[i] == 0) continue
        count++
        val row = tokenEmbeddings[i]
        for (j in 0 until size) result[j] += row[j]
    }
    if (count > 0) for (j in result.indices) result[j] /= count
    return result
}

internal fun l2Normalize(v: FloatArray) {
    var norm = 0f
    for (x in v) norm += x * x
    norm = sqrt(norm)
    if (norm > 1e-8f) for (i in v.indices) v[i] /= norm
}