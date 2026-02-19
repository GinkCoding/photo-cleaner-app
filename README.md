# Photo Cleaner - 照片清理应用

📸 一款简单高效的 Android 照片清理工具，通过手势操作快速清理相册。

## 功能特点

- ✨ **随机选择** - 每次随机加载 10 张照片
- 👈 **左滑跳过** - 保留照片，继续下一张
- 👉 **右滑删除** - 删除照片，释放空间
- 🔄 **撤销操作** - 支持撤销误删
- 📊 **清理统计** - 显示删除和保留的照片数量
- 🔒 **隐私安全** - 所有操作在本地完成，不上传任何数据

## 系统要求

- Android 8.0 (API 26) 及以上
- 相册访问权限

## 构建说明

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 构建步骤

1. 克隆项目
```bash
git clone https://github.com/GinkCoding/photo-cleaner-app.git
cd photo-cleaner-app
```

2. 使用 Android Studio 打开项目

3. 同步 Gradle 文件

4. 构建并运行
   - 点击 Run 按钮
   - 或使用快捷键 `Shift + F10`

### 命令行构建

```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 运行测试
./gradlew test
```

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVVM
- **依赖注入**: Hilt
- **图片加载**: Coil
- **异步处理**: Coroutines + Flow

## 项目结构

```
app/
├── src/main/
│   ├── java/com/ginkcoding/photocleaner/
│   │   ├── MainActivity.kt
│   │   ├── PhotoCleanerApp.kt
│   │   ├── ui/
│   │   │   ├── theme/          # 主题和样式
│   │   │   ├── screens/        # 屏幕组件
│   │   │   └── components/     # 通用组件
│   │   ├── data/
│   │   │   ├── model/          # 数据模型
│   │   │   └── repository/     # 数据仓库
│   │   └── viewModel/          # ViewModel
│   └── res/                    # 资源文件
└── build.gradle.kts
```

## 使用说明

1. **首次启动**
   - 应用会请求相册访问权限
   - 点击"授予权限"允许访问

2. **清理照片**
   - 应用会自动加载 10 张随机照片
   - 左滑跳过（保留照片）
   - 右滑删除（移除照片）
   - 查看底部进度条了解当前进度

3. **查看结果**
   - 处理完 10 张照片后显示统计
   - 可以选择"再来一次"或"退出"

## 权限说明

| 权限 | 用途 | Android 版本 |
|------|------|-------------|
| READ_MEDIA_IMAGES | 读取照片 | Android 13+ |
| READ_MEDIA_VIDEO | 读取视频 | Android 13+ |
| READ_EXTERNAL_STORAGE | 读取存储 | Android 12 及以下 |

## 测试

### 单元测试

```bash
./gradlew testDebugUnitTest
```

### UI 测试

```bash
./gradlew connectedAndroidTest
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

- GitHub: [@GinkCoding](https://github.com/GinkCoding)
- Email: contact@ginkcoding.com

---

**版本**: 1.0.0  
**更新日期**: 2026-02-19
