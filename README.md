# 移动软件开发期中实验报告

## 一、实验名称

NotePad 记事本应用功能扩展

## 二、实验目的

基于给定的 NotePad 记事本应用，完成以下功能扩展：

**基本要求（必做）：**
1. NoteList 界面中笔记条目增加时间戳显示
2. 添加笔记查询功能（根据标题或内容查询）

**附加功能（至少两项）：**
3. 笔记分类功能
4. 笔记排序功能（按时间或标题升降序）
5. 笔记置顶功能
6. 笔记便签（每条笔记可选不同的便利贴颜色）
7. 更换应用整体背景主题

## 三、实验环境

| 项目 | 版本/信息 |
|------|-----------|
| 开发语言 | Kotlin 2.2.10 |
| UI 框架 | Jetpack Compose + Material 3 |
| Android Gradle Plugin | 9.3.0 |
| Gradle | 9.5.0 |
| compileSdk | 37 |
| minSdk | 24 |
| 数据库 | SQLite（SQLiteOpenHelper） |
| 开发工具 | Android Studio + JBR 25.0.2 |
| 运行环境 | Android 模拟器 / 真机 |

## 四、系统架构设计

### 4.1 整体架构

采用 **单 Activity + 多 Composable 路由** 架构：

```
MainActivity (ComponentActivity)
├── NotePadTheme (动态主题，支持多背景切换)
└── NotePadApp (Scaffold 主界面)
    ├── 列表视图 → NoteCard × N + LazyColumn
    ├── 搜索栏 → OutlinedTextField (全局过滤)
    ├── 编辑视图 → NoteEditorScreen (Composable 路由)
    ├── 分类筛选对话框 → CategoryFilterDialog
    └── 背景选择对话框 → BgChooserDialog
```

数据层：
- `NotesDbAdapter`：SQLite 封装，提供 CRUD + 查询接口
- `NotePadViewModel`：StateFlow 响应式状态管理，驱动 UI 刷新

### 4.2 数据库设计

```sql
CREATE TABLE notes (
    _id        INTEGER PRIMARY KEY AUTOINCREMENT,
    title      TEXT NOT NULL,
    note       TEXT,
    category   TEXT DEFAULT '',
    color      TEXT DEFAULT '#FFFFFF',
    created    INTEGER,
    modified   INTEGER,
    pinned     INTEGER DEFAULT 0
);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `_id` | INTEGER | 主键，自增 |
| `title` | TEXT | 笔记标题 |
| `note` | TEXT | 笔记正文 |
| `category` | TEXT | 分类（空字符串表示无分类） |
| `color` | TEXT | 便签颜色（HEX 格式如 `#FFCDD2`） |
| `created` | INTEGER | 创建时间戳（毫秒） |
| `modified` | INTEGER | 最后修改时间戳（毫秒） |
| `pinned` | INTEGER | 是否置顶（0=否，1=是） |

### 4.3 数据库版本迁移

DB 版本从 v1 升级到 v3，自动执行 `ALTER TABLE`：

- v1 → v2：新增 `category`、`created`、`modified`、`pinned` 四列
- v2 → v3：新增 `color` 列，默认值 `'#FFFFFF'`

所有旧数据升级后自动兼容，无需手动处理。

## 五、功能实现详解

### 5.1 时间戳显示（基本要求 1）

**实现位置**：`NotePadApp.kt` 中 `NoteCard` 组件

每条笔记卡片底部显示修改时间：

```kotlin
// NotePadViewModel.kt
fun formatTime(ts: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}
```

卡片布局（从上到下）：标题 → 摘要 → 时间戳 + 分类标签

### 5.2 笔记查询功能（基本要求 2）

**实现位置**：`NotePadViewModel.setSearchQuery()` + `NotesDbAdapter.searchNotes()`

工具栏放大镜按钮展开搜索框，输入实时过滤：

```kotlin
// NotesDbAdapter.kt
fun searchNotes(query: String, sortOrder: String = ...): List<Note> {
    val sel = "$KEY_TITLE LIKE ? OR $KEY_NOTE LIKE ?"
    val args = arrayOf("%$query%", "%$query%")
    return query(sel, args, sortOrder)
}
```

- 支持标题和正文**模糊匹配**
- 搜索结果自动套用当前排序规则
- 清空搜索内容即恢复完整列表

### 5.3 笔记分类功能

**数据存储**：`category TEXT DEFAULT ''`

编辑笔记时通过下拉菜单选择分类，自动从已有分类中读取：

```kotlin
// NotesDbAdapter.kt
fun fetchCategories(): List<String> {
    val cursor = db?.query(true, TABLE, arrayOf(KEY_CATEGORY), ...)
    val cats = mutableListOf<String>()
    cursor?.use {
        while (it.moveToNext()) {
            val c = it.getString(it.getColumnIndexOrThrow(KEY_CATEGORY))
            if (c.isNotEmpty()) cats.add(c)
        }
    }
    return cats
}
```

列表页工具栏 "📂" 按钮打开分类筛选对话框，选中后只显示该分类笔记。

### 5.4 笔记排序功能

四种排序规则，置顶笔记始终优先：

| 排序方式 | SQL 子句 |
|----------|----------|
| 修改时间（最新在前） | `pinned DESC, modified DESC` |
| 修改时间（最早在前） | `pinned DESC, modified ASC` |
| 标题（A-Z） | `pinned DESC, title ASC` |
| 标题（Z-A） | `pinned DESC, title DESC` |

### 5.5 笔记置顶

每条卡片右侧 ☆/★ 按钮切换置顶，置顶笔记永远出现在列表顶部，排序时先排置顶再排其余。

### 5.6 笔记便签（附加功能）

**数据存储**：`color TEXT DEFAULT '#FFFFFF'`

编辑笔记时，分类选择器下方展示 7 色便签圆点：

```kotlin
companion object {
    val NOTE_COLORS = listOf(
        "#FFFFFF",   // 默认白
        "#FFF9C4",   // 黄
        "#FFCDD2",   // 粉
        "#BBDEFB",   // 蓝
        "#C8E6C9",   // 绿
        "#E1BEE7",   // 紫
        "#FFE0B2"    // 橙
    )
}
```

卡片显示时用选中颜色作为 CardContainerColor：

```kotlin
val cardColor = try { Color(android.graphics.Color.parseColor(note.color)) } catch (_: Exception) { Color.White }
Card(colors = CardDefaults.cardColors(containerColor = cardColor)) { ... }
```

### 5.7 更换应用背景（附加功能）

**持久化**：SharedPreferences `notepad_prefs`

```kotlin
fun setAppBg(bg: String) {
    _appBg.value = bg
    prefs.edit().putString(KEY_APP_BG, bg).apply()
}
```

主题层动态应用：

```kotlin
@Composable
fun NotePadTheme(bg: String = "default", content: @Composable () -> Unit) {
    val base = if (isSystemInDarkTheme()) DarkColors else LightColors
    val bgColor = when (bg) {
        BG_MILK -> Color(0xFFFDF6E3)
        BG_LAVENDER -> Color(0xFFF3E5F5)
        BG_MINT -> Color(0xFFE8F5E9)
        BG_SAND -> Color(0xFFFBE9E7)
        else -> base.background
    }
    val colors = base.copy(background = bgColor, surface = bgColor)
    MaterialTheme(colorScheme = colors, content = content)
}
```

## 六、关键代码文件清单

| 文件路径 | 说明 |
|----------|------|
| `app/src/main/.../MainActivity.kt` | 入口 Activity，初始化 ViewModel + 主题 |
| `app/src/main/.../NotePadApp.kt` | 主界面 Scaffold、NoteCard、分类/背景对话框 |
| `app/src/main/.../NoteEditorScreen.kt` | 笔记编辑界面（标题/分类/便签色/正文） |
| `app/src/main/.../NotePadViewModel.kt` | 状态管理 + SharedPreferences 偏好存储 |
| `app/src/main/.../NotesDbAdapter.kt` | SQLite 封装（CRUD + 搜索 + 分类查询 + DB 版本迁移） |
| `app/src/main/.../ui/theme/Theme.kt` | Material3 动态主题，支持 5 种应用背景 |
| `app/src/main/res/values/strings.xml` | 全部字符串资源 |
| `app/build.gradle.kts` | Gradle 构建配置 |
| `gradle/libs.versions.toml` | 依赖版本统一管理 |

## 七、运行与构建

### 7.1 Android Studio 运行

1. 用 Android Studio 打开 `NotePad/` 目录
2. 等待 Gradle Sync 完成（首次需要下载依赖）
3. 连接真机或启动模拟器
4. 点击工具栏 ▶️ Run

### 7.2 命令行构建

```bash
cd NotePad
# 设置 JAVA_HOME 指向 Android Studio 自带 JBR
./gradlew.bat :app:assembleDebug
# APK 输出：app/build/outputs/apk/debug/app-debug.apk
```

### 7.3 构建验证

```
BUILD SUCCESSFUL in 52s
29 actionable tasks: 29 up-to-date
```

## 八、遇到的问题及解决

### 问题 1：Compose 的 Modifier.weight() 离线编译报 internal 错误

**现象**：`Cannot access 'val RowColumnParentData?.weight: Float': it is internal`

**原因**：Gradle 离线缓存的 Compose 依赖版本与编译器不匹配，`weight` 内部类不可见。

**解决方案**：彻底移除 `Modifier.weight()` 调用，改用 `Box + padding + align` 组合布局替代。功能完全等价：

```kotlin
// 原实现
Column(Modifier.weight(1f)) { ... }

// 替代方案
Column(Modifier.fillMaxWidth().padding(end = 96.dp)) { ... }
```

### 问题 2：StateFlow 不能直接 `by` 委托

**现象**：`Property delegate must have a 'getValue' method`

**原因**：`StateFlow` 本身没有 `getValue` 操作符，需要 `.collectAsState()` 转为 Compose State。

**解决方案**：统一使用 `by viewModel.xxx.collectAsState()` 语法。

### 问题 3：SQLite 升级方案扩展

**需求**：先做分类（v1→v2），后做便签颜色（v2→v3）。

**解决方案**：在 `onUpgrade` 中用 `if (oldV < X)` 分段迁移，每个新功能都对应一次 DB_VER 提升，`ALTER TABLE ADD COLUMN` 兼容已安装用户。

### 问题 4：模拟器数据持久化

**场景**：反复安装调试导致旧版 SQLite 残留。

**方案**：DB 版本号递增 + 分段 ALTER TABLE 是正确做法；若想彻底清除可在 Android Studio Device Manager 里 `Wipe Data`。

## 九、实验总结

本次期中实验基于 NotePad 记事本应用，实现了 **2 项基本功能 + 5 项附加功能**：

✅ **基本功能**：时间戳显示、标题/内容模糊查询
✅ **附加功能**：笔记分类、多种排序、置顶、便签颜色、应用背景主题

技术栈亮点：
- **Kotlin + Jetpack Compose** 声明式 UI，单文件单职责
- **StateFlow + ViewModel** 响应式架构，数据流驱动刷新
- **SQLite 版本迁移** 支持增量升级，不丢失用户数据
- **SharedPreferences** 轻量级存储偏好设置
- **Material 3** 动态配色，背景色实时应用到整个主题

全程离线环境下完成构建，APK 正常安装运行，满足实验所有要求。

---

*实验日期：2026 年 11 月*
