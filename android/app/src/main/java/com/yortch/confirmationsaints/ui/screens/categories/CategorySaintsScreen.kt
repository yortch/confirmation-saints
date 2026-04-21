package com.yortch.confirmationsaints.ui.screens.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.ui.components.SaintRow
import com.yortch.confirmationsaints.ui.screens.saints.EmptyMessage
import com.yortch.confirmationsaints.viewmodel.SaintListViewModel

/** Saints matching a specific (group, value). Port of iOS `CategorySaintsListView`. */
@Composable
fun CategorySaintsScreen(
    groupId: String,
    valueId: String,
    onSaintClick: (String) -> Unit,
    viewModel: SaintListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    val saints = remember(state.saints, groupId, valueId) {
        viewModel.saintsForCategory(groupId, valueId)
    }

    if (saints.isEmpty()) {
        EmptyMessage(
            title = AppStrings.localized("No Saints Found", language),
            subtitle = AppStrings.localized("No saints match this category.", language),
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(saints, key = { it.id }) { saint ->
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
