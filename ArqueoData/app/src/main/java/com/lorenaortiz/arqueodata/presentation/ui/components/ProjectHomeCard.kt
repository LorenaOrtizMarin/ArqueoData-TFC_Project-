package com.lorenaortiz.arqueodata.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lorenaortiz.arqueodata.R
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.domain.model.User
import com.lorenaortiz.arqueodata.presentation.viewmodel.TeamMemberViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProjectHomeCard(
    site: ArchaeologicalSite,
    onCardClick: (ArchaeologicalSite) -> Unit,
    modifier: Modifier = Modifier,
    teamMemberViewModel: TeamMemberViewModel = hiltViewModel()
) {
    var director by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(site.userId) {
        director = teamMemberViewModel.getUserInfo(site.userId)
    }

    Card(
        modifier = modifier
            .height(220.dp)
            .clickable { onCardClick(site) },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Director
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (director?.photoUrl != null) {
                    AsyncImage(
                        model = director?.photoUrl,
                        contentDescription = "Foto director",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Sin foto",
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = director?.nombre ?: "Director/es",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Imagen del proyecto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!site.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = site.imageUrl,
                        contentDescription = "Imagen del proyecto",
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFBDBDBD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Sin imagen",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                // Icono de editar (opcional, si aplica)
                // Icon(
                //     imageVector = Icons.Default.Edit,
                //     contentDescription = "Editar",
                //     modifier = Modifier
                //         .align(Alignment.TopEnd)
                //         .padding(8.dp)
                //         .size(20.dp),
                //     tint = Color.White
                // )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Nombre del proyecto
            Text(
                text = site.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF00695C)
            )
            // Ubicación
            Text(
                text = site.location,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
} 