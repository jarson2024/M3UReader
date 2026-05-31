package com.example.m3ureader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/** Reproduz a URL de um canal usando ExoPlayer (Media3). */
class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_NAME = "extra_name"
    }

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        playerView = findViewById(R.id.playerView)
        title = intent.getStringExtra(EXTRA_NAME) ?: getString(R.string.app_name)
    }

    private fun initPlayer() {
        val url = intent.getStringExtra(EXTRA_URL)
        if (url.isNullOrBlank()) {
            finish()
            return
        }
        val exo = ExoPlayer.Builder(this).build()
        playerView.player = exo
        exo.setMediaItem(MediaItem.fromUri(url))
        exo.playWhenReady = true
        exo.prepare()
        player = exo
    }

    private fun releasePlayer() {
        playerView.player = null
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }
}
