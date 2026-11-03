package com.example.rem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rem.ui.theme.RemTheme

private val Ink = Color(0xFF27272B)
private val Muted = Color(0xFF7D7D86)
private val Blue = Color(0xFF1C4FE3)
private val Pale = Color(0xFFF2F2F3)
private val Line = Color(0xFFE4E4E6)
private val Page = Color(0xFFF6F6F7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RemTheme { RelayApp() } }
    }
}

private data class ThreadItem(val title: String, val project: String, val time: String, val status: String)
private val threads = listOf(
    ThreadItem("Map C++ APIs to Header Paths", "KL", "1d", "Done"),
    ThreadItem("Improve Project API Design", "KL", "1d", "Needs review"),
    ThreadItem("Improve Project API Design", "KL", "1d", "Needs review"),
    ThreadItem("Improve Project API Design", "KL", "1d", "Needs review")
)

@Composable
private fun RelayApp() {
    var page by remember { mutableStateOf("Threads") }
    var draft by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("Codex (Personal) · GPT-6-Luna") }
    val back = remember { mutableStateListOf<String>() }
    fun go(target: String) { back.add(page); page = target }
    fun up() { if (back.isNotEmpty()) page = back.removeAt(back.lastIndex) else page = "Threads" }
    BackHandler(page !in listOf("Threads", "Pull requests", "Usage", "Settings")) { up() }
    val root = page in listOf("Threads", "Pull requests", "Usage", "Settings")
    Scaffold(
        containerColor = Page,
        bottomBar = { if (root) BottomNav(page) { page = it; back.clear() } },
        floatingActionButton = {
            if (page == "Threads") ExtendedFloatingActionButton(
                onClick = { go("New thread") },
                shape = RoundedCornerShape(18.dp),
                containerColor = Blue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Edit, null) },
                text = { Text("New thread", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (page) {
                "Threads" -> ThreadsPage(::go)
                "Pull requests" -> PullRequestsPage(::go)
                "Usage" -> UsagePage(::go)
                "Settings" -> SettingsPage(::go)
                "New thread" -> NewThreadPage(::up, ::go, draft, { draft = it }, selectedModel)
                "Thread detail" -> ThreadPage(::up, ::go, draft, { draft = it }, selectedModel)
                "Thread settings" -> ModelPickerPage(::up, selectedModel) { selectedModel = it; up() }
                "Workspace detail" -> WorkspacePage(::up, ::go)
                else -> DetailPage(page, ::up, ::go)
            }
        }
    }
}

@Composable
private fun Top(title: String, back: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().height(66.dp).background(Page).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Back", tint = Ink) }
        if (title == "Relay") {
            Text("Relay", color = Ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(7.dp))
            Text("BETA", Modifier.background(Color.White, RoundedCornerShape(14.dp)).padding(horizontal = 7.dp, vertical = 3.dp), color = Muted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
        } else {
            Text(title, Modifier.weight(1f), color = Ink, style = if (back == null) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        actions()
    }
}

@Composable
private fun BottomNav(selected: String, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = Color(0xFFEAEAEA), tonalElevation = 0.dp) {
        listOf(
            Triple("Threads", Icons.Default.ChatBubbleOutline, "Chats"),
            Triple("Pull requests", Icons.Default.AccountTree, "Pulls"),
            Triple("Usage", Icons.Default.BarChart, "Usage"),
            Triple("Settings", Icons.Default.Settings, "Settings")
        ).forEach { (route, icon, label) ->
                NavigationBarItem(
                    selected = selected == route,
                    onClick = { onSelect(route) },
                    icon = { Icon(icon, label) },
                    label = { Text(label, maxLines = 1, fontSize = 12.sp) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Ink,
                        selectedTextColor = Ink,
                        indicatorColor = Color(0xFFC8C8C9),
                        unselectedIconColor = Ink,
                        unselectedTextColor = Ink
                    )
                )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, Modifier.padding(start = 14.dp, top = 18.dp, bottom = 8.dp), color = Muted, fontSize = 14.sp)
}

@Composable
private fun ListRow(icon: ImageVector, title: String, subtitle: String? = null, trailing: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) Text(trailing, color = Muted, style = MaterialTheme.typography.bodySmall)
        Icon(Icons.Default.ChevronRight, null, tint = Muted, modifier = Modifier.size(20.dp))
    }
    HorizontalDivider(Modifier.padding(start = 64.dp, end = 24.dp), color = Line)
}

@Composable
private fun RoundedContent(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)), content = content)
}

@Composable
private fun ThreadsPage(go: (String) -> Unit) {
    var workingExpanded by remember { mutableStateOf(false) }
    var settledExpanded by remember { mutableStateOf(false) }
    Top("Relay") {
        IconButton(onClick = { go("Search") }) { Icon(Icons.Default.Search, "Search", tint = Ink) }
        IconButton(onClick = { go("Filters") }) { Icon(Icons.Outlined.FilterAlt, "Filters", tint = Ink) }
    }
    RoundedContent {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
            items(threads) { thread -> ThreadRow(thread) { go("Thread detail") } }
            item { CollapsibleLabel("Working (1)", workingExpanded) { workingExpanded = !workingExpanded } }
            if (workingExpanded) item { ThreadRow(ThreadItem("Review build output", "KL", "1h", "Running")) { go("Thread detail") } }
            item { CollapsibleLabel("Settled (40)", settledExpanded) { settledExpanded = !settledExpanded } }
            if (settledExpanded) items(listOf("Export flow notes and transcripts", "Check tool availability", "Fix browser launch")) { name ->
                ThreadRow(ThreadItem(name, "KL", "2d", "Done")) { go("Thread detail") }
            }
        }
    }
}

@Composable
private fun CollapsibleLabel(label: String, expanded: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(Modifier.weight(1f), color = Line)
        Spacer(Modifier.width(8.dp))
        Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Muted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ThreadRow(item: ThreadItem, click: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(58.dp).clickable(onClick = click).padding(horizontal = 17.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(20.dp).background(Color(0xFFDCE9FF), RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
            Text(item.project, color = Color(0xFF3284FD), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(8.dp))
        Text(item.title, Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Column(horizontalAlignment = Alignment.End) {
            Text(item.time, color = Muted, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(5.dp))
            Box(Modifier.size(18.dp)) {
                Box(Modifier.offset(x = 2.dp, y = 1.dp).size(10.dp).border(1.dp, Muted, CircleShape))
                Box(Modifier.offset(x = 5.dp, y = 3.dp).size(10.dp).border(1.dp, Muted, CircleShape))
                Box(Modifier.align(Alignment.BottomEnd).size(9.dp).background(if (item.status == "Running") Blue else if (item.status == "Needs review") Color(0xFFF02D32) else Muted, CircleShape), contentAlignment = Alignment.Center) {
                    Text("C", color = Color.White, fontSize = 6.sp, lineHeight = 6.sp)
                }
            }
        }
    }
}

@Composable
private fun ThreadPage(back: () -> Unit, go: (String) -> Unit, draft: String, onDraftChange: (String) -> Unit, model: String) {
    Row(Modifier.fillMaxWidth().height(72.dp).background(Page).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Back", tint = Ink) }
        Column(Modifier.weight(1f)) {
            Text("Map C++ APIs to Header Paths", color = Ink, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("kernel · completed", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { go("Files") }) { Icon(Icons.Default.FolderOpen, "Files", tint = Ink) }
        IconButton(onClick = { go("Thread options") }) { Icon(Icons.Default.MoreVert, "More options", tint = Ink) }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Spacer(Modifier.height(160.dp))
            Box(Modifier.align(Alignment.End).widthIn(max = 285.dp).background(Color(0xFFF0F0F1), RoundedCornerShape(20.dp)).padding(14.dp)) {
                Text("Give me a rule of thumb for mapping API namespaces, functions and classes to C++ header paths in this codebase.", color = Ink, style = MaterialTheme.typography.bodyLarge)
            }
            Text("8:24 PM  ▢", Modifier.align(Alignment.End).padding(top = 8.dp, bottom = 32.dp), color = Muted, style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth().clickable { go("Work log") }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Reviewed public headers and coding guidelines", color = Muted, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ChevronRight, null, tint = Muted, modifier = Modifier.size(20.dp))
            }
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(4.dp))
            Text("Use one header per coherent caller-facing interface. Name the file for the concept a caller expects to include, and place it under the owning subsystem.", color = Ink, style = MaterialTheme.typography.bodyLarge, lineHeight = 25.sp)
            Spacer(Modifier.height(12.dp))
            Text("For example, Boot and its supporting types belong in <kernel/boot/boot.hpp>. A shared <kernel/cpu/halt.hpp> declaration can have an implementation for each architecture. The exception header now lives at <kernel/interrupts/exceptions.hpp>.", color = Ink, style = MaterialTheme.typography.bodyLarge, lineHeight = 25.sp)
            Row(Modifier.padding(top = 22.dp, bottom = 26.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCopy, null, tint = Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(22.dp))
                Icon(Icons.Default.AccountTree, null, tint = Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(18.dp))
                Text("8:24 PM", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Composer(draft, onDraftChange, model, { go("Thread settings") })
    }
}

@Composable
private fun WorkspacesPage(go: (String) -> Unit) {
    Top("Workspaces") { IconButton(onClick = { go("New workspace") }) { Icon(Icons.Default.Add, "New workspace") } }
    LazyColumn {
        item { Text("Pick a project and continue from anywhere.", Modifier.padding(horizontal = 24.dp, vertical = 12.dp), color = Muted) }
        item { SectionLabel("Projects") }
        items(listOf("Web workspace" to "3 active threads · Connected", "Mobile app" to "1 active thread · Connected", "API service" to "2 threads · Offline")) { (name, sub) ->
            ListRow(Icons.Default.FolderOpen, name, sub) { go("Workspace detail") }
        }
        item { SectionLabel("Remote environments") }
        item { ListRow(Icons.Default.Computer, "Desktop environment", "Connected · Ready", "●") { go("Environments") } }
        item { ListRow(Icons.Default.Computer, "Development server", "Offline", "○") { go("Environments") } }
        item { SectionLabel("Code review") }
        item { ListRow(Icons.Default.AccountTree, "Pull requests", "View changes across projects") { go("Pull requests") } }
    }
}

@Composable
private fun PullRequestsPage(go: (String) -> Unit) {
    Top("Relay") {
        IconButton(onClick = { go("Search pull requests") }) { Icon(Icons.Default.Search, "Search", tint = Ink) }
        IconButton(onClick = { go("Pull request filters") }) { Icon(Icons.Outlined.FilterAlt, "Filters", tint = Ink) }
    }
    RoundedContent {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 12.dp, top = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Open", "All PRs", "Labels", "Author").forEach { label ->
                Row(Modifier.background(Color(0xFFFCFCFC), RoundedCornerShape(20.dp)).clickable { go("Filter by $label") }.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Muted, modifier = Modifier.size(17.dp))
                }
            }
        }
        Column(Modifier.fillMaxWidth().weight(1f).padding(top = 58.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No matching pull requests", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Pull requests from the selected\nenvironments' projects appear here.", color = Muted, style = MaterialTheme.typography.bodyLarge, lineHeight = 27.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun WorkspacePage(back: () -> Unit, go: (String) -> Unit) {
    Top("Web workspace", back)
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text("Connected to desktop environment", Modifier.padding(horizontal = 24.dp, vertical = 10.dp), color = Color(0xFF20865F))
        SectionLabel("Continue working")
        ListRow(Icons.Default.ChatBubbleOutline, "Export flow notes", "Agent running · 19m") { go("Thread detail") }
        ListRow(Icons.Default.ChatBubbleOutline, "MCP command", "Agent running · 17m") { go("Thread detail") }
        SectionLabel("Project")
        ListRow(Icons.Default.FolderOpen, "Files", "Browse project files") { go("Files") }
        ListRow(Icons.Default.AccountTree, "Branches", "main · 2 worktrees") { go("Branches") }
        ListRow(Icons.Default.Description, "Pull requests", "2 open") { go("Pull requests") }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { go("New thread") }, Modifier.padding(horizontal = 24.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("New thread") }
    }
}

@Composable
private fun UsagePage(go: (String) -> Unit) {
    var tab by remember { mutableStateOf("Cost") }
    Top("Usage") { IconButton(onClick = { go("Usage filters") }) { Icon(Icons.Outlined.FilterAlt, "Filters", tint = Ink) } }
    RoundedContent {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(14.dp))
            SegmentedRow(listOf("Cost", "Tokens", "Limits"), tab) { tab = it }
            Spacer(Modifier.height(20.dp))
            if (tab == "Limits") {
                Text("Codex", Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.titleMedium)
                listOf(Triple("Session", "41%", "5m"), Triple("Weekly", "89%", "6d 2h"), Triple("Monthly", "79%", "28d 12h")).forEach { (name, value, reset) ->
                    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).background(Pale, RoundedCornerShape(24.dp)).padding(16.dp)) {
                        Row { Text(name, Modifier.weight(1f), color = Ink); Text(if (name == "Session") "Under pace" else "On pace", color = Muted) }
                        Spacer(Modifier.height(8.dp))
                        Text("$value left", color = Ink, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))
                        LinearProgressIndicator(progress = { if (name == "Session") .41f else if (name == "Weekly") .89f else .79f }, modifier = Modifier.fillMaxWidth(), color = Color(0xFFF5ABAF), trackColor = Color.White)
                        Spacer(Modifier.height(18.dp))
                        Text("Codex (Personal)  ·  resets in $reset", color = Muted)
                    }
                }
            } else {
                SegmentedRow(listOf("24h", "7d", "30d", "90d"), "30d") { go("Usage period") }
                Spacer(Modifier.height(6.dp))
                Column(Modifier.fillMaxWidth().background(Pale, RoundedCornerShape(24.dp)).padding(14.dp)) {
                    Text(if (tab == "Cost") "Raw token cost" else "Processed tokens", color = Muted)
                    Text(if (tab == "Cost") "$71.37*" else "301M", color = Ink, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text(if (tab == "Cost") "* if billed at full API rate" else "Across all providers", color = Muted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                        listOf(0, 0, 0, 0, 0, 0, 0, 0, 2, 18, 7, 0, 0, 0, 0, 165, 4, 0, 0, 26, 0, 18, 62, 38, 46, 8).forEach { h ->
                            Box(Modifier.width(10.dp).height((h + 1).dp).background(Color(0xFF3C3C42), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Oct 4", color = Muted); Text("Nov 2", color = Muted) }
                    Spacer(Modifier.height(8.dp))
                    Text("● Codex", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                Text("Providers", Modifier.padding(top = 20.dp, bottom = 8.dp, start = 14.dp), color = Muted)
                Column(Modifier.fillMaxWidth().background(Pale, RoundedCornerShape(24.dp)).padding(14.dp)) {
                    Row { Text("● Codex", Modifier.weight(1f), color = Ink); Text(if (tab == "Cost") "$71.37" else "301M", color = Ink) }
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF3C3C42), trackColor = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("100.0% of cost · 301M tokens", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                Text("Totals", Modifier.padding(top = 20.dp, bottom = 8.dp, start = 14.dp), color = Muted)
                Row(Modifier.fillMaxWidth().background(Pale, RoundedCornerShape(24.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("Processed tokens", color = Muted); Text("301M", color = Ink, style = MaterialTheme.typography.titleLarge) }
                    Column { Text("Cache savings", color = Muted); Text("$390.79", color = Ink, style = MaterialTheme.typography.titleLarge) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SegmentedRow(labels: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFFECECED), RoundedCornerShape(30.dp)).padding(3.dp)) {
        labels.forEach { label ->
            Box(Modifier.weight(1f).background(if (label == selected) Color.White else Color.Transparent, RoundedCornerShape(26.dp)).clickable { onSelect(label) }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(label, color = if (label == selected) Ink else Muted, fontWeight = if (label == selected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun SettingsPage(go: (String) -> Unit) {
    Top("Settings") {
        IconButton(onClick = { go("Search settings") }) { Icon(Icons.Default.Search, "Search", tint = Ink) }
        IconButton(onClick = { go("Settings filters") }) { Icon(Icons.Outlined.FilterAlt, "Filters", tint = Ink) }
    }
    RoundedContent {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("Connections", Modifier.padding(start = 14.dp, bottom = 8.dp), color = Muted)
            SettingsGroup {
                SettingsRow(Icons.Default.AccountCircle, "Account", "brave.note0061@arys.ch") { go("Account") }
                SettingsRow(Icons.Default.Computer, "Environments", trailing = "1") { go("Environments") }
                SettingsRow(Icons.Default.NotificationsNone, "Notifications") { go("Notifications") }
            }
            Spacer(Modifier.height(12.dp))
            SettingsGroup {
                SettingsRow(Icons.Default.Settings, "General") { go("General") }
                SettingsRow(Icons.Default.Palette, "Appearance") { go("Appearance") }
                SettingsRow(Icons.Default.ChatBubbleOutline, "Chat") { go("Chat") }
            }
            Text("Agents", Modifier.padding(start = 14.dp, top = 14.dp, bottom = 8.dp), color = Muted)
            SettingsGroup {
                SettingsRow(Icons.Default.AccountCircle, "Providers") { go("Providers") }
                SettingsRow(Icons.Default.ChatBubbleOutline, "Threads") { go("Threads preferences") }
                SettingsRow(Icons.Default.Schedule, "Scheduled tasks") { go("Scheduled tasks") }
            }
            Text("Code", Modifier.padding(start = 14.dp, top = 14.dp, bottom = 8.dp), color = Muted)
            SettingsGroup {
                SettingsRow(Icons.Default.FolderOpen, "Projects") { go("Projects") }
                SettingsRow(Icons.Default.AccountTree, "Source control") { go("Source control") }
                SettingsRow(Icons.Default.ContentCopy, "Worktrees") { go("Worktrees") }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), content = content)
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, subtitle: String? = null, trailing: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = if (subtitle == null) 46.dp else 62.dp).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Ink, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
        if (trailing != null) Text(trailing, color = Muted)
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB5B5B9), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun DetailPage(title: String, back: () -> Unit, go: (String) -> Unit) {
    Top(title, back)
    RoundedContent { Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        when (title) {
            "General" -> {
                SectionLabel("Project grouping")
                SettingsGroup {
                    AppearanceOption("Group by repository", true, "Matching repositories appear as one project")
                    AppearanceOption("Group by repository path", false, "Keep monorepo paths separate")
                    AppearanceOption("Keep separate", false, "Show every workspace as its own project")
                }
                SectionLabel("Sidebar")
                SettingsGroup {
                    SwitchOption("Working section", "Show active threads together", true)
                    SwitchOption("Use last project", "Start new threads in the last project", true)
                }
            }
            "Notifications" -> {
                SectionLabel("Agent activity")
                SettingsGroup {
                    SwitchOption("Device notifications", "Updates for important activity", true)
                    SwitchOption("Agent live updates", "Progress while an agent works", true)
                    SettingsRow(Icons.Default.Tune, "Live update settings") { go("Live update settings") }
                }
            }
            "Chat" -> {
                SectionLabel("While the agent is running")
                SettingsGroup {
                    AppearanceOption("Queue", false, "Your message waits for the current turn")
                    AppearanceOption("Steer", true, "Your message reaches the agent immediately")
                }
            }
            "Account" -> {
                Spacer(Modifier.height(32.dp))
                Box(Modifier.size(96.dp).align(Alignment.CenterHorizontally).background(Pale, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Blue, modifier = Modifier.size(48.dp)) }
                Text("Arnav Chandra", Modifier.align(Alignment.CenterHorizontally).padding(16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                ListRow(Icons.Default.Person, "Manage account") { go("Manage account") }
                ListRow(Icons.Default.Lock, "Security") { go("Security") }
                ListRow(Icons.Default.Computer, "Remote connection") { go("Remote connection") }
            }
            "Appearance" -> {
                SectionLabel("Theme")
                AppearanceOption("Light", true)
                AppearanceOption("Dark", false)
                AppearanceOption("Follow system", false)
            }
            else -> {
                SectionLabel("Explore")
                val rows = when(title) {
                    "Projects" -> listOf("Web workspace", "Mobile app", "API service")
                    "Environments" -> listOf("Desktop environment", "Development server", "Add environment")
                    "Pull requests" -> listOf("Update export workflow", "Improve API design")
                    "Worktrees", "Branches" -> listOf("main", "feature/export-flow", "fix/browser-launch")
                    "Providers" -> listOf("Connected provider", "Add provider")
                    "Scheduled tasks" -> listOf("Daily workspace summary", "Add scheduled task")
                    "Files" -> listOf("README.md", "app/", "docs/", "package.json")
                    else -> listOf("Overview", "Details", "Preferences")
                }
                rows.forEach { ListRow(Icons.Default.ChevronRight, it) { go(it) } }
            }
        }
    } }
}

@Composable
private fun AppearanceOption(label: String, selected: Boolean, description: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = Ink, style = MaterialTheme.typography.bodyLarge)
            if (description != null) Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SwitchOption(label: String, description: String, checked: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Ink, style = MaterialTheme.typography.bodyLarge)
            Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun NewThreadPage(back: () -> Unit, go: (String) -> Unit, draft: String, onDraftChange: (String) -> Unit, model: String) {
    Row(Modifier.fillMaxWidth().height(66.dp).background(Page).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Back", tint = Ink) }
        Text("New thread", Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = { go("Files") }) { Icon(Icons.Default.FolderOpen, "Open files", tint = Ink) }
        IconButton(onClick = { go("Git controls") }) { Icon(Icons.Default.AccountTree, "Open git controls", tint = Ink) }
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("What should we build", color = Ink, style = MaterialTheme.typography.headlineLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("in ", color = Ink, style = MaterialTheme.typography.headlineLarge)
                TextButton(onClick = { go("Choose project") }) {
                    Text("Web workspace", color = Ink, style = MaterialTheme.typography.headlineLarge)
                }
                Text("?", color = Ink, style = MaterialTheme.typography.headlineLarge)
            }
            TextButton(onClick = { go("Choose environment") }) {
                Icon(Icons.Default.Computer, null, tint = Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("on desktop environment", color = Muted, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ChevronRight, null, tint = Muted, modifier = Modifier.size(18.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { go("Choose checkout") }) {
                Icon(Icons.Default.FolderOpen, null, tint = Muted, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(6.dp))
                Text("Current checkout", color = Muted)
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { go("Choose branch") }) {
                Icon(Icons.Default.AccountTree, null, tint = Muted, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(6.dp))
                Text("main", color = Muted)
                Icon(Icons.Default.ChevronRight, null, tint = Muted, modifier = Modifier.size(17.dp))
            }
        }
        Composer(draft, onDraftChange, model, { go("Thread settings") })
    }
}

@Composable
private fun Composer(draft: String, onDraftChange: (String) -> Unit, model: String, onModelClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)
            .background(Color(0xFFF4F4F5), RoundedCornerShape(28.dp))
            .border(1.dp, Line, RoundedCornerShape(28.dp))
            .padding(start = 8.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) { Icon(Icons.Default.Add, "Add attachment", tint = Ink) }
        BasicTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
            minLines = 1,
            maxLines = 4,
            decorationBox = { inner ->
                Box {
                    if (draft.isEmpty()) Text("Ask anything", color = Muted, style = MaterialTheme.typography.bodyLarge)
                    inner()
                }
            }
        )
        IconButton(onClick = onModelClick, modifier = Modifier.width(58.dp), colors = IconButtonDefaults.iconButtonColors(contentColor = Muted)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(2.dp))
                Icon(Icons.Default.KeyboardArrowDown, "Select model: $model", modifier = Modifier.size(18.dp))
            }
        }
        IconButton(onClick = {}, enabled = draft.isNotBlank(), modifier = Modifier.size(38.dp).background(if (draft.isNotBlank()) Blue else Color(0xFFE0E0E1), CircleShape)) {
            Icon(Icons.Default.ArrowUpward, "Send", tint = if (draft.isNotBlank()) Color.White else Muted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ModelPickerPage(back: () -> Unit, current: String, onSave: (String) -> Unit) {
    var pending by remember(current) { mutableStateOf(current) }
    var query by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().height(66.dp).background(Page).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.Default.Close, "Cancel", tint = Ink) }
        Text("Thread settings", Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.headlineSmall)
        IconButton(onClick = {}, modifier = Modifier.size(42.dp)) { Icon(Icons.Outlined.FilterAlt, "Filter models", tint = Ink) }
        if (pending != current) TextButton(onClick = { onSave(pending) }) { Text("Save", color = Blue, fontWeight = FontWeight.SemiBold) }
    }
    RoundedContent {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth().background(Color(0xFFFCFCFC), RoundedCornerShape(28.dp)).padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = Muted)
                Spacer(Modifier.width(12.dp))
                BasicTextField(value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f), textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink), singleLine = true,
                    decorationBox = { inner -> Box { if (query.isEmpty()) Text("Find a model", color = Muted); inner() } })
            }
            Spacer(Modifier.height(16.dp))
            ModelGroup("Codex (College)", listOf("GPT-6-Sol", "GPT-5.6-Terra", "GPT-6-Astra", "GPT-6-Luna"), pending, query) { pending = it }
            Spacer(Modifier.height(12.dp))
            ModelGroup("Codex (Personal)", listOf("GPT-6-Luna", "GPT-5.6-Terra"), pending, query) { pending = it }
            if (query.isBlank()) {
                Text("Options", Modifier.padding(start = 4.dp, top = 10.dp, bottom = 8.dp), color = Muted)
                SettingsGroup {
                    SettingsRow(Icons.Default.Tune, "Reasoning", trailing = "Medium") { }
                    HorizontalDivider(color = Line)
                    SettingsRow(Icons.Default.Speed, "Service Tier", trailing = "Standard") { }
                    HorizontalDivider(color = Line)
                    SettingsRow(Icons.Default.Security, "Runtime", trailing = "Full access") { }
                }
                Text("Catalog", Modifier.padding(start = 4.dp, top = 18.dp, bottom = 8.dp), color = Muted)
                SettingsGroup {
                    SwitchOption("Manage hidden models", "", false)
                    SwitchOption("Legacy models", "", false)
                }
            }
        }
    }
}

@Composable
private fun ModelGroup(provider: String, models: List<String>, pending: String, query: String, onSelect: (String) -> Unit) {
    val shown = models.filter { it.contains(query, ignoreCase = true) || provider.contains(query, ignoreCase = true) }
    if (shown.isEmpty()) return
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.SmartToy, null, tint = Ink, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(provider, Modifier.weight(1f), color = Muted)
        Icon(Icons.Default.KeyboardArrowUp, null, tint = Muted, modifier = Modifier.size(18.dp))
    }
    SettingsGroup {
        shown.forEachIndexed { index, model ->
            val key = "$provider · $model"
            Row(Modifier.fillMaxWidth().clickable { onSelect(key) }.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = pending == key, onClick = { onSelect(key) })
                Spacer(Modifier.width(5.dp))
                Text(model, Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.bodyLarge)
                if (model == "GPT-6-Astra" || provider == "Codex (Personal)" && model == "GPT-5.6-Terra") Text("Default", color = Muted, style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = {}) { Icon(Icons.Default.StarBorder, "Favorite", tint = Muted) }
            }
            if (index != shown.lastIndex) HorizontalDivider(color = Line)
        }
    }
}
