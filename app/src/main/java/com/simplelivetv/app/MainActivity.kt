package com.simplelivetv.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvStatus: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnOpenFolder: Button

    private val folderName = "SimpleLiveTV"
    private val fileName = "live.txt"

    private val folderPath: String
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .absolutePath + "/" + folderName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        tvStatus = findViewById(R.id.tvStatus)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnOpenFolder = findViewById(R.id.btnOpenFolder)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnRefresh.setOnClickListener { loadChannels() }
        btnOpenFolder.setOnClickListener { openFolder() }

        checkAndRequestPermission()
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                setupFolderAndLoad()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                AlertDialog.Builder(this)
                    .setTitle("需要存储权限")
                    .setMessage("本应用需要读取存储权限来加载直播源文件。")
                    .setPositiveButton("确定") { _, _ ->
                        requestPermission(permission)
                    }
                    .show()
            }
            else -> {
                requestPermission(permission)
            }
        }
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQ_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupFolderAndLoad()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    AlertDialog.Builder(this)
                        .setTitle("需要全部文件访问权限")
                        .setMessage("Android 11+ 需要手动开启全部文件访问权限。")
                        .setPositiveButton("去设置") { _, _ ->
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }
                        .setNegativeButton("取消", null)
                        .show()
                } else {
                    Toast.makeText(this, "没有权限无法读取直播源", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupFolderAndLoad() {
        val folder = File(folderPath)
        if (!folder.exists()) {
            val created = folder.mkdirs()
            if (created) {
                // 创建示例文件
                val sampleFile = File(folder, fileName)
                sampleFile.writeText(
                    "# SimpleLiveTV 直播源文件\n" +
                    "# 格式：名称,URL\n" +
                    "# 或：名称 URL\n" +
                    "# 或：名称#URL\n" +
                    "# 请将你的直播源放在此文件中\n\n" +
                    "CCTV1,https://example.com/cctv1.m3u8\n" +
                    "CCTV2,https://example.com/cctv2.m3u8\n"
                )
                Toast.makeText(this, "已在 Documents/SimpleLiveTV 创建示例文件", Toast.LENGTH_LONG).show()
            }
        }
        loadChannels()
    }

    private fun loadChannels() {
        val file = File(folderPath, fileName)
        if (!file.exists()) {
            tvStatus.text = "未找到直播源文件\n请将 ${fileName} 放入 Documents/${folderName}/"
            recyclerView.adapter = ChannelAdapter(emptyList()) {}
            return
        }

        val channels = ChannelParser.parse(file)
        if (channels.isEmpty()) {
            tvStatus.text = "直播源文件为空或格式不正确"
            recyclerView.adapter = ChannelAdapter(emptyList()) {}
            return
        }

        tvStatus.text = "共 ${channels.size} 个频道"
        recyclerView.adapter = ChannelAdapter(channels) { channel ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("name", channel.name)
                putExtra("url", channel.url)
            }
            startActivity(intent)
        }
    }

    private fun openFolder() {
        val folder = File(folderPath)
        if (!folder.exists()) {
            Toast.makeText(this, "文件夹不存在", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(folder.absolutePath), "resource/folder")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "请使用文件管理器打开：${folder.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQ_PERMISSION = 100
    }
}
