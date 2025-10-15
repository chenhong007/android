# Android 时间管理应用 - 详细待办清单

**生成日期：** 2025-01-12  
**项目完成度：** 48%  
**剩余任务数：** 37 个

---

## 📊 任务概览

| 任务组 | 完成度 | 任务数 | 预计工作量 |
|--------|--------|--------|------------|
| 任务组1：完善核心架构 | 100% | 3 | 已完成 |
| 任务组2：完善图表和过滤系统 | 60% → 100% | 7 | 8-10小时 |
| 任务组3：实现缺失页面 | 40% → 100% | 11 | 15-20小时 |
| 任务组4：实现提醒系统 | 0% → 100% | 7 | 10-12小时 |
| 任务组5：实现数据导出和备份 | 0% → 100% | 9 | 12-15小时 |
| 任务组6：测试和优化 | 0% → 100% | 6 | 8-10小时 |

**总计：** 55-70 小时的开发工作

---

## 🔴 高优先级任务（核心功能）

### 任务组1：完善核心架构（已完成，验证通过）

- NotificationRepository：CRUD、统计、响应率、趋势、清理旧数据均已实现
- SettingsRepository：主题、语言、性能模式、提醒配置、黑名单、保留期、引导与权限已实现
- ScreenEventRepository：CRUD、解锁次数、屏幕时长、会话统计、分布分析、首解锁/末关屏、清理旧数据已实现

已通过 assembleDebug 编译验证，仓库层无编译错误。

#### arch-01: 创建 NotificationRepository
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/data/repository/NotificationRepository.kt`  
**预计时间：** 45 分钟  

**功能描述：**
- 封装 NotificationRecordDao
- 提供通知记录的增删改查方法
- 实现通知统计功能（按应用、按时间段）
- 计算通知响应率
- 使用协程处理异步操作

**依赖：**
- NotificationRecordDao.kt（已存在）

**测试要点：**
- Repository 方法的单元测试
- 响应率计算准确性

---

#### arch-02: 创建 SettingsRepository
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/data/repository/SettingsRepository.kt`  
**预计时间：** 45 分钟  

**功能描述：**
- 封装 UserSettingsDao
- 提供用户设置的读写方法
- 实现主题管理（深色/浅色模式）
- 语言设置管理
- 性能模式管理
- 提醒配置管理

**依赖：**
- UserSettingsDao.kt（已存在）

**测试要点：**
- 设置持久化测试
- 默认值处理测试

---

#### arch-03: 创建 ScreenEventRepository
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/data/repository/ScreenEventRepository.kt`  
**预计时间：** 45 分钟  

**功能描述：**
- 封装 ScreenEventDao
- 提供屏幕事件的增删改查
- 计算每日解锁次数
- 计算屏幕开启总时长
- 统计屏幕使用模式

**依赖：**
- ScreenEventDao.kt（已存在）

**测试要点：**
- 解锁次数计算准确性
- 时长统计准确性

---

### 任务组2：图表集成（任务3）

#### chart-07: 集成 MPAndroidChart 到 StatisticsScreen
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/statistics/StatisticsScreen.kt`  
**预计时间：** 3-4 小时  

**功能描述：**
- 将 StatisticsScreen 中的图表占位符替换为实际渲染
- 使用 AndroidView 包装 BarChartManager
- 使用 AndroidView 包装 CalendarHeatmapManager
- 使用 AndroidView 包装 TimelineChartManager
- 处理图表生命周期
- 实现图表点击交互
- 添加加载状态和错误处理

**依赖：**
- BarChartManager.kt（已存在）
- CalendarHeatmapManager.kt（已存在）
- TimelineChartManager.kt（已存在）

**实现示例：**
```kotlin
AndroidView(
    factory = { context ->
        val manager = BarChartManager(context)
        lifecycleScope.launch {
            manager.createHorizontalBarChart(data, config)
        }
    },
    modifier = Modifier.fillMaxWidth().height(300.dp)
)
```

**测试要点：**
- 图表渲染性能 <300ms
- 内存泄漏检测
- 旋转屏幕测试

---

### 任务组3：实现缺失页面（任务4剩余60%）

#### ui-01: 创建应用管理页面
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/apps/AppManagementScreen.kt`  
**预计时间：** 4-5 小时  

**功能描述：**
- 显示所有已安装应用列表（带图标）
- 实现应用搜索功能
- 为每个应用显示使用时长统计
- 添加时间限制设置（小时/分钟选择器）
- 实现应用黑名单开关
- 添加应用分类过滤
- 支持下拉刷新

**UI 组件：**
- LazyColumn 显示应用列表
- SearchBar 搜索应用
- FilterChip 类别过滤
- TimePicker 时间限制设置
- Switch 黑名单开关

**依赖：**
- UsageRepository（已存在）

---

#### ui-02: 创建应用管理 ViewModel
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/apps/AppManagementViewModel.kt`  
**预计时间：** 2-3 小时  

**功能描述：**
- 加载已安装应用列表
- 管理应用搜索状态
- 管理类别过滤状态
- 处理时间限制配置
- 管理黑名单状态
- 实现数据持久化

**State 定义：**
```kotlin
data class AppManagementState(
    val apps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

---

#### ui-03: 创建提醒设置页面
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/reminders/RemindersScreen.kt`  
**预计时间：** 4-5 小时  

**功能描述：**
- 全局使用限制设置（每日总时长）
- 每个应用的时间限制设置
- 进度提醒设置（80%、90%、100%）
- 休息提醒间隔设置
- 免打扰时段设置（开始/结束时间）
- 免打扰应用白名单
- 提醒声音和振动设置

**UI 组件：**
- Slider 时间限制滑块
- TimePicker 免打扰时段
- Switch 功能开关
- CheckBox 进度提醒选项

---

#### ui-04: 创建提醒设置 ViewModel
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/reminders/RemindersViewModel.kt`  
**预计时间：** 2-3 小时  

**功能描述：**
- 管理全局限制状态
- 管理每应用限制状态
- 管理免打扰时段
- 管理白名单
- 验证时间设置合法性
- 持久化配置

---

#### ui-05: 创建历史数据页面
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/history/HistoryScreen.kt`  
**预计时间：** 4-5 小时  

**功能描述：**
- 月视图日历（自定义或使用 Compose Calendar）
- 每日使用时长标记（颜色编码）
- 点击日期显示详细信息
- 趋势分析图表（周趋势、月趋势）
- 对比功能（本周 vs 上周）
- 导出功能入口

**UI 组件：**
- 自定义日历网格
- LineChart 趋势图
- Card 每日详情卡片

---

#### ui-06: 创建历史数据 ViewModel
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/history/HistoryViewModel.kt`  
**预计时间：** 2-3 小时  

**功能描述：**
- 加载历史数据（按月、按周）
- 计算每日总使用时长
- 计算趋势数据
- 处理日期导航
- 实现对比逻辑

---

#### ui-07: 更新 TimeApp.kt 导航
**优先级：** 🔴 高  
**文件：** `app/src/main/java/com/example/time/ui/TimeApp.kt`  
**预计时间：** 30 分钟  

**功能描述：**
- 将应用管理占位符替换为 AppManagementScreen
- 将提醒设置占位符替换为 RemindersScreen
- 将历史数据占位符替换为 HistoryScreen
- 配置 ViewModel 依赖注入

---

## 🟡 中优先级任务（重要功能）

### 任务组2：过滤系统（任务3剩余40%）

#### chart-01: 创建 FilterManager 类
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/charts/managers/FilterManager.kt`  
**预计时间：** 2 小时  

**功能描述：**
- 日期范围过滤（今天、昨天、本周、本月、自定义）
- 应用类别过滤（社交、娱乐、效率等）
- 应用名称搜索过滤
- 使用时长范围过滤
- 多条件组合过滤
- 过滤结果缓存

**API 设计：**
```kotlin
class FilterManager {
    fun applyFilters(
        data: List<AppUsageSummary>,
        dateRange: DateRange,
        categories: List<String>,
        searchQuery: String
    ): List<AppUsageSummary>
}
```

---

#### chart-02: 实现应用类别过滤 UI
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/statistics/components/CategoryFilterSection.kt`  
**预计时间：** 1.5 小时  

**功能描述：**
- 多选类别 FilterChip
- 支持类别：社交、娱乐、效率、工具、游戏、新闻、其他
- 全选/清空按钮
- 选中状态持久化

---

#### chart-03: 实现应用搜索功能
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/statistics/components/AppSearchBar.kt`  
**预计时间：** 1.5 小时  

**功能描述：**
- 实时搜索应用名称
- 搜索包名
- 搜索历史
- 清空按钮
- 搜索建议

---

#### chart-04: 实现智能分组算法
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/charts/utils/GroupingAlgorithm.kt`  
**预计时间：** 2 小时  

**功能描述：**
- 按使用频率智能分组
- Top N 应用单独显示
- 其余应用归入 "Others"
- 可配置分组阈值
- 类别自动分组

**算法逻辑：**
```kotlin
fun smartGroup(
    apps: List<AppUsageData>,
    topN: Int = 10,
    threshold: Float = 0.02f // 2%
): List<AppUsageData>
```

---

#### chart-05: 实现黑名单管理
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/charts/managers/BlacklistManager.kt`  
**预计时间：** 1.5 小时  

**功能描述：**
- 添加/移除黑名单应用
- 黑名单持久化（SharedPreferences）
- 系统应用默认黑名单
- 从统计中排除黑名单应用

---

#### chart-06: 创建 ComparisonViewController
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/statistics/ComparisonViewController.kt`  
**预计时间：** 3 小时  

**功能描述：**
- 时间段对比（本周 vs 上周）
- 应用对比（应用 A vs 应用 B）
- 并排显示图表
- 计算增长率和变化百分比
- 趋势分析

---

### 任务组4：实现提醒系统（任务5.1-5.3）

#### reminder-01: 创建 ReminderEngine 核心类
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/reminder/ReminderEngine.kt`  
**预计时间：** 3 小时  

**功能描述：**
- 监控全局使用阈值
- 监控每应用使用阈值
- 计算使用百分比
- 触发提醒通知（80%、90%、100%）
- 防止重复提醒（冷却期）
- 与 DataCollectionService 集成

**核心逻辑：**
```kotlin
class ReminderEngine {
    fun checkThresholds(
        currentUsage: Long,
        limit: Long,
        appName: String
    ): ReminderTrigger?
    
    fun sendReminder(trigger: ReminderTrigger)
}
```

---

#### reminder-02: 创建 UsageThreshold 模型
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/reminder/models/UsageThreshold.kt`  
**预计时间：** 30 分钟  

**功能描述：**
- 定义阈值配置数据结构
- 支持全局和每应用阈值
- 包含提醒级别（警告、严重）
- 包含通知设置

```kotlin
data class UsageThreshold(
    val id: String,
    val packageName: String?, // null 表示全局
    val dailyLimit: Long, // 毫秒
    val warningPercentages: List<Int> = listOf(80, 90, 100),
    val isEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val vibration: Boolean = true
)
```

---

#### reminder-03: 创建 ReminderNotificationManager
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/reminder/ReminderNotificationManager.kt`  
**预计时间：** 2 小时  

**功能描述：**
- 创建通知渠道
- 构建提醒通知
- 显示通知
- 处理通知点击
- 通知优先级管理

---

#### reminder-04: 创建 RestReminderService
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/reminder/RestReminderService.kt`  
**预计时间：** 2.5 小时  

**功能描述：**
- 跟踪连续使用会话
- 检测休息间隔
- 发送休息提醒
- 可配置间隔（30分钟、1小时）
- 自定义提醒消息

---

#### reminder-05: 创建 DoNotDisturbScheduler
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/reminder/DoNotDisturbScheduler.kt`  
**预计时间：** 2.5 小时  

**功能描述：**
- 免打扰时段管理
- 基于时间的规则（开始/结束时间）
- 星期选择（周一到周日）
- 应用白名单管理
- 检查当前是否在免打扰时段
- 过滤通知

---

#### reminder-06: 集成提醒系统到 DataCollectionService
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/service/DataCollectionService.kt`  
**预计时间：** 1 小时  

**功能描述：**
- 在数据收集循环中调用 ReminderEngine
- 传递当前使用数据
- 处理提醒触发

---

#### reminder-07: 创建提醒历史 DAO
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/data/dao/ReminderHistoryDao.kt`  
**预计时间：** 1 小时  

**功能描述：**
- 创建 ReminderHistory 实体
- 记录提醒发送时间
- 记录提醒类型和应用
- 查询提醒历史
- 统计提醒效果

---

### 任务组5：实现数据导出和备份（任务5.4-5.5）

#### export-01: 创建 BackupService
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/backup/BackupService.kt`  
**预计时间：** 3 小时  

**功能描述：**
- AES-256 加密备份文件
- 序列化用户设置
- 序列化使用历史（可选择时间范围）
- 序列化提醒配置
- 生成备份元数据（版本、时间戳、校验和）
- 压缩备份文件

---

#### export-02: 创建 RestoreService
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/backup/RestoreService.kt`  
**预计时间：** 3 小时  

**功能描述：**
- 解密备份文件
- 验证备份完整性（校验和）
- 反序列化数据
- 处理数据冲突（合并或覆盖）
- 数据验证
- 恢复进度通知

---

#### export-03: 创建 ExportManager
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/export/ExportManager.kt`  
**预计时间：** 2 小时  

**功能描述：**
- 管理多种导出格式
- 选择导出字段
- 选择导出时间范围
- 协调各格式导出器
- 导出进度管理

---

#### export-04: 实现 CSV 导出
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/export/CsvExporter.kt`  
**预计时间：** 1.5 小时  

**功能描述：**
- 导出使用记录为 CSV
- 可自定义列（应用名、时长、时间戳等）
- UTF-8 编码
- Excel 兼容格式

**CSV 格式示例：**
```
应用名称,包名,开始时间,结束时间,使用时长(分钟),类别
微信,com.tencent.mm,2025-01-12 10:00,2025-01-12 10:15,15,社交
```

---

#### export-05: 实现 PDF 导出
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/export/PdfExporter.kt`  
**预计时间：** 3 小时  

**功能描述：**
- 使用 iText 或类似库生成 PDF
- 包含使用统计表格
- 包含图表截图或重新渲染
- 格式化报告（标题、日期、汇总）
- 支持中文字体

**依赖：**
- 需要添加 PDF 生成库（如 iText）

---

#### export-06: 实现 JSON 导出
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/export/JsonExporter.kt`  
**预计时间：** 1 小时  

**功能描述：**
- 导出结构化 JSON 数据
- 使用 Gson 或 Kotlinx Serialization
- 包含完整数据结构
- 便于程序化处理

---

#### export-07: 创建导出进度 UI
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/export/ExportProgressDialog.kt`  
**预计时间：** 1.5 小时  

**功能描述：**
- 显示导出进度条
- 显示处理进度（已处理/总数）
- 预计完成时间
- 取消按钮
- 完成后打开文件选项

---

#### export-08: 实现批处理逻辑
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/export/BatchProcessor.kt`  
**预计时间：** 2 小时  

**功能描述：**
- 分批查询数据库（1000条/批）
- 流式处理大数据集
- 内存管理
- 进度回调

---

#### export-09: 创建备份/导出设置页面
**优先级：** 🟡 中  
**文件：** `app/src/main/java/com/example/time/ui/settings/BackupSettingsScreen.kt`  
**预计时间：** 2 小时  

**功能描述：**
- 立即备份按钮
- 恢复备份按钮
- 导出数据按钮（选择格式）
- 自动备份开关和频率设置
- 备份历史列表
- 数据清理选项

---

## 🟢 低优先级任务（体验优化）

### 任务组3：可访问性和本地化（任务4）

#### ui-08: 实现可访问性支持
**优先级：** 🟢 低  
**文件：** 所有 Screen 文件  
**预计时间：** 3-4 小时  

**功能描述：**
- 为所有 UI 元素添加 contentDescription
- 使用 semantics 修饰符
- 确保合理的焦点顺序
- 支持 TalkBack 屏幕阅读器
- 键盘导航支持

**示例：**
```kotlin
Icon(
    imageVector = Icons.Default.Home,
    contentDescription = stringResource(R.string.home_icon_description),
    modifier = Modifier.semantics {
        role = Role.Image
    }
)
```

---

#### ui-09: 实现高对比度模式
**优先级：** 🟢 低  
**文件：** `app/src/main/java/com/example/time/ui/theme/Theme.kt`  
**预计时间：** 2 小时  

**功能描述：**
- 创建高对比度配色方案
- 添加设置开关
- 持久化用户选择
- 确保 WCAG AA 标准

---

#### ui-10: 实现双语支持（中文/英文）
**优先级：** 🟢 低  
**文件：** `app/src/main/res/values/strings.xml` 和 `app/src/main/res/values-en/strings.xml`  
**预计时间：** 4-5 小时  

**功能描述：**
- 提取所有硬编码字符串
- 创建中文资源文件（values/strings.xml）
- 创建英文资源文件（values-en/strings.xml）
- 确保复数、格式化字符串正确

**字符串数量估计：** 200-300 个

---

#### ui-11: 添加语言切换功能
**优先级：** 🟢 低  
**文件：** `app/src/main/java/com/example/time/ui/settings/LanguageSelector.kt`  
**预计时间：** 1.5 小时  

**功能描述：**
- 创建语言选择器 UI
- 支持中文和英文切换
- 持久化语言选择
- 应用语言更改（需要重启应用）

---

### 任务组6：测试和优化

#### test-01: 编写数据库单元测试
**优先级：** 🟢 低  
**文件：** `app/src/test/java/com/example/time/DatabaseTest.kt`  
**预计时间：** 3 小时  

**测试内容：**
- 所有 DAO 操作
- 数据库迁移
- 事务处理
- 索引性能

---

#### test-02: 编写 Repository 测试
**优先级：** 🟢 低  
**文件：** `app/src/test/java/com/example/time/RepositoryTest.kt`  
**预计时间：** 2 小时  

**测试内容：**
- Repository 方法逻辑
- 错误处理
- 协程使用

---

#### test-03: 编写 ViewModel 测试
**优先级：** 🟢 低  
**文件：** `app/src/test/java/com/example/time/ViewModelTest.kt`  
**预计时间：** 2 小时  

**测试内容：**
- 业务逻辑
- 状态管理
- LiveData/StateFlow 转换

---

#### test-04: UI 集成测试
**优先级：** 🟢 低  
**文件：** `app/src/androidTest/java/com/example/time/UiTest.kt`  
**预计时间：** 3 小时  

**测试内容：**
- 导航流程
- 用户交互
- 数据显示

---

#### test-05: 性能优化 - 图表渲染
**优先级：** 🟢 低  
**预计时间：** 2 小时  

**优化内容：**
- 确保图表渲染 <300ms
- 数据采样优化
- 内存使用优化
- 使用 Profiler 分析

---

#### test-06: 性能优化 - 数据库查询
**优先级：** 🟢 低  
**预计时间：** 2 小时  

**优化内容：**
- 复杂查询优化
- 添加必要索引
- 使用 EXPLAIN QUERY PLAN
- 批量操作优化

---

## 📅 建议实施顺序

### 第一阶段：完善核心（1周）
1. arch-01, arch-02, arch-03（完善 Repository 层）
2. chart-07（图表集成）
3. ui-01, ui-02（应用管理页面）
4. ui-07（更新导航）

### 第二阶段：关键功能（2周）
5. ui-03, ui-04（提醒设置页面）
6. ui-05, ui-06（历史数据页面）
7. reminder-01 到 reminder-07（提醒系统）

### 第三阶段：进阶功能（1-2周）
8. chart-01 到 chart-06（过滤系统）
9. export-01 到 export-09（数据导出和备份）

### 第四阶段：优化完善（1周）
10. ui-08 到 ui-11（可访问性和本地化）
11. test-01 到 test-06（测试和优化）

---

## 📝 备注

- 所有待办项均已按优先级和依赖关系排序
- 预计总工作量：55-70 小时
- 建议采用敏捷开发方式，每完成一个任务组进行测试
- 关键文件已验证存在且功能完整：
  - ✅ TimeDatabase.kt（SQLCipher 加密正确实现）
  - ✅ 所有 Manager 类（BarChart, CalendarHeatmap, Timeline）
  - ✅ 现有 Screen 文件（Home, Statistics, Onboarding, Permissions）

**最后更新：** 2025-01-12

