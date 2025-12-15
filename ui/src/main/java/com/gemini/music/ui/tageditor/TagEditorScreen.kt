package com.gemini.music.ui.tageditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Comment
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DiscFull
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: TagEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for errors
    LaunchedEffect(uiState.saveError) {
        uiState.saveError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TagEditorEvent.DismissError)
        }
    }
    
    // Navigate back on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Tags", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.onEvent(TagEditorEvent.Save) },
                            enabled = uiState.hasChanges
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Save",
                                tint = if (uiState.hasChanges) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Info Section
                SectionHeader(title = "Basic Information")
                
                TagTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateTitle(it)) },
                    label = "Title",
                    leadingIcon = Icons.Rounded.MusicNote
                )
                
                TagTextField(
                    value = uiState.artist,
                    onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateArtist(it)) },
                    label = "Artist",
                    leadingIcon = Icons.Rounded.Person
                )
                
                TagTextField(
                    value = uiState.album,
                    onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateAlbum(it)) },
                    label = "Album",
                    leadingIcon = Icons.Rounded.Album
                )
                
                TagTextField(
                    value = uiState.albumArtist,
                    onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateAlbumArtist(it)) },
                    label = "Album Artist",
                    leadingIcon = Icons.Rounded.Person
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Details Section
                SectionHeader(title = "Details")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TagTextField(
                        value = uiState.year,
                        onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateYear(it)) },
                        label = "Year",
                        leadingIcon = Icons.Rounded.DateRange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    
                    TagTextField(
                        value = uiState.genre,
                        onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateGenre(it)) },
                        label = "Genre",
                        leadingIcon = Icons.Rounded.TheaterComedy,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TagTextField(
                        value = uiState.trackNumber,
                        onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateTrackNumber(it)) },
                        label = "Track #",
                        leadingIcon = Icons.Rounded.Numbers,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    
                    TagTextField(
                        value = uiState.discNumber,
                        onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateDiscNumber(it)) },
                        label = "Disc #",
                        leadingIcon = Icons.Rounded.DiscFull,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Comment Section
                SectionHeader(title = "Comment")
                
                TagTextField(
                    value = uiState.comment,
                    onValueChange = { viewModel.onEvent(TagEditorEvent.UpdateComment(it)) },
                    label = "Comment",
                    leadingIcon = Icons.Rounded.Comment,
                    singleLine = false,
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun TagTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        modifier = modifier.fillMaxWidth()
    )
}
