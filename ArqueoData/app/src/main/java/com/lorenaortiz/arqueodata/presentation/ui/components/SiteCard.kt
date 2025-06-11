package com.lorenaortiz.arqueodata.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteCard(
    site: ArchaeologicalSite,
    onCardClick: (ArchaeologicalSite) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onCardClick(site) },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = site.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = site.location,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = site.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = site.period,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = site.type,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
} 