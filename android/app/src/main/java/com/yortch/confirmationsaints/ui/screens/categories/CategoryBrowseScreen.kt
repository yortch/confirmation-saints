package com.yortch.confirmationsaints.ui.screens.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yortch.confirmationsaints.data.model.CategoryGroup
import com.yortch.confirmationsaints.data.model.CategoryValue
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.viewmodel.SaintListViewModel

/**
 * Browse categories grouped by type (patronage, interests, age, region, etc.).
 * Tapping a value navigates to [CategorySaintsScreen]. Mirrors iOS
 * `CategoryBrowseView`.
 */
@Composable
fun CategoryBrowseScreen(
    onValueClick: (groupId: String, valueId: String, title: String) -> Unit,
    viewModel: SaintListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(state.categories, key = { it.id }) { group ->
            CategoryGroupSection(
                group = group,
                saintsVersion = state.saints.size,
                viewModel = viewModel,
                onValueClick = onValueClick,
            )
        }
        if (state.categories.isEmpty()) {
            item {
                Text(
                    AppStrings.localized("Content Loading...", language),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun CategoryGroupSection(
    group: CategoryGroup,
    saintsVersion: Int,
    viewModel: SaintListViewModel,
    onValueClick: (String, String, String) -> Unit,
) {
    val language = LocalAppLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Grid4x4, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            group.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // 2-column value grid. Heights are constrained per row so the
        // enclosing LazyColumn scroll stays smooth (no nested infinite).
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            // Height sized to content — compute from values count (2 per row, ~70dp each).
            modifier = Modifier.height(((group.values.size + 1) / 2 * 70).dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            userScrollEnabled = false,
        ) {
            items(group.values, key = { it.id }) { value ->
                CategoryValueCard(
                    group = group,
                    value = value,
                    count = remember(group.id, value.id, saintsVersion) {
                        viewModel.saintsForCategory(group.id, value.id).size
                    },
                    onClick = { onValueClick(group.id, value.id, value.label) },
                )
            }
        }
    }
}

@Composable
private fun CategoryValueCard(
    group: CategoryGroup,
    value: CategoryValue,
    count: Int,
    onClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val saintWord = if (count == 1) AppStrings.localized("saint", language)
    else AppStrings.localized("saints", language)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    value.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "$count $saintWord",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
