# Design: 智慧播放清單技術設計

> **Change ID**: `add-smart-playlist`
> **版本**: 1.0
> **建立日期**: 2026-01-05

---

## 1. 系統架構

### 1.1 元件圖

```
┌─────────────────────────────────────────────────────────────────┐
│                          UI Layer                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              SmartPlaylistEditorScreen                   │    │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐    │    │
│  │  │ Name/Icon   │ │ Rule        │ │ Live Preview   │    │    │
│  │  │ Editor      │ │ Builder     │ │ List           │    │    │
│  │  └─────────────┘ └─────────────┘ └─────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              SmartPlaylistEditorViewModel                │    │
│  │  • name: MutableState<String>                            │    │
│  │  • icon: MutableState<String>                            │    │
│  │  • rules: MutableState<List<RuleCondition>>              │    │
│  │  • logic: MutableState<RuleLogic>                        │    │
│  │  • sortBy: MutableState<SortOption>                      │    │
│  │  • limit: MutableState<Int?>                             │    │
│  │  • previewSongs: StateFlow<List<Song>>                   │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    RuleCondition (Sealed)                  │  │
│  │  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐    │  │
│  │  │ Duration      │ │ PlayCount     │ │ AddedDate     │    │  │
│  │  │ • operator    │ │ • operator    │ │ • operator    │    │  │
│  │  │ • value       │ │ • value       │ │ • value       │    │  │
│  │  └───────────────┘ └───────────────┘ └───────────────┘    │  │
│  │  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐    │  │
│  │  │ LastPlayed    │ │ Artist        │ │ Album         │    │  │
│  │  │ • operator    │ │ • operator    │ │ • operator    │    │  │
│  │  │ • value       │ │ • value       │ │ • value       │    │  │
│  │  └───────────────┘ └───────────────┘ └───────────────┘    │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                SmartPlaylistQueryEngine                    │  │
│  │  + buildSqlQuery(rules, logic, sort, limit): RawQuery     │  │
│  │  + evaluateSong(song, rules, logic): Boolean              │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      UseCases                              │  │
│  │  ┌─────────────────┐  ┌──────────────────────────────┐    │  │
│  │  │ CreateSmart     │  │ GetSmartPlaylistSongs        │    │  │
│  │  │ Playlist        │  │ UseCase                      │    │  │
│  │  └─────────────────┘  └──────────────────────────────┘    │  │
│  │  ┌─────────────────┐  ┌──────────────────────────────┐    │  │
│  │  │ UpdateSmart     │  │ GetSystemSmartPlaylists      │    │  │
│  │  │ Playlist        │  │ UseCase                      │    │  │
│  │  └─────────────────┘  └──────────────────────────────┘    │  │
│  │  ┌─────────────────┐                                      │  │
│  │  │ DeleteSmart     │                                      │  │
│  │  │ Playlist        │                                      │  │
│  │  └─────────────────┘                                      │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Data Layer                               │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │               SmartPlaylistRepositoryImpl                │    │
│  │  + save(playlist): Long                                  │    │
│  │  + getAll(): Flow<List<SmartPlaylist>>                   │    │
│  │  + getSongsForPlaylist(id): Flow<List<Song>>             │    │
│  │  + delete(id): Unit                                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Room Database                         │    │
│  │  ┌──────────────────┐  ┌────────────────────────────┐   │    │
│  │  │ smart_playlists  │  │ smart_playlist_rules       │   │    │
│  │  │ • id             │  │ • id                       │   │    │
│  │  │ • name           │  │ • playlist_id (FK)         │   │    │
│  │  │ • icon           │  │ • condition_type           │   │    │
│  │  │ • logic          │  │ • operator                 │   │    │
│  │  │ • sort_by        │  │ • value_string             │   │    │
│  │  │ • sort_order     │  │ • value_int                │   │    │
│  │  │ • limit          │  │ • value_long               │   │    │
│  │  │ • is_system      │  │ • order_index              │   │    │
│  │  │ • created_at     │  │                            │   │    │
│  │  └──────────────────┘  └────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 資料流 - 智慧清單查詢

```
User opens Smart Playlist
        │
        ▼
┌─────────────────┐
│ Load playlist   │
│ definition      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Parse rules     │
│ from database   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ QueryEngine     │
│ builds SQL      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Execute query   │
│ against songs   │
│ + song_stats    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Return matching │
│ songs as Flow   │
└─────────────────┘
```

---

## 2. 資料模型

### 2.1 Room Entities

```kotlin
@Entity(tableName = "smart_playlists")
data class SmartPlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String,  // Emoji or icon identifier

    @ColumnInfo(name = "logic")
    val logic: String,  // AND, OR

    @ColumnInfo(name = "sort_by")
    val sortBy: String,  // TITLE, ARTIST, DATE_ADDED, PLAY_COUNT, etc.

    @ColumnInfo(name = "sort_order")
    val sortOrder: String,  // ASC, DESC

    @ColumnInfo(name = "limit_count")
    val limitCount: Int?,  // null = no limit

    @ColumnInfo(name = "is_system")
    val isSystem: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "smart_playlist_rules",
    foreignKeys = [
        ForeignKey(
            entity = SmartPlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SmartPlaylistRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "playlist_id", index = true)
    val playlistId: Long,

    @ColumnInfo(name = "condition_type")
    val conditionType: String,  // DURATION, PLAY_COUNT, ADDED_DATE, etc.

    @ColumnInfo(name = "operator")
    val operator: String,  // EQUALS, GREATER_THAN, LESS_THAN, CONTAINS, etc.

    @ColumnInfo(name = "value_string")
    val valueString: String? = null,

    @ColumnInfo(name = "value_int")
    val valueInt: Int? = null,

    @ColumnInfo(name = "value_long")
    val valueLong: Long? = null,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0
)
```

### 2.2 Domain Models

```kotlin
@Immutable
data class SmartPlaylist(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val rules: List<RuleCondition>,
    val logic: RuleLogic,
    val sortBy: SortOption,
    val sortOrder: SortOrder,
    val limit: Int?,
    val isSystem: Boolean,
    val createdAt: Long,
    val songCount: Int = 0  // Cached/calculated
)

enum class RuleLogic { AND, OR }

enum class SortOption {
    TITLE, ARTIST, ALBUM, DATE_ADDED, LAST_PLAYED,
    PLAY_COUNT, DURATION, RANDOM
}

enum class SortOrder { ASC, DESC }

sealed class RuleCondition {
    abstract val operator: RuleOperator

    // Duration in milliseconds
    data class Duration(
        override val operator: RuleOperator,
        val valueMs: Long
    ) : RuleCondition()

    // Play count from stats
    data class PlayCount(
        override val operator: RuleOperator,
        val value: Int
    ) : RuleCondition()

    // Skip count from stats
    data class SkipCount(
        override val operator: RuleOperator,
        val value: Int
    ) : RuleCondition()

    // Date song was added
    data class AddedDate(
        override val operator: RuleOperator,
        val daysAgo: Int  // Relative days
    ) : RuleCondition()

    // Last played date from stats
    data class LastPlayed(
        override val operator: RuleOperator,
        val daysAgo: Int
    ) : RuleCondition()

    // Artist name
    data class Artist(
        override val operator: RuleOperator,
        val value: String
    ) : RuleCondition()

    // Album name
    data class Album(
        override val operator: RuleOperator,
        val value: String
    ) : RuleCondition()

    // Song title
    data class Title(
        override val operator: RuleOperator,
        val value: String
    ) : RuleCondition()

    // Genre
    data class Genre(
        override val operator: RuleOperator,
        val value: String
    ) : RuleCondition()

    // Year
    data class Year(
        override val operator: RuleOperator,
        val value: Int
    ) : RuleCondition()

    // Is favorited
    data class IsFavorite(
        val value: Boolean
    ) : RuleCondition() {
        override val operator = RuleOperator.EQUALS
    }
}

enum class RuleOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_OR_EQUAL,
    LESS_OR_EQUAL,
    CONTAINS,
    NOT_CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    IN_LAST_N_DAYS,
    NOT_IN_LAST_N_DAYS
}
```

---

## 3. 規則查詢引擎

### 3.1 SQL 查詢建構

```kotlin
class SmartPlaylistQueryEngine @Inject constructor() {

    fun buildQuery(
        rules: List<RuleCondition>,
        logic: RuleLogic,
        sortBy: SortOption,
        sortOrder: SortOrder,
        limit: Int?
    ): SimpleSQLiteQuery {
        val whereClause = buildWhereClause(rules, logic)
        val orderClause = buildOrderClause(sortBy, sortOrder)
        val limitClause = limit?.let { "LIMIT $it" } ?: ""

        val sql = """
            SELECT s.* FROM songs s
            LEFT JOIN song_stats ss ON s.id = ss.song_id
            LEFT JOIN favorites f ON s.id = f.song_id
            ${if (whereClause.isNotBlank()) "WHERE $whereClause" else ""}
            ORDER BY $orderClause
            $limitClause
        """.trimIndent()

        return SimpleSQLiteQuery(sql)
    }

    private fun buildWhereClause(
        rules: List<RuleCondition>,
        logic: RuleLogic
    ): String {
        val clauses = rules.mapNotNull { ruleToSql(it) }
        return clauses.joinToString(
            separator = if (logic == RuleLogic.AND) " AND " else " OR ",
            prefix = "(",
            postfix = ")"
        )
    }

    private fun ruleToSql(rule: RuleCondition): String? {
        return when (rule) {
            is RuleCondition.Duration -> {
                val column = "s.duration"
                operatorToSql(column, rule.operator, rule.valueMs.toString())
            }

            is RuleCondition.PlayCount -> {
                val column = "COALESCE(ss.play_count, 0)"
                operatorToSql(column, rule.operator, rule.value.toString())
            }

            is RuleCondition.AddedDate -> {
                val column = "s.date_added"
                val timestamp = System.currentTimeMillis() -
                    (rule.daysAgo * 24 * 60 * 60 * 1000L)
                when (rule.operator) {
                    RuleOperator.IN_LAST_N_DAYS -> "$column >= $timestamp"
                    RuleOperator.NOT_IN_LAST_N_DAYS -> "$column < $timestamp"
                    else -> null
                }
            }

            is RuleCondition.LastPlayed -> {
                val column = "COALESCE(ss.last_played_at, 0)"
                val timestamp = System.currentTimeMillis() -
                    (rule.daysAgo * 24 * 60 * 60 * 1000L)
                when (rule.operator) {
                    RuleOperator.IN_LAST_N_DAYS -> "$column >= $timestamp"
                    RuleOperator.NOT_IN_LAST_N_DAYS -> "$column < $timestamp"
                    else -> null
                }
            }

            is RuleCondition.Artist -> {
                val column = "s.artist"
                stringOperatorToSql(column, rule.operator, rule.value)
            }

            is RuleCondition.Album -> {
                val column = "s.album"
                stringOperatorToSql(column, rule.operator, rule.value)
            }

            is RuleCondition.IsFavorite -> {
                if (rule.value) "f.song_id IS NOT NULL"
                else "f.song_id IS NULL"
            }

            // ... other cases
            else -> null
        }
    }

    private fun operatorToSql(
        column: String,
        operator: RuleOperator,
        value: String
    ): String? {
        return when (operator) {
            RuleOperator.EQUALS -> "$column = $value"
            RuleOperator.NOT_EQUALS -> "$column != $value"
            RuleOperator.GREATER_THAN -> "$column > $value"
            RuleOperator.LESS_THAN -> "$column < $value"
            RuleOperator.GREATER_OR_EQUAL -> "$column >= $value"
            RuleOperator.LESS_OR_EQUAL -> "$column <= $value"
            else -> null
        }
    }

    private fun stringOperatorToSql(
        column: String,
        operator: RuleOperator,
        value: String
    ): String? {
        val escaped = value.replace("'", "''")
        return when (operator) {
            RuleOperator.EQUALS -> "$column = '$escaped'"
            RuleOperator.NOT_EQUALS -> "$column != '$escaped'"
            RuleOperator.CONTAINS -> "$column LIKE '%$escaped%'"
            RuleOperator.NOT_CONTAINS -> "$column NOT LIKE '%$escaped%'"
            RuleOperator.STARTS_WITH -> "$column LIKE '$escaped%'"
            RuleOperator.ENDS_WITH -> "$column LIKE '%$escaped'"
            else -> null
        }
    }

    private fun buildOrderClause(
        sortBy: SortOption,
        sortOrder: SortOrder
    ): String {
        val column = when (sortBy) {
            SortOption.TITLE -> "s.title"
            SortOption.ARTIST -> "s.artist"
            SortOption.ALBUM -> "s.album"
            SortOption.DATE_ADDED -> "s.date_added"
            SortOption.LAST_PLAYED -> "COALESCE(ss.last_played_at, 0)"
            SortOption.PLAY_COUNT -> "COALESCE(ss.play_count, 0)"
            SortOption.DURATION -> "s.duration"
            SortOption.RANDOM -> "RANDOM()"
        }
        val order = sortOrder.name
        return "$column $order"
    }
}
```

---

## 4. 系統預設智慧清單

```kotlin
object SystemSmartPlaylists {

    val recentlyAdded = SmartPlaylist(
        id = -1,
        name = "最近添加",
        icon = "🕐",
        rules = listOf(
            RuleCondition.AddedDate(RuleOperator.IN_LAST_N_DAYS, 30)
        ),
        logic = RuleLogic.AND,
        sortBy = SortOption.DATE_ADDED,
        sortOrder = SortOrder.DESC,
        limit = null,
        isSystem = true,
        createdAt = 0
    )

    val mostPlayed = SmartPlaylist(
        id = -2,
        name = "最常播放",
        icon = "🔥",
        rules = listOf(
            RuleCondition.PlayCount(RuleOperator.GREATER_THAN, 0)
        ),
        logic = RuleLogic.AND,
        sortBy = SortOption.PLAY_COUNT,
        sortOrder = SortOrder.DESC,
        limit = 50,
        isSystem = true,
        createdAt = 0
    )

    val longUnplayed = SmartPlaylist(
        id = -3,
        name = "很久沒聽",
        icon = "💎",
        rules = listOf(
            RuleCondition.LastPlayed(RuleOperator.NOT_IN_LAST_N_DAYS, 60)
        ),
        logic = RuleLogic.AND,
        sortBy = SortOption.RANDOM,
        sortOrder = SortOrder.ASC,
        limit = null,
        isSystem = true,
        createdAt = 0
    )

    val shortSongs = SmartPlaylist(
        id = -4,
        name = "短歌曲",
        icon = "⚡",
        rules = listOf(
            RuleCondition.Duration(RuleOperator.LESS_THAN, 3 * 60 * 1000L)
        ),
        logic = RuleLogic.AND,
        sortBy = SortOption.DURATION,
        sortOrder = SortOrder.ASC,
        limit = null,
        isSystem = true,
        createdAt = 0
    )

    val longSongs = SmartPlaylist(
        id = -5,
        name = "長歌曲",
        icon = "🎸",
        rules = listOf(
            RuleCondition.Duration(RuleOperator.GREATER_THAN, 6 * 60 * 1000L)
        ),
        logic = RuleLogic.AND,
        sortBy = SortOption.DURATION,
        sortOrder = SortOrder.DESC,
        limit = null,
        isSystem = true,
        createdAt = 0
    )

    val favorites = SmartPlaylist(
        id = -6,
        name = "我的最愛",
        icon = "❤️",
        rules = listOf(
            RuleCondition.IsFavorite(true)
        ),
        logic = RuleLogic.AND,
        sortBy = SortOption.TITLE,
        sortOrder = SortOrder.ASC,
        limit = null,
        isSystem = true,
        createdAt = 0
    )

    val all = listOf(
        recentlyAdded, mostPlayed, longUnplayed,
        shortSongs, longSongs, favorites
    )
}
```

---

## 5. UI 設計詳細

### 5.1 規則條件編輯器元件

```kotlin
@Composable
fun RuleConditionEditor(
    rule: RuleCondition,
    onRuleChange: (RuleCondition) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Condition Type Dropdown
        ConditionTypeDropdown(
            selectedType = rule.conditionType,
            onTypeChange = { /* recreate rule with new type */ }
        )

        Spacer(Modifier.width(8.dp))

        // Operator Dropdown (context-sensitive)
        OperatorDropdown(
            conditionType = rule.conditionType,
            selectedOperator = rule.operator,
            onOperatorChange = { /* update rule */ }
        )

        Spacer(Modifier.width(8.dp))

        // Value Input (type-specific)
        when (rule) {
            is RuleCondition.Duration -> DurationInput(...)
            is RuleCondition.PlayCount -> NumberInput(...)
            is RuleCondition.Artist -> TextInput(...)
            is RuleCondition.AddedDate -> DaysInput(...)
            // ...
        }

        Spacer(Modifier.weight(1f))

        // Delete Button
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Delete rule")
        }
    }
}
```

### 5.2 即時預覽

```kotlin
@Composable
fun LivePreview(
    songs: List<Song>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "符合條件：${songs.size} 首歌曲",
                style = MaterialTheme.typography.titleSmall
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(songs.take(5)) { song ->
                PreviewSongItem(song)
            }
            if (songs.size > 5) {
                item {
                    TextButton(onClick = { /* show all */ }) {
                        Text("顯示更多...")
                    }
                }
            }
        }
    }
}
```

---

## 6. 效能優化

### 6.1 查詢效能

- 使用 `@RawQuery` 執行動態查詢
- 確保 `song_stats.song_id` 和 `favorites.song_id` 有索引
- 使用 `COALESCE` 處理 NULL 值避免 JOIN 過濾

### 6.2 預覽防抖

```kotlin
class SmartPlaylistEditorViewModel : ViewModel() {
    private val rulesFlow = MutableStateFlow<List<RuleCondition>>(emptyList())

    val previewSongs = rulesFlow
        .debounce(300)  // 防抖 300ms
        .flatMapLatest { rules ->
            if (rules.isEmpty()) {
                flowOf(emptyList())
            } else {
                repository.getSongsMatching(rules, logic.value)
                    .map { it.take(20) }  // 預覽限制 20 首
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
```

---

## 7. 測試策略

### 7.1 單元測試
- `SmartPlaylistQueryEngine` SQL 生成測試
- `RuleCondition` 序列化/反序列化測試
- 各種 operator 組合測試

### 7.2 整合測試
- 完整查詢流程測試
- 系統預設清單查詢測試
- 複雜多條件組合測試

### 7.3 效能測試
- 1000 首歌曲 + 5 條規則 < 200ms
- 10000 首歌曲 + 5 條規則 < 1s

---

## 8. 未來擴展

1. **巢狀規則群組**：支援 (A AND B) OR (C AND D) 複雜邏輯
2. **規則範本**：預設常用規則組合
3. **匯入/匯出**：JSON 格式匯出規則定義
4. **規則共享**：與其他使用者分享規則
