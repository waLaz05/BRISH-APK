package com.katchy.focuslive.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    play: Boolean,
    onFinished: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.katchy.focuslive.R.raw.confetti))
    
    // We want to trigger animation when play becomes true.
    // However, Lottie compose works with state.
    // If play is true, we want to play once.
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = play,
        restartOnPlay = true
    )

    LaunchedEffect(progress) {
        if (play && progress == 1f) {
            onFinished()
        }
    }

    if (play) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier
        )
    }
}
