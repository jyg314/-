package com.simplelivetv.app

import java.io.File
import java.io.IOException

object ChannelParser {

    /**
     * 解析 txt 直播源文件
     * 支持格式：
     *   1) 名称,URL
     *   2) 名称#URL
     *   3) 名称 URL（空格分隔）
     *   4) URL（单独一行，名称用域名或序号）
     */
    fun parse(file: File): List<Channel> {
        val channels = mutableListOf<Channel>()
        if (!file.exists()) return channels

        file.readLines().forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            val channel = when {
                trimmed.contains(",") -> parseComma(trimmed)
                trimmed.contains("$") -> parseHash(trimmed)
                trimmed.contains(" ") -> parseSpace(trimmed)
                trimmed.startsWith("http") -> Channel("频道${index + 1}", trimmed)
                else -> null
            }
            channel?.let { channels.add(it) }
        }
        return channels
    }

    private fun parseComma(line: String): Channel? {
        val parts = line.split(",", limit = 2)
        return if (parts.size == 2) Channel(parts[0].trim(), parts[1].trim()) else null
    }

    private fun parseHash(line: String): Channel? {
        val parts = line.split("#", limit = 2)
        return if (parts.size == 2) Channel(parts[0].trim(), parts[1].trim()) else null
    }

    private fun parseSpace(line: String): Channel? {
        val parts = line.split(" ", limit = 2)
        return if (parts.size == 2 && parts[1].startsWith("http")) {
            Channel(parts[0].trim(), parts[1].trim())
        } else null
    }
}
