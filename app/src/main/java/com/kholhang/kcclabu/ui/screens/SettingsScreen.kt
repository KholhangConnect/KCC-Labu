package com.kholhang.kcclabu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.constants.UiPref
import com.kholhang.kcclabu.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val theme by viewModel.theme.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val fontRes by viewModel.fontRes.collectAsState()
    val backgroundColor by viewModel.backgroundColor.collectAsState()
    val chorusColor by viewModel.chorusColor.collectAsState()
    val highlightsEnabled by viewModel.highlightsEnabled.collectAsState()
    val textIndent by viewModel.textIndent.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val lineSpacing by viewModel.lineSpacing.collectAsState()
    val pinchZoomEnabled by viewModel.pinchZoomEnabled.collectAsState()

    // Apply keep screen on setting
    LaunchedEffect(keepScreenOn) {
        if (context is android.app.Activity) {
            if (keepScreenOn) {
                context.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                context.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp), // Extra bottom padding to ensure last card is fully visible
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                ThemeSelector(
                    selectedTheme = theme,
                    onThemeSelected = { viewModel.setTheme(it) }
                )
            }

            // Text Settings Section
            SettingsSection(title = stringResource(R.string.settings_text_settings)) {
                FontSizeSlider(
                    fontSize = fontSize,
                    onFontSizeChange = { viewModel.setFontSize(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FontSelector(
                    selectedFont = fontRes,
                    onFontSelected = { viewModel.setFontRes(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LineSpacingSlider(
                    lineSpacing = lineSpacing,
                    onLineSpacingChange = { viewModel.setLineSpacing(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextIndentSlider(
                    textIndent = textIndent,
                    onTextIndentChange = { viewModel.setTextIndent(it) }
                )
            }

            // Color Settings Section
            SettingsSection(title = stringResource(R.string.settings_colors)) {
                BackgroundColorSelector(
                    selectedColor = backgroundColor,
                    onColorSelected = { viewModel.setBackgroundColor(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ChorusColorSelector(
                    selectedColor = chorusColor,
                    onColorSelected = { viewModel.setChorusColor(it) }
                )
            }

            // Display Settings Section
            SettingsSection(title = stringResource(R.string.settings_display)) {
                SettingSwitch(
                    title = stringResource(R.string.settings_highlights),
                    description = stringResource(R.string.settings_highlights_description),
                    iconRes = R.drawable.ic_text_format,
                    checked = highlightsEnabled,
                    onCheckedChange = { viewModel.setHighlightsEnabled(it) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SettingSwitch(
                    title = stringResource(R.string.settings_keep_screen_on),
                    description = stringResource(R.string.settings_keep_screen_on_description),
                    iconRes = R.drawable.ic_fullscreen,
                    checked = keepScreenOn,
                    onCheckedChange = { viewModel.setKeepScreenOn(it) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SettingSwitch(
                    title = stringResource(R.string.settings_pinch_zoom),
                    description = stringResource(R.string.settings_pinch_zoom_description),
                    iconRes = R.drawable.ic_text_format,
                    checked = pinchZoomEnabled,
                    onCheckedChange = { viewModel.setPinchZoomEnabled(it) }
                )
                
                // Extra spacing at bottom of last section
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: UiPref,
    onThemeSelected: (UiPref) -> Unit
) {
    val themes = listOf(
        UiPref.DAY to stringResource(R.string.theme_day),
        UiPref.NIGHT to stringResource(R.string.theme_night),
        UiPref.BATTERY_SAVER to stringResource(R.string.theme_battery_saver),
        UiPref.FOLLOW_SYSTEM to stringResource(R.string.theme_follow_system)
    )

    Column {
        themes.forEach { (theme, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(theme) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun FontSizeSlider(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_text_format),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_font_size),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = "${fontSize.toInt()}sp",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = fontSize,
            onValueChange = onFontSizeChange,
            valueRange = 12f..36f,
            steps = 23,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun FontSelector(
    selectedFont: Int,
    onFontSelected: (Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_text_format),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_font_family),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsViewModel.availableFonts.forEach { (font, nameRes) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFontSelected(font) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFont == font) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (selectedFont == font) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(nameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        if (selectedFont == font) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineSpacingSlider(
    lineSpacing: Float,
    onLineSpacingChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_text_format),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_line_spacing),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = String.format("%.1f", lineSpacing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = lineSpacing,
            onValueChange = onLineSpacingChange,
            valueRange = 1.0f..2.5f,
            steps = 14,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun TextIndentSlider(
    textIndent: Float,
    onTextIndentChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_text_format),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_text_indent),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = "${textIndent.toInt()}dp",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = textIndent,
            onValueChange = onTextIndentChange,
            valueRange = 0f..32f,
            steps = 31,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun BackgroundColorSelector(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_admin_panel_settings),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_background_color),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsViewModel.backgroundColors.forEach { (color, nameRes) ->
                Box(modifier = Modifier.weight(1f)) {
                    ColorOption(
                        color = color.toInt(),
                        nameRes = nameRes,
                        isSelected = selectedColor == color.toInt(),
                        onClick = { onColorSelected(color.toInt()) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChorusColorSelector(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_admin_panel_settings),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_chorus_color),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsViewModel.chorusColors.forEach { (color, nameRes) ->
                Box(modifier = Modifier.weight(1f)) {
                    ColorOption(
                        color = color.toInt(),
                        nameRes = nameRes,
                        isSelected = selectedColor == color.toInt(),
                        onClick = { onColorSelected(color.toInt()) }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    color: Int,
    nameRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color(color))
                .then(
                    if (isSelected) {
                        Modifier.padding(3.dp).background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ).padding(3.dp)
                    } else Modifier
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingSwitch(
    title: String,
    description: String,
    iconRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

