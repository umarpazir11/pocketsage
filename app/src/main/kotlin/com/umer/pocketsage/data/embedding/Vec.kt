package com.umer.pocketsage.data.embedding

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun FloatArray.toByteArray(): ByteArray {
    val buf = ByteBuffer.allocate(size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
    for (f in this) buf.putFloat(f)
    return buf.array()
}

fun ByteArray.toFloatArray(): FloatArray {
    val buf = ByteBuffer.wrap(this).order(ByteOrder.nativeOrder())
    return FloatArray(size / Float.SIZE_BYTES) { buf.getFloat() }
}