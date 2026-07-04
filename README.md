# SimpleLiveTV

一个极简的 Android 直播播放器，基于 ExoPlayer 引擎，支持 txt 格式直播源。

## 功能

- 首次启动自动在 `Documents/SimpleLiveTV/` 创建文件夹和示例直播源文件
- 支持 txt 格式直播源（格式：`名称,URL` 或 `名称 URL` 或 `名称#URL`）
- 默认限制最大分辨率为 540p，节省流量
- 横屏全屏播放，自动隐藏系统栏
- 频道列表点击即播

## 编译方法

### 方式一：GitHub Actions 在线编译（推荐，免费）

无需安装任何软件，用 GitHub 免费云服务器编译：

1. 注册 [GitHub](https://github.com) 账号（免费）
2. 新建一个仓库，上传本项目所有文件
3. 进入仓库的 **Actions** 标签页
4. 点击左侧 **Build Android APK** → **Run workflow**
5. 等待约 5-10 分钟编译完成
6. 在 **Artifacts** 中下载 `app-debug.apk`

> 公开仓库每月无限免费编译时长，私有仓库每月 2000 分钟免费额度。

### 方式二：Android Studio（本地编译）

1. 安装 [Android Studio](https://developer.android.com/studio)
2. 打开本项目文件夹
3. 等待 Gradle 同步完成
4. 连接手机或启动模拟器
5. 点击运行按钮（▶）

编译后的 APK 在 `app/build/outputs/apk/debug/app-debug.apk`

### 方式三：命令行（需安装 Gradle）

```bash
./gradlew assembleDebug
```

## 使用方法

1. 安装 APK 后打开 App，授予存储权限
2. App 会自动在 `内部存储/Documents/SimpleLiveTV/` 创建 `live.txt` 文件
3. 用文件管理器打开该文件夹，编辑 `live.txt`，填入你的直播源：

```
CCTV1,http://your-server.com/cctv1.m3u8
CCTV2,http://your-server.com/cctv2.m3u8
湖南卫视,http://your-server.com/hunan.m3u8
```

4. 回到 App，点击"刷新列表"即可看到频道
5. 点击频道名称开始播放

## 分辨率说明

播放器默认限制最大分辨率为 540p（960x540），如需调整，请修改 `PlayerActivity.kt` 中的：

```kotlin
.setMaxVideoSize(960, 540)
```

可改为 `(1280, 720)` 或 `(640, 480)` 等。

## 技术栈

- Kotlin
- ExoPlayer (Media3)
- AndroidX / Material Design
- RecyclerView
