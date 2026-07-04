package com.simplelivetv.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全屏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)

        val name = intent.getStringExtra("name") ?: "未知频道"
        val url = intent.getStringExtra("url") ?: ""

        tvTitle.text = name
        btnBack.setOnClickListener { finish() }

        if (url.isNotEmpty()) {
            initPlayer(url)
        } else {
            Toast.makeText(this, "无效的直播地址", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initPlayer(url: String) {
        val selector = DefaultTrackSelector(this).apply {
            // 限制最大分辨率为 540p
            setParameters(
                buildUponParameters()
                    .setMaxVideoSizeSd() // SD = 720x480，更严格的话可以手动设
                    .setMaxVideoSize(960, 540) // 最大 540p
            )
        }
        trackSelector = selector

        val player = ExoPlayer.Builder(this)
            .setTrackSelector(selector)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
            .also { exoPlayer = it }

        playerView.player = player
        playerView.useController = true
        playerView.controllerHideOnTouch = true
        playerView.controllerShowTimeoutMs = 3000

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType("application/x-mpegURL")
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {}
                    Player.STATE_ENDED -> player.seekTo(0)
                    Player.STATE_BUFFERING -> {}
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@PlayerActivity, "播放失败: ${error.message}", Toast.LENGTH_LONG).show()
            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                // 打印当前选中轨道信息（调试用）
                for (group in tracks.groups) {
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until group.length) {
                            if (group.isTrackSelected(i)) {
                                val format = group.getTrackFormat(i)
                                val res = "${format.width}x${format.height}"
                                tvTitle.text = "${tvTitle.text} ($res)"
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
        trackSelector = null
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowInsetsControllerCompat(window, window.decorView).hide(
                WindowInsetsCompat.Type.systemBars()
            )
        }
    }
}
