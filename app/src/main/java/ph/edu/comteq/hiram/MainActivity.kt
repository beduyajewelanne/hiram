package ph.edu.comteq.hiram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val recordList = populateRecordList()
            val navController = rememberNavController()
            var themeMode by rememberSaveable { mutableStateOf("Light") }
            val darkTheme = themeMode == "Dark"

            MaterialTheme(
                colorScheme = if (darkTheme) darkColorScheme()
                else lightColorScheme()) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(recordList, navController, darkTheme)
                    }
                    composable("add") {
                        AddEditScreen(null, navController, darkTheme)
                    }
                    composable(
                        route = "edit/{recordId}",
                        arguments = listOf(navArgument("recordId") {
                            type = NavType.IntType
                        })
                    ) { navBackStackEntry ->
                        val recordId = navBackStackEntry.arguments?.getInt("recordId")
                        val record = recordList.find { it.id == recordId }
                        AddEditScreen(record, navController, darkTheme)
                    }
                    composable(
                        route = "details/{recordId}",
                        arguments = listOf(navArgument("recordId") {
                            type = NavType.IntType
                        })
                    ) { navBackStackEntry ->
                        val recordId = navBackStackEntry.arguments?.getInt("recordId")
                        val record = recordList.find { it.id == recordId }
                        if (record != null) {
                            RecordDetailsScreen(record, navController, darkTheme)
                        }
                    }
                    composable("settings") {
                        SettingsScreen(
                            recordList,
                            navController,
                            themeMode,
                            darkTheme
                        ) { themeMode = it }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(records: List<Record>, navController: NavController, darkTheme: Boolean) {
    val bg =
        if (darkTheme) Color(0xFF222222)
        else Color(0xFFF6F5F1)
    val ink =
        if (darkTheme) Color(0xFFF6F5F1)
        else Color(0xFF222222)
    val muted =
        if (darkTheme) Color(0xFFA9A8A3)
        else Color(0xFF6B6A66)
    val tint =
        if (darkTheme) Color(0xFF303030)
        else Color(0xFFE9E7E0)
    val red =
        if (darkTheme) Color(0xFFFF8A80)
        else Color(0xFFB3261E)

    var filter by remember { mutableStateOf("All") }
    val active = records.filter { it.status != "Returned" }
    val owed = active.filter { it.kind == "Money" && it.type == "Lent" }.sumOf { it.amount }
    val owe = active.filter { it.kind == "Money" && it.type == "Borrowed" }.sumOf { it.amount }

    val shown = when (filter) {
        "Lent" -> active.filter { it.type == "Lent" }
        "Borrowed" -> active.filter { it.type == "Borrowed" }
        "Returned" -> records.filter { it.status == "Returned" }
        else -> records.sortedBy { it.status == "Returned" }
    }
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))

    Scaffold(
        containerColor = bg,
        contentColor = ink,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 2.dp, color = ink)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(ink)
                            .clickable {
                                navController.navigate("home") {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Home",
                            color = bg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Settings",
                            color = ink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "hiram",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                today,
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 2.dp, color = ink)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tint)
                    .border(2.dp, ink)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Owed to you",
                        style = MaterialTheme.typography.bodySmall,
                        color = muted
                    )
                    Text(
                        amount(owed),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "You owe",
                        style = MaterialTheme.typography.bodySmall,
                        color = muted
                    )
                    Text(
                        amount(owe),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(
                modifier = Modifier.height(12.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Lent", "Borrowed", "Returned").forEach { option ->
                    Box(
                        modifier = Modifier
                            .background(if (filter == option) ink else bg)
                            .border(2.dp, ink)
                            .clickable { filter = option }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (filter == option) bg else ink
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Text(
                "${active.size} active · ${records.size - active.size} returned",
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )

            if (shown.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "A fresh little ledger",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Lent a book? Borrowed some cash? Add your first record so it's easy to remember later.",
                        textAlign = TextAlign.Center,
                        color = muted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(shown) { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("details/${record.id}") }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    recordName(record),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (record.type == "Lent") "You lent to ${record.person}"
                                    else "You borrowed from ${record.person}"
                                )
                                Text(
                                    record.dueInfo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (record.status == "Overdue") red else muted
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    record.kind.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = muted
                                )
                                Text(
                                    date(record.dueDate),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Button(
                onClick = { navController.navigate("add") },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ink, contentColor = bg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (records.isEmpty()) "Add your first record"
                    else "Add record"
                )
            }
            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(record: Record?, navController: NavController, darkTheme: Boolean) {
    val bg =
        if (darkTheme) Color(0xFF222222)
        else Color(0xFFF6F5F1)
    val ink =
        if (darkTheme) Color(0xFFF6F5F1)
        else Color(0xFF222222)
    val muted =
        if (darkTheme) Color(0xFFA9A8A3)
        else Color(0xFF6B6A66)
    val red =
        if (darkTheme) Color(0xFFFF8A80)
        else Color(0xFFB3261E)

    val isEdit = record != null

    var kind by remember { mutableStateOf(record?.kind ?: "Item") }
    var type by remember { mutableStateOf(record?.type ?: "Lent") }
    var title by remember { mutableStateOf(record?.title ?: "") }
    var amount by remember {
        mutableStateOf(
            if (record != null && record.kind == "Money") record.amount.toString()
            else ""
        )
    }
    var person by remember { mutableStateOf(record?.person ?: "") }
    var dueDate by remember { mutableStateOf(record?.dueDate ?: "") }
    var note by remember { mutableStateOf(record?.note ?: "") }
    var showErrors by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }

    val titleError = kind == "Item" && title.isBlank()
    val amountError = kind == "Money" && (amount.toDoubleOrNull() ?: 0.0) <= 0.0
    val personError = person.isBlank()
    val dateError = dueDate.isBlank()
    val hasError = titleError || amountError || personError || dateError

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = ink,
        unfocusedTextColor = ink,
        errorTextColor = ink,
        focusedBorderColor = ink,
        unfocusedBorderColor = ink,
        errorBorderColor = red,
        cursorColor = ink,
        errorCursorColor = red
    )

    Scaffold(
        containerColor = bg,
        contentColor = ink,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            if (isEdit) "Edit record"
                            else "Add record",
                            fontWeight = FontWeight.Black
                        )
                    },
                    actions = {
                        TextButton(
                            onClick = { navController.navigateUp() },
                            shape = RectangleShape,
                            colors = ButtonDefaults.textButtonColors(contentColor = ink)
                        ) {
                            Text("Cancel")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bg,
                        titleContentColor = ink,
                        actionIconContentColor = ink
                    )
                )
                HorizontalDivider(thickness = 2.dp, color = ink)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                if (showErrors && hasError) {
                    Text(
                        "Check the details below, then save again.",
                        color = bg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(red)
                            .padding(12.dp)
                    )
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }

                Text(
                    "What are you tracking?",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Item", "Money").forEach { option ->
                        Box(
                            modifier = Modifier
                                .background(if (kind == option) ink else bg)
                                .border(2.dp, ink)
                                .clickable { kind = option }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                option,
                                color = if (kind == option) bg else ink
                            )
                        }
                    }
                }
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    "Which way did it go?",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Lent", "Borrowed").forEach { option ->
                        Box(
                            modifier = Modifier
                                .background(if (type == option) ink else bg)
                                .border(2.dp, ink)
                                .clickable { type = option }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                option,
                                color = if (type == option) bg else ink
                            )
                        }
                    }
                }
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (kind == "Item") {
                    Text(
                        "Item name",
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "e.g. Scientific calculator",
                                color = muted
                            )
                        },
                        isError = showErrors && titleError,
                        supportingText = {
                            if (showErrors && titleError) {
                                Text(
                                    "Enter the item name.",
                                    color = red
                                )
                            }
                        },
                        singleLine = true,
                        shape = RectangleShape,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        "Amount (PHP)",
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        prefix = {
                            Text(
                                "₱",
                                color = ink
                            )
                        },
                        placeholder = {
                            Text(
                                "0.00",
                                color = muted
                            )
                        },
                        isError = showErrors && amountError,
                        supportingText = {
                            if (showErrors && amountError) {
                                Text(
                                    "Enter an amount greater than ₱0.",
                                    color = red
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RectangleShape,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    if (type == "Lent") "Who did you lend it to?"
                    else "Who did you borrow from?",
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    placeholder = {
                        Text(
                            "Person's name",
                            color = muted
                        )
                    },
                    isError = showErrors && personError,
                    supportingText = {
                        if (showErrors && personError) {
                            Text(
                                "Add the name of the person involved.",
                                color = red
                            )
                        }
                    },
                    singleLine = true,
                    shape = RectangleShape,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Due date",
                    fontWeight = FontWeight.SemiBold
                )
                Box {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Text(
                                "Tap to choose a due date",
                                color = muted
                            )
                        },
                        isError = showErrors && dateError,
                        supportingText = {
                            if (showErrors && dateError) {
                                Text(
                                    "Choose a date to keep track of what's due.",
                                    color = red
                                )
                            }
                        },
                        shape = RectangleShape,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showPicker = true }
                    )
                }

                Text(
                    "Note (optional)",
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text(
                            "Anything worth remembering?",
                            color = muted
                        )
                    },
                    minLines = 3,
                    shape = RectangleShape,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Button(
                onClick = {
                    showErrors = true
                    if (!hasError) {
                        navController.navigateUp()
                    }
                },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ink, contentColor = bg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isEdit) "Save changes"
                    else "Save record"
                )
            }
            Text(
                "Just on this device. No account needed.",
                style = MaterialTheme.typography.bodySmall,
                color = muted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState()
        val pickerColors = DatePickerDefaults.colors(
            containerColor = bg,
            titleContentColor = ink,
            headlineContentColor = ink,
            weekdayContentColor = muted,
            subheadContentColor = ink,
            navigationContentColor = ink,
            yearContentColor = ink,
            currentYearContentColor = ink,
            selectedYearContentColor = bg,
            selectedYearContainerColor = ink,
            dayContentColor = ink,
            selectedDayContentColor = bg,
            selectedDayContainerColor = ink,
            todayContentColor = ink,
            todayDateBorderColor = ink
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            shape = RectangleShape,
            colors = pickerColors,
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            dueDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                        }
                        showPicker = false
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.textButtonColors(contentColor = ink)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPicker = false },
                    shape = RectangleShape,
                    colors = ButtonDefaults.textButtonColors(contentColor = ink)
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = pickerState,
                colors = pickerColors
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailsScreen(record: Record, navController: NavController, darkTheme: Boolean) {
    val bg =
        if (darkTheme) Color(0xFF222222)
        else Color(0xFFF6F5F1)
    val ink =
        if (darkTheme) Color(0xFFF6F5F1)
        else Color(0xFF222222)
    val muted =
        if (darkTheme) Color(0xFFA9A8A3)
        else Color(0xFF6B6A66)
    val tint =
        if (darkTheme) Color(0xFF303030)
        else Color(0xFFE9E7E0)
    val red =
        if (darkTheme) Color(0xFFFF8A80)
        else Color(0xFFB3261E)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val returned = record.status == "Returned"
    val overdue = record.status == "Overdue"
    val thing =
        if (record.kind == "Money") "money"
        else "item"
    val subtitle = when {
        returned && record.type == "Lent" -> "${record.person} returned your $thing."
        returned -> "You returned ${record.person}'s $thing."
        record.type == "Lent" -> "${record.person} has your $thing."
        else -> "You have ${record.person}'s $thing."
    }

    val rows = mutableListOf(
        "Person" to record.person,
        "Type" to (
                if (record.type == "Lent") "Lent to someone"
                else "Borrowed from someone"
                ),
        "Due date" to record.dueDate,
        "Status" to (
                if (returned) "Returned"
                else if (overdue) "Overdue"
                else "Not returned"
                )
    )
    if (returned) {
        rows.add("Returned on" to record.returnedOn)
    }

    Scaffold(
        containerColor = bg,
        contentColor = ink,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    data,
                    shape = RectangleShape,
                    containerColor = ink,
                    contentColor = bg
                )
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Record details",
                            fontWeight = FontWeight.Black
                        )
                    },
                    navigationIcon = {
                        TextButton(
                            onClick = { navController.navigateUp() },
                            shape = RectangleShape,
                            colors = ButtonDefaults.textButtonColors(contentColor = ink)
                        ) {
                            Text("Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bg,
                        titleContentColor = ink,
                        navigationIconContentColor = ink
                    )
                )
                HorizontalDivider(thickness = 2.dp, color = ink)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                Text(
                    "${record.kind.uppercase()} · YOU ${record.type.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    recordName(record),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    subtitle,
                    color = muted
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (overdue) red else tint)
                        .border(2.dp, if (overdue) red else ink)
                        .padding(16.dp)
                ) {
                    Text(
                        if (returned) "All returned. All good."
                        else record.dueInfo,
                        fontWeight = FontWeight.Bold,
                        color = if (overdue) bg else ink
                    )
                    Text(
                        if (returned) "Returned on ${record.returnedOn}"
                        else "Due on ${record.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overdue) bg else ink
                    )
                }
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    "THE DETAILS",
                    style = MaterialTheme.typography.labelMedium,
                    color = muted
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, ink)
                ) {
                    rows.forEachIndexed { index, row ->
                        if (index > 0) {
                            HorizontalDivider(color = ink)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                row.first,
                                color = muted
                            )
                            Text(
                                row.second,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (record.note.isNotBlank()) {
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                    Text(
                        "NOTE",
                        style = MaterialTheme.typography.labelMedium,
                        color = muted
                    )
                    Text(record.note)
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
            if (returned) {
                Button(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ink, contentColor = bg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("${recordName(record)} marked as returned.")
                        }
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ink, contentColor = bg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as returned")
                }
            }
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("edit/${record.id}") },
                    shape = RectangleShape,
                    border = BorderStroke(2.dp, ink),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit record")
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    shape = RectangleShape,
                    border = BorderStroke(2.dp, red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RectangleShape,
            containerColor = bg,
            titleContentColor = ink,
            textContentColor = ink,
            modifier = Modifier.border(2.dp, ink),
            title = {
                Text(
                    "Delete this record?",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text("${recordName(record)} with ${record.person} will be permanently removed. This can't be undone.")
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RectangleShape,
                    border = BorderStroke(2.dp, ink),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ink)
                ) {
                    Text("Keep record")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        navController.navigateUp()
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = red, contentColor = bg)
                ) {
                    Text("Delete record")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    records: List<Record>,
    navController: NavController,
    themeMode: String,
    darkTheme: Boolean,
    onThemeChange: (String) -> Unit
) {
    val bg =
        if (darkTheme) Color(0xFF222222)
        else Color(0xFFF6F5F1)
    val ink =
        if (darkTheme) Color(0xFFF6F5F1)
        else Color(0xFF222222)
    val muted =
        if (darkTheme) Color(0xFFA9A8A3)
        else Color(0xFF6B6A66)

    val active = records.filter { it.status != "Returned" }
    val owed = active.filter { it.kind == "Money" && it.type == "Lent" }.sumOf { it.amount }
    val owe = active.filter { it.kind == "Money" && it.type == "Borrowed" }.sumOf { it.amount }
    val itemsOut = active.filter { it.kind == "Item" }

    Scaffold(
        containerColor = bg,
        contentColor = ink,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            fontWeight = FontWeight.Black
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bg,
                        titleContentColor = ink
                    )
                )
                HorizontalDivider(thickness = 2.dp, color = ink)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 2.dp, color = ink)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                navController.navigate("home") {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Home",
                            color = ink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(ink)
                            .clickable {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Settings",
                            color = bg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(
                modifier = Modifier.height(12.dp)
            )
            Text(
                "YOUR LEDGER AT A GLANCE",
                style = MaterialTheme.typography.labelMedium,
                color = muted
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, ink)
                    .padding(vertical = 16.dp)
            ) {
                listOf(
                    records.size to "Total",
                    active.size to "Active",
                    (records.size - active.size) to "Returned",
                    records.count { it.status == "Overdue" } to "Overdue"
                ).forEach { stat ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stat.first.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            stat.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = muted
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ink)
                    .padding(16.dp)
            ) {
                Text(
                    "MONEY · ACTIVE RECORDS ONLY",
                    style = MaterialTheme.typography.labelMedium,
                    color = bg
                )
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
                Row {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            amount(owed),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = bg
                        )
                        Text(
                            "Owed to you",
                            style = MaterialTheme.typography.bodySmall,
                            color = bg
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            amount(owe),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = bg
                        )
                        Text(
                            "You owe",
                            style = MaterialTheme.typography.bodySmall,
                            color = bg
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, ink)
                    .padding(16.dp)
            ) {
                Text(
                    "${itemsOut.size} items still out",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${itemsOut.count { it.type == "Lent" }} lent to others · ${itemsOut.count { it.type == "Borrowed" }} borrowed by you",
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }
            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                "APPEARANCE",
                style = MaterialTheme.typography.labelMedium,
                color = muted
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Light", "Dark").forEach { option ->
                    Box(
                        modifier = Modifier
                            .background(if (themeMode == option) ink else bg)
                            .border(2.dp, ink)
                            .clickable { onThemeChange(option) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            option,
                            color = if (themeMode == option) bg else ink
                        )
                    }
                }
            }
            Text(
                "Uses ${themeMode.lowercase()} mode.",
                style = MaterialTheme.typography.bodySmall,
                color = muted,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                "Hiram, a little less to remember",
                style = MaterialTheme.typography.bodySmall,
                color = muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }
}