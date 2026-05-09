package com.example.a216453_wan_project1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

// --- PROJECT 1: DATA CLASS ---
data class HygieneState(
    val score: Int = 0,
    val waterGlasses: Int = 0,
    val history: List<String> = emptyList(),
    val isReminderEnabled: Boolean = true
)

// --- PROJECT 1: VIEWMODEL (Shared Data Layer) ---
class HygieneViewModel : ViewModel() {
    var state by mutableStateOf(HygieneState())
        private set

    fun addTask(name: String, points: Int) {
        state = state.copy(
            score = state.score + points,
            history = state.history + name
        )
    }

    fun addWater() {
        val newCount = state.waterGlasses + 1
        state = state.copy(
            waterGlasses = newCount,
            score = state.score + 5,
            history = state.history + "Drank Water (Glass $newCount)"
        )
    }

    fun toggleReminder(enabled: Boolean) {
        state = state.copy(isReminderEnabled = enabled)
    }

    // NEW FUNCTION: Reset all data from the Settings page
    fun resetData() {
        state = HygieneState()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Creates the ViewModel safely
            val viewModel: HygieneViewModel = viewModel()
            var isDarkMode by remember { mutableStateOf(false) }

            SmartHygieneTheme(useDarkTheme = isDarkMode) {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { AppBottomNavigation(navController) }
                ) { innerPadding ->
                    // PROJECT 1: 5 DISTINCT ROUTES
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") { DashboardScreen(viewModel) }
                        composable("add_task") { AddTaskScreen(viewModel) }
                        composable("statistics") { StatisticsScreen(viewModel) }
                        composable("history") { HistoryScreen(viewModel) }
                        // Passed ViewModel to MenuScreen so the Reset Button works!
                        composable("menu") { MenuScreen(viewModel, isDarkMode, onThemeChange = { isDarkMode = it }) }
                    }
                }
            }
        }
    }
}

// --- SCREEN 1: DASHBOARD (Creative Redesign) ---
@Composable
fun DashboardScreen(viewModel: HygieneViewModel) {
    val uiState = viewModel.state
    var hygieneInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Ready to stay clean!") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- HEADER SECTION ---
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Hello, 216453! 👋", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(text = statusMessage, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
            }
            // Small Score Badge at the top right
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Red,
                contentColor = Color.White
            ) {
                Text(text = "🌟 ${uiState.score} Pts", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- SCROLLABLE MAIN CONTENT ---
        Column(
            modifier = Modifier
                .weight(1f) // This pushes the mascot image to the bottom!
                .verticalScroll(rememberScrollState())
        ) {
            // HERO CARD: Progress
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.Red),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Daily Hygiene Goal", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    val progress = (uiState.score % 100) / 100f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(progress * 100).toInt()}% to next level", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // SPLIT GRID: Water & Reminders (Side-by-Side Cards)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // LEFT CARD: Water Tracker
                ElevatedCard(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚰", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Water Tracker", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${uiState.waterGlasses} / 8", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.addWater()
                                statusMessage = "Water logged! (+5 pts)"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("+1", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // RIGHT CARD: Smart Reminders
                ElevatedCard(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏰", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reminders", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Switch(
                            checked = uiState.isReminderEnabled,
                            onCheckedChange = { viewModel.toggleReminder(it) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // QUICK ADD TASKS
            Text("Quick Tasks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickTasks = listOf("🧼 Wash Hands", "🚿 Shower", "🦷 Brush", "🧴 Sanitize")
                quickTasks.forEach { task ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable {
                            viewModel.addTask(task, 10)
                            statusMessage = "Added: $task"
                        }
                    ) {
                        Text(task, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // CUSTOM TASK FORM
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hygieneInput,
                        onValueChange = { hygieneInput = it },
                        placeholder = { Text("Custom task...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (hygieneInput.isNotEmpty()) {
                                viewModel.addTask(hygieneInput, 15)
                                statusMessage = "Task Saved: $hygieneInput"
                                hygieneInput = ""
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } // End of scrollable area

        // --- FOOTER IMAGE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.hygiene_icon),
                contentDescription = "Mascot",
                modifier = Modifier.height(300.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// --- SCREEN 2: ADD TASK ---
@Composable
fun AddTaskScreen(viewModel: HygieneViewModel) {
    var advancedTask by remember { mutableStateOf("") }
    var pointsToAward by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TopCategoryRow(viewModel.state.score)
        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Red)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Log Advanced SDG Activity", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                Text("Help achieve Clean Water & Sanitation", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = advancedTask,
                    onValueChange = { advancedTask = it },
                    label = { Text("Activity Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pointsToAward,
                    onValueChange = { pointsToAward = it },
                    label = { Text("Points (e.g. 50)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val points = pointsToAward.toIntOrNull() ?: 10
                        if (advancedTask.isNotEmpty()) {
                            viewModel.addTask(advancedTask, points)
                            message = "Activity Saved Successfully!"
                            advancedTask = ""
                            pointsToAward = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) { Text("Save Activity", color = Color.Red, fontWeight = FontWeight.Bold) }
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// --- SCREEN 3: STATISTICS ---
@Composable
fun StatisticsScreen(viewModel: HygieneViewModel) {
    val uiState = viewModel.state
    val waterGoalPercentage = if (uiState.waterGlasses >= 8) 100 else (uiState.waterGlasses / 8.0 * 100).toInt()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TopCategoryRow(uiState.score)
        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Red)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Points Earned", fontWeight = FontWeight.Bold, color = Color.White)
                Text("${uiState.score} 🌟", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Red)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Tasks Completed", fontWeight = FontWeight.Bold, color = Color.White)
                Text("${uiState.history.size} ✅", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Red)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Water Goal Completion", fontWeight = FontWeight.Bold, color = Color.White)
                Text("$waterGoalPercentage% 🚰", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SCREEN 4: HISTORY ---
@Composable
fun HistoryScreen(viewModel: HygieneViewModel) {
    val uiState = viewModel.state
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("History Log", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.history.isEmpty()) {
            Text("No completed tasks yet.")
        } else {
            uiState.history.reversed().forEach { task ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("✓ $task", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

// --- SCREEN 5: MENU/SETTINGS (Upgraded!) ---
@Composable
fun MenuScreen(viewModel: HygieneViewModel, isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val uiState = viewModel.state

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("App Menu & Settings", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        // 1. PROFILE SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("👤", fontSize = 40.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Student 216453", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Smart Hygiene Member", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 2. PREFERENCES SECTION
        Text("Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column {
                // Dark Mode Toggle
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🌙 Dark Mode", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(checked = isDarkMode, onCheckedChange = onThemeChange)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                // Push Notifications (Linked to ViewModel)
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔔 Push Notifications", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(checked = uiState.isReminderEnabled, onCheckedChange = { viewModel.toggleReminder(it) })
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 3. INFORMATION SECTION
        Text("Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        // Expandable SDG Card
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.animateContentSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌍 About SDG 6", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(if (expanded) "▲" else "▼", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                if (expanded) {
                    Text("Clean Water and Sanitation: Ensure availability and sustainable management of water and sanitation for all.",
                        fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 4. DANGER ZONE SECTION
        Text("Danger Zone", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.resetData() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Reset All Data", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // App Version
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Version 1.0.0", fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Helper block for the other pages
@Composable
fun TopCategoryRow(score: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("🧼 Smart Hygiene", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text("🌟 Score: $score", fontWeight = FontWeight.Bold)
    }
}

// --- NAVIGATION BAR ---
@Composable
fun AppBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Text("🏠") }, label = { Text("Home", fontSize = 10.sp) },
            selected = currentRoute == "dashboard",
            onClick = { navController.navigate("dashboard") }
        )
        NavigationBarItem(
            icon = { Text("📝") }, label = { Text("Add", fontSize = 10.sp) },
            selected = currentRoute == "add_task",
            onClick = { navController.navigate("add_task") }
        )
        NavigationBarItem(
            icon = { Text("📈") }, label = { Text("Stats", fontSize = 10.sp) },
            selected = currentRoute == "statistics",
            onClick = { navController.navigate("statistics") }
        )
        NavigationBarItem(
            icon = { Text("📊") }, label = { Text("History", fontSize = 10.sp) },
            selected = currentRoute == "history",
            onClick = { navController.navigate("history") }
        )
        NavigationBarItem(
            icon = { Text("⚙️") }, label = { Text("Menu", fontSize = 10.sp) },
            selected = currentRoute == "menu",
            onClick = { navController.navigate("menu") }
        )
    }
}