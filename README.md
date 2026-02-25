# 番茄小说下载器 Android 版

一个基于 Kotlin 开发的 Android 应用，用于从番茄小说平台下载小说内容。

## 项目特点

- **MVVM 架构**: 采用现代化的 Android 架构组件
- **Clean Architecture**: 清晰的分层设计，便于维护和测试
- **Kotlin + Jetpack**: 使用最新的 Android 开发技术栈
- **依赖注入**: 使用 Hilt 进行依赖管理
- **响应式编程**: 使用 Kotlin Coroutines 和 Flow
- **持久化存储**: 使用 Room 数据库
- **网络请求**: 使用 Retrofit + OkHttp

## 功能特性

- 输入小说ID或URL搜索小说
- 显示小说详细信息（名称、作者、简介、章节数）
- 支持下载为 TXT 或 EPUB 格式
- 后台下载服务，支持进度通知
- 断点续传功能
- 下载任务管理（暂停、继续、取消、删除）

## 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin | 主要开发语言 |
| Jetpack Components | 架构组件（ViewModel, LiveData, Room, DataStore） |
| Hilt | 依赖注入 |
| Retrofit | 网络请求 |
| OkHttp | HTTP 客户端 |
| Coroutines | 异步编程 |
| Flow | 响应式数据流 |
| Material Design | UI 设计 |

## 项目结构

```
app/src/main/java/com/tomato/novel/downloader/
├── data/                    # 数据层
│   ├── local/              # 本地数据源
│   │   ├── AppDatabase.kt  # Room 数据库
│   │   └── DownloadEntities.kt # 实体类
│   ├── model/              # 数据模型
│   ├── remote/             # 远程数据源
│   │   ├── TomatoApiService.kt # API 接口
│   │   └── NovelRemoteDataSource.kt
│   └── repository/         # 数据仓库
│       └── NovelRepository.kt
├── domain/                  # 领域层
│   ├── model/              # 领域模型
│   └── usecase/            # 用例
├── ui/                      # 表现层
│   └── main/               # 主界面
│       ├── MainActivity.kt
│       ├── MainViewModel.kt
│       └── DownloadTaskAdapter.kt
├── service/                 # 服务
│   └── DownloadService.kt  # 下载服务
├── di/                      # 依赖注入模块
│   └── AppModule.kt
├── utils/                   # 工具类
│   └── FileUtils.kt
└── TomatoNovelApp.kt       # 应用入口
```

## 构建要求

- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2

## 构建步骤

1. 克隆项目
```bash
git clone https://github.com/UnkownWorld/testfortm.git
```

2. 用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 点击 Run 按钮或使用命令：
```bash
./gradlew assembleDebug
```

## 使用说明

1. 打开应用，输入小说ID或番茄小说链接
2. 点击"搜索"按钮获取小说信息
3. 确认信息后点击"开始下载"
4. 选择下载格式（TXT/EPUB）
5. 在下载任务列表中管理下载进度

## 注意事项

- 本项目仅供学习研究使用
- 下载的小说内容仅供个人阅读
- 请勿用于商业用途或二次传播
- 需要配合番茄小说官方API使用

## 许可证

MIT License

## 致谢

- 原项目: [Tomato-Novel-Downloader-Lite](https://github.com/Dlmily/Tomato-Novel-Downloader-Lite)
- 开发者: Dlmily
