package com.ginkcoding.photocleaner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 粒子特效组件
 * 用于删除照片时的视觉反馈
 */
data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color,
    val alpha: Float = 1f
)

@Composable
fun ParticleEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    effectDuration: Int = 800,
    onComplete: () -> Unit = {}
) {
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    var elapsedTime by remember { mutableStateOf(0) }
    
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(effectDuration, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        )
    )
    
    LaunchedEffect(Unit) {
        // 初始化粒子
        particles = List(particleCount) {
            Particle(
                x = Random.nextFloat() * 100,
                y = Random.nextFloat() * 100,
                vx = (Random.nextFloat() - 0.5f) * 2,
                vy = (Random.nextFloat() - 0.5f) * 2 - 1,
                radius = Random.nextFloat() * 3 + 1,
                color = if (Random.nextBoolean()) Color(0xFF00D4FF) else Color(0xFF0066FF),
                alpha = 1f
            )
        }
        
        // 动画循环
        for (i in 0..effectDuration / 16) {
            delay(16)
            elapsedTime += 16
            
            particles = particles.map { particle ->
                particle.copy(
                    x = particle.x + particle.vx,
                    y = particle.y + particle.vy,
                    vy = particle.vy + 0.05f, // 重力
                    alpha = 1f - (elapsedTime.toFloat() / effectDuration)
                )
            }
            
            if (elapsedTime >= effectDuration) {
                onComplete()
                break
            }
        }
    }
    
    Canvas(
        modifier = modifier.size(200.dp)
    ) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.radius.dp.toPx(),
                center = Offset(particle.x.dp.toPx(), particle.y.dp.toPx())
            )
        }
    }
}

/**
 * 删除动画容器
 * 包含粒子特效和缩放动画
 */
@Composable
fun DeleteAnimation(
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit
) {
    val transition = rememberInfiniteTransition()
    
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    
    val rotate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = tween(500, easing = LinearEasing)
    )
    
    Canvas(
        modifier = modifier.size(100.dp)
    ) {
        // 绘制缩放圆圈
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = scale),
            radius = size.minDimension * scale / 2,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
    
    ParticleEffect(
        modifier = modifier,
        particleCount = 30,
        effectDuration = 600,
        onComplete = onAnimationComplete
    )
}
