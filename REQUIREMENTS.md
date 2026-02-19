# Photo Cleaner App - 需求文档

## 1. 产品概述

**Photo Cleaner** 是一款 Android 照片清理应用，通过简单的手势操作帮助用户快速清理相册中的照片。

## 2. 核心功能

### 2.1 照片浏览
- 每次随机加载 10 张照片
- 全屏显示当前照片
- 显示照片基本信息（拍摄日期、文件大小）
- 显示进度指示（当前第几张/共 10 张）

### 2.2 手势操作
- **左滑** → 跳过（保留照片）
- **右滑** → 删除照片（移动到回收站或永久删除）
- **上滑** → 查看照片详情
- **下滑** → 返回/退出

### 2.3 批量操作
- 支持批量选择多张照片
- 一键删除所有标记的照片
- 撤销误删操作

### 2.4 相册权限
- 请求相册访问权限
- 支持 Android 10+ 的分区存储
- 兼容 Android 13+ 的照片和视频权限

## 3. 技术栈

### 3.1 开发环境
- **语言**: Kotlin
- **最低 SDK**: API 26 (Android 8.0)
- **目标 SDK**: API 34 (Android 14)
- **构建工具**: Gradle 8.0+

### 3.2 架构
- **架构模式**: MVVM (Model-View-ViewModel)
- **UI 框架**: Jetpack Compose
- **依赖注入**: Hilt
- **异步处理**: Coroutines + Flow

### 3.3 关键库
- `androidx.camera` - 相机和媒体访问
- `androidx.lifecycle` - ViewModel + LiveData
- `androidx.navigation` - 导航
- `coil` 或 `glide` - 图片加载
- `accompanist` - Compose 扩展

## 4. 项目结构

```
app/
├── src/main/
│   ├── java/com/ginkcoding/photocleaner/
│   │   ├── MainActivity.kt
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── PhotoViewerScreen.kt
│   │   │   │   └── SettingsScreen.kt
│   │   │   └── components/
│   │   │       ├── PhotoCard.kt
│   │   │       ├── GestureDetector.kt
│   │   │       └── ProgressBar.kt
│   │   ├── data/
│   │   │   ├── repository/
│   │   │   │   └── PhotoRepository.kt
│   │   │   └── model/
│   │   │       └── Photo.kt
│   │   └── viewModel/
│   │       └── PhotoViewModel.kt
│   └── res/
│       ├── values/
│       │   ├── strings.xml
│       │   ├── colors.xml
│       │   └── themes.xml
│       └── drawable/
└── build.gradle.kts
```

## 5. 用户流程

```
启动应用
    ↓
请求权限
    ↓
加载照片 (随机 10 张)
    ↓
显示照片 → 用户手势
    ├─ 左滑 → 跳过 → 下一张
    ├─ 右滑 → 删除 → 下一张
    └─ 最后一张 → 完成
        ↓
显示清理结果
    ↓
返回主页/退出
```

## 6. 验收标准

### 6.1 功能验收
- [ ] 应用能正常启动
- [ ] 能正确请求并获取相册权限
- [ ] 能随机加载 10 张照片
- [ ] 左滑手势能跳过照片
- [ ] 右滑手势能删除照片
- [ ] 删除的照片能从相册移除
- [ ] 进度指示正确显示
- [ ] 10 张照片处理完成后显示结果

### 6.2 性能验收
- [ ] 照片加载时间 < 500ms
- [ ] 手势响应延迟 < 100ms
- [ ] 应用内存占用 < 100MB
- [ ] 无内存泄漏

### 6.3 兼容性验收
- [ ] Android 8.0 (API 26) 正常运行
- [ ] Android 10 (API 29) 分区存储兼容
- [ ] Android 13 (API 33) 照片权限兼容
- [ ] 不同屏幕尺寸适配

## 7. 测试计划

### 7.1 单元测试
- PhotoRepository 测试
- PhotoViewModel 测试
- 手势识别逻辑测试

### 7.2 UI 测试
- Compose UI 测试
- 手势操作测试
- 导航流程测试

### 7.3 集成测试
- 相册访问测试
- 删除操作测试
- 权限流程测试

## 8. 交付物

1. 完整的 Android 项目源代码
2. 可安装的 APK 文件
3. 测试报告
4. README.md 使用说明
5. GitHub 仓库推送

## 9. 时间估算

| 阶段 | 时间 |
|------|------|
| 项目初始化 | 30 分钟 |
| 核心功能开发 | 2 小时 |
| UI/UX 优化 | 1 小时 |
| 测试 | 1 小时 |
| 修复与优化 | 1 小时 |
| **总计** | **5.5 小时** |

---

*文档版本：1.0*
*创建日期：2026-02-19*
