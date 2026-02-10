package com.katchy.focuslive.ui.notes

import kotlinx.coroutines.delay

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import com.katchy.focuslive.data.model.NoteCategory
import androidx.compose.material3.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.katchy.focuslive.data.model.Note
import com.katchy.focuslive.ui.components.BrishMascotWithBubble
import com.katchy.focuslive.ui.components.BrishPose
import com.katchy.focuslive.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val colorFilter by viewModel.colorFilter.collectAsState()
    val selectedMascot by viewModel.selectedMascot.collectAsState()

    var showAddNoteSheet by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Colors for filtering (Keep for manual color selection if user wants)
    val filterColors = listOf("#FFF9C4", "#D1C4E9", "#B3E5FC", "#FFCCBC", "#C8E6C9", "#F0F4C3")

    // Grouping Logic
    val groupedNotes = remember(notes, categories) {
        val categoryMap = categories.associateWith { cat ->
            notes.filter { it.categoryId == cat.id }
        }
        
        val categoryIds = categories.map { it.id }.toSet()
        val uncategorized = notes.filter { it.categoryId == null || it.categoryId !in categoryIds }
        
        val result = categoryMap.toMutableMap()
        if (uncategorized.isNotEmpty()) {
             // Create a dummy category for UI
             result[com.katchy.focuslive.data.model.NoteCategory(id = "uncategorized", name = "General", isDefault = true)] = uncategorized
        }
        
        // Sort: Default/Sorted categories first, then Uncategorized (or vice versa depending on preference)
        // Here we just use the order from the mapped list (which comes from Flow sorted by sortOrder)
        // We append uncategorized at the end if it exists.
        result
    }
    // We need a stable list order for the UI
    val sortedGroups = remember(groupedNotes) {
        val list = groupedNotes.keys.sortedBy { it.sortOrder }.toMutableList()
        // Ensure "General" / Uncategorized is at a specific place if needed.
        // For now, let's just trust the keys order, usually LinkedHashMap preserves insertion if we built it that way.
        // But `categories` flow is sorted by DB.
        
        // Explicitly move "uncategorized" to top or bottom? User said "Sections".
        // Let's keep mapped order.
        list
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentPadding = PaddingValues(
                start = 24.dp, 
                end = 24.dp, 
                top = 24.dp, 
                bottom = 140.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            // --- HEADER ---
            item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NOTAS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Mis Secciones",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Add Category Button
                            IconButton(
                                onClick = { showAddCategoryDialog = true },
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.CreateNewFolder, "Nueva Sección", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            
                            // Add Note Button
                            IconButton(
                                onClick = { showAddNoteSheet = true },
                                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Add, "Nueva Nota", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // --- SEARCH ---
            item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                 OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Buscar en todas las secciones...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // --- EMPTY STATE ---
            if (notes.isEmpty() && categories.isEmpty()) {
                item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BrishMascotWithBubble(pose = BrishPose.NOTES, mascotType = selectedMascot)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "¡Organízate con Secciones!",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                             Text(
                                text = "Crea tu primera sección (ej. 'Ideas', 'Compras').",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { showAddCategoryDialog = true },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Rounded.CreateNewFolder, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear Sección")
                            }
                        }
                    }
                }
            }

            // --- SECTIONS & NOTES ---
            // If search is active, maybe flatten? Or keep sections? KEEP SECTIONS for context.
            
            sortedGroups.forEach { category ->
                val categoryNotes = groupedNotes[category] ?: emptyList()
                
                // Show header if there are notes OR if it's a user-defined category (so they can see empty sections to add to)
                // Filter out empty user categories IF search is active to avoid clutter
                if (categoryNotes.isNotEmpty() || (searchQuery.isEmpty() && category.id != "uncategorized")) {
                    
                    // SECTION HEADER
                    item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .animateItemPlacement()
                        ) {
                            Text(
                                text = category.name.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary, // Using primary color for section name as requested ("Notes de studio")
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            
                            // Delete category option (only for non-default/uncategorized)
                            if (category.id != "uncategorized") {
                                IconButton(onClick = { viewModel.deleteCategory(category) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Close, "Eliminar Sección", tint = MaterialTheme.colorScheme.error.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // NOTES IN THIS SECTION
                    if (categoryNotes.isEmpty()) {
                        item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                            Text(
                                "Vacío por ahora...", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                            )
                        }
                    } else {
                        items(
                            categoryNotes,
                            key = { note -> note.id },
                            contentType = { "note" }
                        ) { note ->
                             val isVisible = rememberSaveable(note.id) { mutableStateOf(false) }
                             LaunchedEffect(note.id) { isVisible.value = true }
                             
                             AnimatedVisibility(
                                visible = isVisible.value,
                                enter = fadeIn() + scaleIn(),
                                modifier = Modifier.animateItemPlacement()
                             ) {
                                NoteCard(
                                    note = note, 
                                    onDelete = { viewModel.deleteNote(note.id) },
                                    onEdit = { noteToEdit = note },
                                    onConvertToTask = { viewModel.convertToTask(note) }
                                )
                             }
                        }
                    }
                }
            }
            
            item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // --- DIALOGS ---

    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Nueva Sección") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nombre (ej. Compras)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCategory(newCategoryName)
                        showAddCategoryDialog = false
                    },
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddNoteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddNoteSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NoteEditorContent(
                categories = categories,
                onSave = { title, content, color, isPinned, priority, categoryId ->
                    viewModel.addNote(title, content, color, isPinned, priority, categoryId)
                    showAddNoteSheet = false
                }
            )
        }
    }

    if (noteToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = { noteToEdit = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NoteEditorContent(
                initialTitle = noteToEdit!!.title,
                initialContent = noteToEdit!!.content,
                initialColor = noteToEdit!!.colorHex,
                initialPinned = noteToEdit!!.isPinned,
                initialPriority = noteToEdit!!.priority,
                initialCategoryId = noteToEdit!!.categoryId,
                isEditing = true,
                categories = categories,
                onSave = { title, content, color, isPinned, priority, categoryId ->
                    viewModel.updateNote(noteToEdit!!.copy(
                        title = title, 
                        content = content, 
                        colorHex = color, 
                        isPinned = isPinned, 
                        priority = priority,
                        categoryId = categoryId
                    ))
                    noteToEdit = null
                }
            )
        }
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NoteCard(note: Note, onDelete: () -> Unit, onEdit: () -> Unit, onConvertToTask: () -> Unit) {
    val bgColor = remember(note.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.colorHex))
        } catch (e: Exception) {
            Color(0xFFFFF9C4)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "noteScale",
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {},
                onLongClick = onEdit
            ),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        shadowElevation = if (isPressed) 8.dp else 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (note.title.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                         Icon(Icons.Rounded.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                         Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (note.priority != "LOW") {
                         val priorityColor = if(note.priority == "HIGH") Color(0xFFEF4444) else Color(0xFFF59E0B)
                         Icon(Icons.Rounded.PriorityHigh, null, tint = priorityColor, modifier = Modifier.size(16.dp))
                         Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f),
                maxLines = 10,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onConvertToTask, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.PlaylistAddCheck, null, tint = Color.Black.copy(alpha = 0.4f))
                    // Tooltip?
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = Color.Black.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun PulsingLightbulb() {
    val infiniteTransition = rememberInfiniteTransition(label = "bulb")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bulbAlpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bulbScale"
    )

    Box(contentAlignment = Alignment.Center) {
        // Glow effect
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .alpha(alpha * 0.5f)
                .background(Color(0xFFFFF176).copy(alpha = 0.4f), CircleShape)
        )
        Icon(
            Icons.Rounded.Lightbulb, 
            null, 
            tint = if(alpha > 0.6f) Color(0xFFFDD835) else Color.Gray.copy(alpha = 0.5f), 
            modifier = Modifier.size(64.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorContent(
    initialTitle: String = "",
    initialContent: String = "",
    initialColor: String = "#FFF9C4",
    initialPinned: Boolean = false,
    initialPriority: String = "LOW",
    initialCategoryId: String? = null,
    isEditing: Boolean = false,
    categories: List<com.katchy.focuslive.data.model.NoteCategory> = emptyList(),
    onSave: (String, String, String, Boolean, String, String?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    // Use TextFieldValue to control cursor position
    var contentField by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(initialContent, androidx.compose.ui.text.TextRange(initialContent.length))) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var isPinned by remember { mutableStateOf(initialPinned) }
    var priority by remember { mutableStateOf(initialPriority) }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    
    // Focus Requester to handle formatting inserts
    val contentFocusRequester = remember { FocusRequester() }
    
    val colors = listOf("#FFF9C4", "#D1C4E9", "#B3E5FC", "#FFCCBC", "#C8E6C9", "#F0F4C3")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(24.dp)
            .imePadding()
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if(isEditing) "EDITAR NOTA" else "NUEVA NOTA", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Bold
            )
            Row {
                 IconButton(onClick = { 
                     priority = when(priority) {
                         "LOW" -> "MEDIUM"
                         "MEDIUM" -> "HIGH"
                         else -> "LOW"
                     }
                 }) {
                    val (icon, color) = when(priority) {
                        "HIGH" -> Icons.Rounded.PriorityHigh to Color(0xFFEF4444)
                        "MEDIUM" -> Icons.Rounded.LowPriority to Color(0xFFF59E0B)
                        else -> Icons.Rounded.LowPriority to Color.Gray.copy(alpha=0.3f)
                    }
                    Icon(icon, contentDescription = "Prioridad", tint = color)
                 }

                IconButton(onClick = { isPinned = !isPinned }) {
                    Icon(
                        Icons.Rounded.PushPin, 
                        contentDescription = "Fijar",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // --- CATEGORY SELECTOR (New) ---
        if (categories.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("General") },
                        leadingIcon = { if(selectedCategoryId == null) Icon(Icons.Rounded.Check, null) }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name) },
                        leadingIcon = { if(selectedCategoryId == cat.id) Icon(Icons.Rounded.Check, null) }
                    )
                }
            }
        }
        
        // --- TITLE ---
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Título (Opcional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // --- CONTENT ---
        TextField(
            value = contentField,
            onValueChange = { contentField = it },
            placeholder = { Text("Escribe tus ideas aquí...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Grow to fill available space
                .focusRequester(contentFocusRequester),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // --- FORMATTING TOOLBAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checklist Button
            FilledTonalButton(
                onClick = {
                    val currentText = contentField.text
                    val cursor = contentField.selection.start
                    
                    // Logic: Insert "\n-" if not at start, or just "- "
                    // If cursor is at start of line, just insert "- "
                    // If cursor is in middle/end, insert "\n- "
                    
                    // Simple logic: Insert "\n- " if cursor > 0 and char before isn't newline
                    val prefix = if (cursor > 0 && currentText[cursor - 1] != '\n') "\n- " else "- "
                    
                    val newText = currentText.substring(0, cursor) + prefix + currentText.substring(cursor)
                    val newCursorPosition = cursor + prefix.length
                    
                    contentField = androidx.compose.ui.text.input.TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newCursorPosition)
                    )
                    contentFocusRequester.requestFocus() 
                },
                contentPadding = PaddingValues(horizontal = 12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.FormatListBulleted, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lista", style = MaterialTheme.typography.labelMedium)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Color Picker (Compact)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 items(colors) { colorHex ->
                    val color = Color(android.graphics.Color.parseColor(colorHex))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == colorHex) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorHex }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // --- SAVE BUTTON ---
        Button(
            onClick = { onSave(title, contentField.text, selectedColor, isPinned, priority, selectedCategoryId) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
        ) {
            Text(if(isEditing) "ACTUALIZAR" else "GUARDAR", fontWeight = FontWeight.Bold)
        }
    }
}
