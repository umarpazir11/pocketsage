package com.umer.pocketsage.data.embedding

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Named

class AssetLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("modelAssetPath") private val modelPath: String,
    @Named("vocabAssetPath") private val vocabPath: String,
) {
    fun loadModel(): MappedByteBuffer {
        val fd = context.assets.openFd(modelPath)
        return FileInputStream(fd.fileDescriptor).channel
            .map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    fun loadVocab(): List<String> =
        context.assets.open(vocabPath).bufferedReader().readLines()
}