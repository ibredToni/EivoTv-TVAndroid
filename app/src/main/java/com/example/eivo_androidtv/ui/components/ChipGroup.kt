package com.example.eivo_androidtv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun ChipGroup(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onItemFocused: ((String) -> Unit)? = null,
    focusRequesters: List<FocusRequester>? = null

) {
    val listState = rememberLazyListState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(items) { index, item ->
                SelectableChip(
                    text = item,
                    isSelected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                    onFocus = { onItemFocused?.invoke(item) },
                    modifier = if (focusRequesters != null && index < focusRequesters.size) {
                        Modifier.focusRequester(focusRequesters[index])
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}