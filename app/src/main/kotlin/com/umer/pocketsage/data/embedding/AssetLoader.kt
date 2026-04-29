package com.umer.pocketsage.data.embedding

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AssetLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("modelAssetPath") private val modelAssetPath: String,
    @Named("vocabAssetPath") private val vocabAssetPath: String
) {
    fun loadModel(): ByteBuffer = FileUtil.loadMappedFile(context, modelAssetPath)

    fun loadVocab(): Map<String, Int> =
        context.assets.open(vocabAssetPath).bufferedReader().use { reader ->
            reader.lineSequence()
                .mapIndexed { index, token -> token.trim() to index }
                .filter { it.first.isNotEmpty() }
                .toMap()
        }
}