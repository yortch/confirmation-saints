package com.yortch.confirmationsaints.ui.screens.saints

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yortch.confirmationsaints.data.model.Saint
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.ui.components.AppFilterChip
import com.yortch.confirmationsaints.ui.components.SaintRow
import com.yortch.confirmationsaints.viewmodel.SaintListViewModel

/**
 * Searchable saints list with filter chips. Port of iOS `SaintListView`.
 *
 * Reactive to language change via [LocalAppLanguage] — the view model reloads
 * saints whenever the language flips (see [SaintListViewModel]).
 */
@Composable
fun SaintListScreen(
    onSaintClick: (saintId: String) -> Unit,
    viewModel: SaintListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current

    Column(Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = state.filters.searchText,
            onValueChange = viewModel::setSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(AppStrings.localized("Name, interest, country...", language)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.filters.searchText.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearch("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = AppStrings.localized("Clear All", language),
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )

        // Filter chips (horizontal scroll)
        FilterChipsRow(state, viewModel)

        if (state.filters.hasActiveFilters) {
            ActiveFiltersBar(
                count = state.filteredSaints.size,
                onClear = viewModel::clearFilters,
            )
        }

        Divider()

        when {
            state.isLoading -> EmptyMessage(
                title = AppStrings.localized("Content Loading...", language),
                subtitle = AppStrings.localized("Saints data is loading...", language),
            )
            state.saints.isEmpty() -> EmptyMessage(
                title = AppStrings.localized("No Saints Found", language),
                subtitle = AppStrings.localized("Saints data is loading...", language),
            )
            state.filteredSaints.isEmpty() -> EmptyMessage(
                title = AppStrings.localized("No Saints Found", language),
                subtitle = AppStrings.localized("No saints match this category.", language),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(state.filteredSaints, key = { it.id }) { saint: Saint ->
                    SaintRow(
                        saint = saint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSaintClick(saint.id) },
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    state: com.yortch.confirmationsaints.viewmodel.SaintListUiState,
    viewModel: SaintListViewModel,
) {
    val language = LocalAppLanguage.current
    val scrollState = rememberScrollState()
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppFilterChip(
            label = AppStrings.localized("Young Saints", language),
            icon = Icons.Default.AutoAwesome,
            selected = state.filters.selectedAgeCategory == "young",
            onClick = {
                viewModel.setAgeCategory(
                    if (state.filters.selectedAgeCategory == "young") null else "young",
                )
            },
        )
        AppFilterChip(
            label = AppStrings.localized("Modern Day Saints", language),
            icon = Icons.Default.Schedule,
            selected = state.filters.selectedEra == "modern-day",
            onClick = {
                viewModel.setEra(
                    if (state.filters.selectedEra == "modern-day") null else "modern-day",
                )
            },
        )
        AppFilterChip(
            label = AppStrings.localized("Female Saints", language),
            icon = Icons.Default.Person,
            selected = state.filters.selectedGender == "female",
            onClick = {
                viewModel.setGender(
                    if (state.filters.selectedGender == "female") null else "female",
                )
            },
        )
        AppFilterChip(
            label = AppStrings.localized("Male Saints", language),
            icon = Icons.Default.Person,
            selected = state.filters.selectedGender == "male",
            onClick = {
                viewModel.setGender(
                    if (state.filters.selectedGender == "male") null else "male",
                )
            },
        )

        // Life state chips (canonical ids: married / religious / single)
        listOf("married", "religious", "single").forEach { lifeState ->
            val label = AppStrings.localized(lifeState.replaceFirstChar { it.uppercaseChar() }, language)
            AppFilterChip(
                label = label,
                icon = Icons.Default.Favorite,
                selected = state.filters.selectedLifeState == lifeState,
                onClick = {
                    viewModel.setLifeState(
                        if (state.filters.selectedLifeState == lifeState) null else lifeState,
                    )
                },
            )
        }

        // Region chips — canonical ids are lowercase (viewmodel filters case-insensitively).
        listOf("Europe", "Americas", "Africa", "Asia", "Middle East").forEach { region ->
            val key = region.lowercase()
            AppFilterChip(
                label = AppStrings.localized(region, language),
                icon = Icons.Default.Public,
                selected = state.filters.selectedRegion == key,
                onClick = {
                    viewModel.setRegion(
                        if (state.filters.selectedRegion == key) null else key,
                    )
                },
            )
        }
    }
}

@Composable
private fun ActiveFiltersBar(count: Int, onClear: () -> Unit) {
    val language = LocalAppLanguage.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$count ${AppStrings.localized("results", language)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onClear) {
            Text(
                AppStrings.localized("Clear All", language),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
internal fun EmptyMessage(title: String, subtitle: String? = null) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
