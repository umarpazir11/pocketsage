package com.umer.pocketsage.data.llm

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepository @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val modelFile: File
        get() = File(ctx.filesDir, "models/gemma2b.litertlm")

    private val _importProgress = MutableStateFlow(0f)
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    fun isModelReady(): Boolean = modelFile.exists() && modelFile.length() > 0

    fun getModelPath(): File = modelFile

    suspend fun importFromUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            _importProgress.value = 0f

            val destDir = File(ctx.filesDir, "models").also { it.mkdirs() }
            // Write to a temp file so a partial copy is never mistaken for a valid model.
            val tmp = File(destDir, "gemma2b.litertlm.tmp")

            val totalBytes = queryFileSize(uri)
            var copiedBytes = 0L

            ctx.contentResolver.openInputStream(uri)
                ?.use { input ->
                    FileOutputStream(tmp).use { output ->
                        val buffer = ByteArray(COPY_BUFFER_SIZE)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            copiedBytes += read
                            if (totalBytes > 0L) {
                                _importProgress.value = copiedBytes.toFloat() / totalBytes
                            }
                        }
                    }
                } ?: error("Cannot open content stream for $uri")

            tmp.renameTo(modelFile)
            _importProgress.value = 1f
        }
    }

    private fun queryFileSize(uri: Uri): Long =
        ctx.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && col != -1) cursor.getLong(col) else 0L
        } ?: 0L

    private companion object {
        const val COPY_BUFFER_SIZE = 8 * 1024
    }
}