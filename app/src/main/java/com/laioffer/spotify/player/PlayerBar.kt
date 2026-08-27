package com.laioffer.spotify.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.laioffer.spotify.R
import com.laioffer.spotify.ui.theme.TransparentBlack

@Composable
fun PlayerBar(viewModel: PlayerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isVisible = uiState.album != null && uiState.song != null

    AnimatedVisibility(visible = isVisible) {
        PlayerBarContent(
            uiState = uiState,
            togglePlay = {
                if (uiState.isPlaying) viewModel.pause() else viewModel.play()
            },
            seekTo = viewModel::seekTo
        )
    }
}

@Composable
private fun PlayerBarContent(
    uiState: PlayerUiState,
    togglePlay: () -> Unit,
    seekTo: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TransparentBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = uiState.album?.cover,
                contentDescription = uiState.album?.name,
                modifier = Modifier
                    .width(60.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.song?.name.orEmpty(),
                    style = MaterialTheme.typography.body2,
                    color = Color.White
                )
                Text(
                    text = uiState.song?.lyric.orEmpty(),
                    style = MaterialTheme.typography.caption,
                    color = Color.LightGray
                )
            }
            Icon(
                painter = painterResource(
                    if (uiState.isPlaying) R.drawable.ic_pause_24
                    else R.drawable.ic_play_arrow_24
                ),
                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = togglePlay)
            )
        }
        SeekBar(
            currentMs = uiState.currentMs.toFloat(),
            durationMs = uiState.durationMs.toFloat(),
            seekTo = seekTo
        )
    }
}

@Composable
private fun SeekBar(
    currentMs: Float,
    durationMs: Float,
    seekTo: (Long) -> Unit
) {
    var seekBarPosition by remember { mutableStateOf(0f) }
    var seeking by remember { mutableStateOf(false) }
    val safeDuration = durationMs.coerceAtLeast(1f)

    if (!seeking) {
        seekBarPosition = currentMs.coerceIn(0f, safeDuration)
    }

    Slider(
        value = seekBarPosition,
        onValueChange = {
            seeking = true
            seekBarPosition = it
        },
        onValueChangeFinished = {
            if (durationMs > 0f) seekTo(seekBarPosition.toLong())
            seeking = false
        },
        valueRange = 0f..safeDuration,
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent,
            inactiveTrackColor = Color.LightGray,
            activeTrackColor = Color.Green
        )
    )
}
