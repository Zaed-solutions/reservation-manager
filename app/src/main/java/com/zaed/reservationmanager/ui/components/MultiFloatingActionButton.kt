package com.zaed.reservationmanager.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

enum class MultiFabState {
    COLLAPSED, EXPANDED
}

class FabItem(
    val icon: ImageVector,
    val label: String,
    val onFabItemClicked: () -> Unit
)

@Composable
fun MultiFloatingActionButton(
    modifier: Modifier = Modifier,
    fabIcon: ImageVector,
    items: List<FabItem>,
    backgroundColor: Color,
    onStateChanged: ((state: MultiFabState) -> Unit)? = null
) {
    var currentState by remember { mutableStateOf(MultiFabState.COLLAPSED) }
    val stateTransition: Transition<MultiFabState> =
        updateTransition(targetState = currentState, label = "")
    val stateChange: () -> Unit = {
        currentState = if (stateTransition.currentState == MultiFabState.EXPANDED) {
            MultiFabState.COLLAPSED
        } else MultiFabState.EXPANDED
        onStateChanged?.invoke(currentState)
    }
    val rotation: Float by stateTransition.animateFloat(
        transitionSpec = {
            if (targetState == MultiFabState.EXPANDED) {
                spring(stiffness = Spring.StiffnessLow)
            } else {
                spring(stiffness = Spring.StiffnessMedium)
            }
        },
        label = ""
    ) { state ->
        if (state == MultiFabState.EXPANDED) 45f else 0f
    }
    val isEnable = currentState == MultiFabState.EXPANDED

    BackHandler(isEnable) {
        currentState = MultiFabState.COLLAPSED
    }

    val modifier2 = if (currentState == MultiFabState.EXPANDED)
        modifier
            .fillMaxSize()
            .clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                currentState = MultiFabState.COLLAPSED
            } else modifier

    Box(modifier = modifier2, contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .heightIn(max = 400.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            if (currentState == MultiFabState.EXPANDED) {
                val density = LocalDensity.current
                var targetRadiusDp by remember { mutableStateOf(0.dp) }
                val animatedRadiusDp by animateFloatAsState(
                    targetValue = targetRadiusDp.value,
                    animationSpec = tween(durationMillis = 200)
                )
                val layoutDirection = LocalLayoutDirection.current
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.scaleX = 2.2f
                            this.scaleY = 2.1f
                        }
                ) {
                    val animatedRadiusPx = with(density) { animatedRadiusDp.dp.toPx() }
                    val horizontalPosition = when (layoutDirection) {
                        LayoutDirection.Ltr -> 150f
                        LayoutDirection.Rtl -> -150f
                    }
                    translate(horizontalPosition, top = 300f) {
                        drawCircle(backgroundColor, radius = animatedRadiusPx, alpha = 0.85f)
                    }
                }
                LaunchedEffect(Unit) {
                    targetRadiusDp = 200.dp
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (currentState == MultiFabState.EXPANDED) {
                    items.forEach { item ->
                        SmallFloatingActionButtonRow(
                            item = item,
                            stateTransition = stateTransition,
                            stateChange = stateChange,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                FloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        stateChange()
                    }) {
                    Icon(
                        imageVector = fabIcon,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

        }
    }

}


@Composable
fun SmallFloatingActionButtonRow(
    item: FabItem,
    stateTransition: Transition<MultiFabState>,
    stateChange: () -> Unit
) {
    val alpha: Float by stateTransition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 50)
        }, label = ""
    ) { state ->
        if (state == MultiFabState.EXPANDED) 1f else 0f
    }
    val scale: Float by stateTransition.animateFloat(
        label = ""
    ) { state ->
        if (state == MultiFabState.EXPANDED) 1.0f else 0f
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(animateFloatAsState((alpha), label = "").value)
            .scale(animateFloatAsState(targetValue = scale, label = "").value)
            .clickable {
                item.onFabItemClicked()
                stateChange()
            }
    ) {
        Text(
            text = item.label,
            modifier = Modifier
                .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        )
        SmallFloatingActionButton(
            shape = CircleShape,
            modifier = Modifier
                .padding(4.dp),
            onClick = {
                item.onFabItemClicked()
                stateChange()
            },
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}