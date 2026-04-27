package com.umer.pocketsage

import android.app.Application
import android.util.Log
import com.umer.pocketsage.data.embedding.AssetLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var assetLoader: AssetLoader

    override fun onCreate() {
        super.onCreate()
        smokeTestAssets()
    }

    private fun smokeTestAssets() {
        try {
            val model = assetLoader.loadModel()
            val vocab = assetLoader.loadVocab()
            Log.d(TAG, "model buffer: ${model.capacity()} bytes | vocab: ${vocab.size} tokens")
        } catch (e: Exception) {
            Log.e(TAG, "asset load failed", e)
        }
    }

    companion object {
        private const val TAG = "PocketSage"
    }
}