package com.katchy.focuslive.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import com.katchy.focuslive.data.model.MascotType
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap

enum class BrishPose {
    DEFAULT,    // Home (Fish)
    PLANNER,    // Holding Bat/Stick (Threatening)
    FINANCE,    // Holding Money Bag
    NOTES,      // Holding Spray Can (Graffiti)
    HABITS,     // Holding Dumbbell (Workout)
    FOCUS,      // Headphones (Deep Work)
    CHAT,        // Arms crossed / Listening
    GARDENING   // Watering plants
}



@Composable
fun BrishMascotAnimation(
    modifier: Modifier = Modifier,
    pose: BrishPose = BrishPose.DEFAULT,
    mascotType: MascotType = MascotType.POPPIN,
    level: Int = 1
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot")
    
    // --- ENTRANCE ANIMATIONS ---
    val jumpAnim = remember { Animatable(0f) }
    val handWaveAnim = remember { Animatable(0f) }
    
    // Trigger entrance animation whenever pose changes
    LaunchedEffect(pose) {
        // Reset
        jumpAnim.snapTo(0f)
        handWaveAnim.snapTo(0f)
        
        // Sequence: Jump Up -> Land -> Hand Wave
        val jumpDuration = 300
        
        // Jump Up
        jumpAnim.animateTo(
            targetValue = -20f,
            animationSpec = tween(durationMillis = jumpDuration / 2, easing = EaseOutQuad)
        )
        // Land
        jumpAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = jumpDuration / 2, easing = EaseInQuad)
        )
        
        // Hand Wave (Raise hand momentarily)
        handWaveAnim.animateTo(
            targetValue = -130f, // Raise arm up high
            animationSpec = tween(400)
        )
        delay(200)
        handWaveAnim.animateTo(
            targetValue = 0f, // Return to normal
            animationSpec = tween(400)
        )
    }

    // --- IDLE ANIMATIONS ---
    
    // Breathing/Bobbing animation
    val bodyBob by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f, 
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bodyBob"
    )
    
    // Combine jump and bob
    val bodyOffset = bodyBob + jumpAnim.value
    
    // Blinking animation
    val eyeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                1f at 0
                1f at 2800
                0.1f at 2900
                1f at 3000
            },
            repeatMode = RepeatMode.Restart
        ), label = "eyeBlink"
    )

    // Arm animation (Generic idle)
    val armRotationIdle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "armRot"
    )
    
    // Eating Animation (Specific to DEFAULT pose)
    // Arm moves from side to mouth
    val eatingArmRotation by infiniteTransition.animateFloat(
        initialValue = 0f, 
        targetValue = -45f, // Move towards mouth
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0
                0f at 1000 // Hold down
                (-50f) at 1300 // Move up quickly
                (-40f) at 1500 // Munch
                (-50f) at 1700 // Munch
                0f at 2000 // Return down
            },
            repeatMode = RepeatMode.Restart
        ), label = "eating"
    )

    // GARDENING ANIMATION: Pouring Water
    val wateringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f, // Tilt forward
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "watering"
    )

    // Water Droplets (Y offset)
    val dropletOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "droplets"
    )

    // Determine finalized arm rotation based on state
    // If waving (entrance), use wave. 
    // If not waving: Use specific animation per pose.
    val isWaving = handWaveAnim.value < -10f // Threshold to detect active wave
    
    val currentArmRotation = if (handWaveAnim.value != 0f) {
        handWaveAnim.value // Priority to entrance wave
    } else {
         when (pose) {
             BrishPose.DEFAULT -> eatingArmRotation
             BrishPose.GARDENING -> wateringRotation
             else -> armRotationIdle
         }
    }
    
    // Mouth open/shut for eating
    val beakOpen by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0
                0f at 1300
                5f at 1400 // Open
                0f at 1500 // Close
                5f at 1600 // Open
                0f at 1700 // Close
                0f at 2000
            },
            repeatMode = RepeatMode.Restart
        ), label = "beak"
    )

    // --- PACO SPECIFIC ANIMATIONS ---
    // 1. Bounce (Default)
    val pacoBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pacoBounce"
    )

    // 2. Balance sway (Planner)
    val pacoBalance by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pacoBalance"
    )

    // 3. Roll (Habits) - X offset
    val pacoRoll by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pacoRoll"
    )

    // 4. Shake (Notes)
    val pacoShake by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pacoShake"
    )

    // --- PANDA SPECIFIC ANIMATIONS ---
    // 1. Head Nod (Default - Beat)
    val pandaHeadNod by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pandaNod"
    )
    
    // 2. DJ Scratch Hand (Focus)
    val pandaDJHand by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pandaDJ"
    )

    // --- CEBRIC SPECIFIC ANIMATIONS ---
    // 1. Head Bob (Default/Focus) - Gentle float
    val cebricBob by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "cebricBob"
    )

    // 2. Propeller Spin (Default) - Continuous
    val cebricProp by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "cebricProp"
    )

    // 3. Weight Lift (Habits) - Up/Down
    val cebricLift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "cebricLift"
    )
    
    // 3. Foot Tap (Planner - Waiting)
    val pandaFootTap by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f, // Lift toe
        animationSpec = infiniteRepeatable(
            animation = keyframes { 
                durationMillis = 800
                0f at 0
                (-5f) at 200
                0f at 400
                0f at 800
            },
            repeatMode = RepeatMode.Restart
        ), label = "pandaTap"
    )

    // --- POPPIN SPECIFIC ANIMATIONS (Coach) ---
    // 1. Whistle Blow (Default)
    val poppinWhistle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f, // Scale/Move whistle
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0
                0f at 1000
                10f at 1200 // Blow
                0f at 1400
                10f at 1600 // Blow
                0f at 3000
            },
            repeatMode = RepeatMode.Restart
        ), label = "poppinWhistle"
    )
    
    // 2. Stopwatch Tick (Habits)
    val poppinTicking by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "poppinTick"
    )

    // --- KITTY SPECIFIC ANIMATIONS (Practical) ---
    // 1. Yoga Stretch (Habits)
    val kittyStretch by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f, // Offset Y
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "kittyStretch"
    )
    
    // 2. Reading Head Bob (Planner)
    val kittyRead by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "kittyRead"
    )




    // --- CEBRIC SPECIFIC ANIMATIONS ---
    // 1. Neck Sway (Default) - Reduced amplitude for steadier look
    val cebricNeckSway by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "cebricSway"
    )
    
    // 2. Chew Motion (Default) - Smoother
    val cebricChew by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 1000
                3f at 1300
                0f at 1600
                3f at 1900
                0f at 2200
                0f at 4000
            },
            repeatMode = RepeatMode.Restart
        ), label = "cebricChew"
    )

    // 3. Neck Lift (Habits - "Neck Day") - Adjusted range
    val cebricNeckLift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f, 
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "cebricLift"
    )


    // --- GLOBAL ANIMATIONS (REFINED - NO BOUNCE) ---
    // 1. Subtle Breathe (Replaces Body Bob)
    val breatheAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f, // Very subtle up/down
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "breathe"
    )

    // 2. Reading Eye Pan (For Planner/Notes)
    val eyePanAnim by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "eyePan"
    )

    // 3. Head Bob (For Music/Focus)
    val headBobAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600 // ~100 BPM
                0f at 0
                5f at 300
                0f at 600
            },
            repeatMode = RepeatMode.Restart
        ), label = "headBob"
    )
    
    // 4. Weight Lift (For Habits)
    val liftAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0
                0f at 500
                -15f at 1000 // Lift
                -15f at 1500 // Hold
                0f at 2000 // Drop
            },
            repeatMode = RepeatMode.Restart
        ), label = "lift"
    )

    // --- KITTY SPECIFIC ANIMATIONS (NEW) ---
    // Tail Wag (Continuous Sine Wave)
    val tailWagAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "tailWag"
    )

    // Ear Twitch (Random intervals)
    val earTwitchAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000 // Occurs every 5 seconds
                0f at 0
                0f at 4000
                15f at 4100 // Twitch
                0f at 4200
                0f at 5000
            },
            repeatMode = RepeatMode.Restart
        ), label = "earTwitch"
    )

    Canvas(modifier = modifier.size(120.dp, 140.dp)) {
        val centerX = size.width / 2
        val bottomY = size.height - 10f
        
        // Use breatheAnim instead of bounce
        val bodyOffset = breatheAnim

        // --- LEVEL SHINE (Aura) ---
        if (level >= 10) {
            val auraColor = if (level >= 20) Color(0xFFFFD700).copy(alpha = 0.3f) else Color(0xFFC0C0C0).copy(alpha = 0.3f)
            drawCircle(auraColor, radius = 60.dp.toPx(), center = Offset(centerX, size.height / 2))
        }
        
        // --- PREPARE COLORS ---
        val mainColor = when(mascotType) {
            MascotType.POPPIN -> Color(0xFF1F2937) // Black
            MascotType.KITTY -> Color(0xFF9CA3AF) // Gray
            MascotType.PACO -> Color(0xFFF97316) // Orange
            MascotType.PANDA -> Color.White // Panda Body is White
            MascotType.CEBRIC -> Color(0xFFFEF3C7) // Cream
            MascotType.TURTLE -> Color(0xFF10B981) // Emerald Green
            MascotType.LLAMA -> Color(0xFFD4B483) // Tan/Beige
        }
        val secondaryColor = when(mascotType) {
            MascotType.POPPIN -> Color.White // Belly
            MascotType.KITTY -> Color(0xFFE5E7EB) // Lighter Gray Belly
            MascotType.PACO -> Color(0xFFFDBA74) // Lighter Orange Belly
            MascotType.PANDA -> Color(0xFF1F2937) // Panda Limbs/Ears are Black
            MascotType.CEBRIC -> Color(0xFF92400E) // Brown Spots
            MascotType.TURTLE -> Color(0xFF047857) // Dark Green Shell
            MascotType.LLAMA -> Color(0xFFFEF3C7) // Cream Wool
        }

        // --- DRAW BODY & HEAD ---
        when(mascotType) {
            MascotType.TURTLE -> {
                // --- TORTY V3: SAGE MODE (Upright Ninja Style) ---
                val shellColor = secondaryColor // Dark Green
                val skinColor = mainColor // Emerald
                val plastronColor = Color(0xFFD1FAE5) // Light Belly
                
                val turtleCx = centerX
                // Poppin-style body center
                val bodyCy = 75.dp.toPx() + bodyOffset
                
                // 1. SHELL RIM (Background)
                drawOval(shellColor, topLeft = Offset(turtleCx - 40.dp.toPx(), bodyCy - 35.dp.toPx()), size = Size(80.dp.toPx(), 90.dp.toPx()))
                
                // 2. FEET (Chunky pads)
                drawOval(skinColor, topLeft = Offset(turtleCx - 35.dp.toPx(), bodyCy + 35.dp.toPx()), size = Size(25.dp.toPx(), 20.dp.toPx()))
                drawOval(skinColor, topLeft = Offset(turtleCx + 10.dp.toPx(), bodyCy + 35.dp.toPx()), size = Size(25.dp.toPx(), 20.dp.toPx()))
                
                // 3. BODY (Plastron/Belly)
                drawOval(skinColor, topLeft = Offset(turtleCx - 28.dp.toPx(), bodyCy - 25.dp.toPx()), size = Size(56.dp.toPx(), 70.dp.toPx()))
                // Plastron Plates
                drawRect(plastronColor, topLeft = Offset(turtleCx - 15.dp.toPx(), bodyCy - 15.dp.toPx()), size = Size(30.dp.toPx(), 45.dp.toPx()), style = Stroke(width = 2.dp.toPx()))
                drawLine(plastronColor, start = Offset(turtleCx - 15.dp.toPx(), bodyCy + 5.dp.toPx()), end = Offset(turtleCx + 15.dp.toPx(), bodyCy + 5.dp.toPx()), strokeWidth = 2.dp.toPx())
                
                // 4. HEAD
                val headBob = if(pose == BrishPose.FOCUS) headBobAnim else 0f
                val headCy = bodyCy - 40.dp.toPx() + headBob
                
                drawCircle(skinColor, radius = 26.dp.toPx(), center = Offset(turtleCx, headCy))
                
                // Bandana (Ninja Style - Optional based on pose, or default orange/red mask)
                val maskColor = Color(0xFFF97316) // Orange Mask
                drawRect(maskColor, topLeft = Offset(turtleCx - 24.dp.toPx(), headCy - 8.dp.toPx()), size = Size(48.dp.toPx(), 16.dp.toPx()))
                
                // 5. FACE
                val eyeX = if(pose == BrishPose.PLANNER) eyePanAnim else 0f
                // Eyes (White over mask)
                drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(turtleCx - 10.dp.toPx() + eyeX, headCy))
                drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(turtleCx + 10.dp.toPx() + eyeX, headCy))
                // Pupils
                drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(turtleCx - 10.dp.toPx() + eyeX, headCy))
                drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(turtleCx + 10.dp.toPx() + eyeX, headCy))
                
                // 6. PROPS
                // Arms (Chunky)
                when(pose) {
                    BrishPose.HABITS -> {
                        // Lifting
                        val armY = bodyCy - 5.dp.toPx() + liftAnim
                        drawCircle(skinColor, radius = 8.dp.toPx(), center = Offset(turtleCx - 35.dp.toPx(), armY))
                        drawCircle(skinColor, radius = 8.dp.toPx(), center = Offset(turtleCx + 35.dp.toPx(), armY))
                        // Dumbbell
                        drawLine(Color.Gray, start = Offset(turtleCx - 40.dp.toPx(), armY), end = Offset(turtleCx + 40.dp.toPx(), armY), strokeWidth = 4.dp.toPx())
                        drawRect(Color.Black, topLeft = Offset(turtleCx - 42.dp.toPx(), armY - 5.dp.toPx()), size = Size(6.dp.toPx(), 10.dp.toPx()))
                        drawRect(Color.Black, topLeft = Offset(turtleCx + 36.dp.toPx(), armY - 5.dp.toPx()), size = Size(6.dp.toPx(), 10.dp.toPx()))
                    }
                    else -> {
                        // Default Arms
                        drawOval(skinColor, topLeft = Offset(turtleCx - 36.dp.toPx(), bodyCy - 10.dp.toPx()), size = Size(12.dp.toPx(), 25.dp.toPx()))
                        drawOval(skinColor, topLeft = Offset(turtleCx + 24.dp.toPx(), bodyCy - 10.dp.toPx()), size = Size(12.dp.toPx(), 25.dp.toPx()))
                    }
                }
            }
            
            MascotType.LLAMA -> {
                // --- KUZCO (The Peruvian Llama) - REDESIGNED V3 (Bug Fixes) ---
                val furColor = mainColor // Tan (Body)
                val woolColor = secondaryColor // Cream (Neck/Wool)
                val hoofColor = Color(0xFF5D4037) // Dark Brown
                
                // Animation Refs
                val headBob = if(pose == BrishPose.FOCUS) headBobAnim else 0f
                val chewY = if(pose == BrishPose.DEFAULT) breatheAnim else 0f
                val headX = centerX
                // Move body slightly up to fit legs better in frame
                val bodyBaseY = 95.dp.toPx() + bodyOffset

                // 1. LEGS (Patitas) - Adjusted positions to look connected
                // Back Legs (Darker/Behind)
                val legY = bodyBaseY + 10.dp.toPx()
                drawOval(furColor, topLeft = Offset(headX - 32.dp.toPx(), legY), size = Size(14.dp.toPx(), 28.dp.toPx()))
                drawOval(furColor, topLeft = Offset(headX + 18.dp.toPx(), legY), size = Size(14.dp.toPx(), 28.dp.toPx()))
                drawOval(hoofColor, topLeft = Offset(headX - 32.dp.toPx(), legY + 22.dp.toPx()), size = Size(14.dp.toPx(), 8.dp.toPx()))
                drawOval(hoofColor, topLeft = Offset(headX + 18.dp.toPx(), legY + 22.dp.toPx()), size = Size(14.dp.toPx(), 8.dp.toPx()))
                
                // Front Legs
                val frontLegY = bodyBaseY + 15.dp.toPx()
                drawOval(furColor, topLeft = Offset(headX - 22.dp.toPx(), frontLegY), size = Size(14.dp.toPx(), 28.dp.toPx()))
                drawOval(furColor, topLeft = Offset(headX + 8.dp.toPx(), frontLegY), size = Size(14.dp.toPx(), 28.dp.toPx()))
                drawOval(hoofColor, topLeft = Offset(headX - 22.dp.toPx(), frontLegY + 22.dp.toPx()), size = Size(14.dp.toPx(), 8.dp.toPx()))
                drawOval(hoofColor, topLeft = Offset(headX + 8.dp.toPx(), frontLegY + 22.dp.toPx()), size = Size(14.dp.toPx(), 8.dp.toPx()))


                // 2. NECK & BODY (Unified Shape)
                val llamaPath = Path().apply {
                    // Start at bottom center
                    moveTo(headX, bodyBaseY + 20.dp.toPx())
                    // Bottom curve (Body) - Wider to cover leg tops
                    quadraticTo(headX + 48.dp.toPx(), bodyBaseY + 20.dp.toPx(), headX + 42.dp.toPx(), bodyBaseY - 20.dp.toPx())
                    // Neck going up
                    quadraticTo(headX + 30.dp.toPx(), bodyBaseY - 40.dp.toPx(), headX + 20.dp.toPx(), bodyBaseY - 80.dp.toPx() + headBob) // Neck Top Right
                    // Head/Neck Join
                    lineTo(headX - 20.dp.toPx(), bodyBaseY - 80.dp.toPx() + headBob) // Neck Top Left
                    // Neck going down
                    quadraticTo(headX - 30.dp.toPx(), bodyBaseY - 40.dp.toPx(), headX - 42.dp.toPx(), bodyBaseY - 20.dp.toPx())
                    // Bottom curve
                    quadraticTo(headX - 48.dp.toPx(), bodyBaseY + 20.dp.toPx(), headX, bodyBaseY + 20.dp.toPx())
                    close()
                }
                drawPath(llamaPath, woolColor)
                
                // Chest Wool (Fluffy)
                drawCircle(woolColor, radius = 26.dp.toPx(), center = Offset(headX, bodyBaseY - 20.dp.toPx()))
                
                // 3. OUTFITS (Body Layer)
                // 3. OUTFITS (Body Layer)
                when(pose) {
                    BrishPose.DEFAULT -> {
                        // Poncho (Colorful stripes) - Draped shape matching body curve
                        val ponchoPath = Path().apply {
                            moveTo(headX - 22.dp.toPx(), bodyBaseY - 45.dp.toPx()) // Neck Left
                            lineTo(headX + 22.dp.toPx(), bodyBaseY - 45.dp.toPx()) // Neck Right
                            // Drapes over shoulders
                            quadraticTo(headX + 50.dp.toPx(), bodyBaseY - 10.dp.toPx(), headX + 48.dp.toPx(), bodyBaseY + 15.dp.toPx()) 
                            // Bottom Point
                            lineTo(headX, bodyBaseY + 35.dp.toPx()) 
                            // Left Side
                            lineTo(headX - 48.dp.toPx(), bodyBaseY + 15.dp.toPx())
                            quadraticTo(headX - 50.dp.toPx(), bodyBaseY - 10.dp.toPx(), headX - 22.dp.toPx(), bodyBaseY - 45.dp.toPx())
                            close()
                        }
                        drawPath(ponchoPath, Color(0xFFD32F2F)) // Red Base
                        // Stripes (Curved)
                        val stripePath1 = Path().apply {
                             moveTo(headX - 38.dp.toPx(), bodyBaseY - 10.dp.toPx())
                             quadraticTo(headX, bodyBaseY + 5.dp.toPx(), headX + 38.dp.toPx(), bodyBaseY - 10.dp.toPx())
                        }
                        drawPath(stripePath1, Color(0xFFFFC107), style = Stroke(6.dp.toPx()))
                        
                        val stripePath2 = Path().apply {
                             moveTo(headX - 28.dp.toPx(), bodyBaseY + 10.dp.toPx())
                             quadraticTo(headX, bodyBaseY + 20.dp.toPx(), headX + 28.dp.toPx(), bodyBaseY + 10.dp.toPx())
                        }
                        drawPath(stripePath2, Color(0xFF388E3C), style = Stroke(6.dp.toPx()))
                    }
                    BrishPose.PLANNER -> {
                         // Peru Jersey (La Blanquirroja) - Fitted to Body Oval
                         val jerseyPath = Path().apply {
                             // Neck
                             moveTo(headX - 26.dp.toPx(), bodyBaseY - 40.dp.toPx()) 
                             quadraticTo(headX, bodyBaseY - 35.dp.toPx(), headX + 26.dp.toPx(), bodyBaseY - 40.dp.toPx())
                             // Shoulders/Sides
                             quadraticTo(headX + 42.dp.toPx(), bodyBaseY - 10.dp.toPx(), headX + 38.dp.toPx(), bodyBaseY + 15.dp.toPx())
                             // Bottom Hem (Curved with belly)
                             quadraticTo(headX, bodyBaseY + 25.dp.toPx(), headX - 38.dp.toPx(), bodyBaseY + 15.dp.toPx())
                             // Left Side
                             quadraticTo(headX - 42.dp.toPx(), bodyBaseY - 10.dp.toPx(), headX - 26.dp.toPx(), bodyBaseY - 40.dp.toPx())
                             close()
                         }
                         drawPath(jerseyPath, Color.White)
                         
                         // Red Sash (Diagonal & Curved)
                         val sashPath = Path().apply {
                             moveTo(headX - 26.dp.toPx(), bodyBaseY - 40.dp.toPx()) // Top Left Shoulder
                             lineTo(headX - 10.dp.toPx(), bodyBaseY - 40.dp.toPx())
                             // Curve down across belly
                             cubicTo(headX - 5.dp.toPx(), bodyBaseY - 20.dp.toPx(), 
                                     headX + 20.dp.toPx(), bodyBaseY - 5.dp.toPx(),
                                     headX + 36.dp.toPx(), bodyBaseY + 15.dp.toPx()) 
                             lineTo(headX + 18.dp.toPx(), bodyBaseY + 16.dp.toPx())
                             // Curve back up
                             cubicTo(headX + 5.dp.toPx(), bodyBaseY, 
                                     headX - 20.dp.toPx(), bodyBaseY - 20.dp.toPx(),
                                     headX - 26.dp.toPx(), bodyBaseY - 40.dp.toPx())
                             close()
                         }
                         drawPath(sashPath, Color(0xFFD32F2F)) // Peru Red
                         
                         // Sleeves (Little white caps on shoulders)
                         drawArc(Color.White, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(headX - 44.dp.toPx(), bodyBaseY - 30.dp.toPx()), size = Size(14.dp.toPx(), 20.dp.toPx()))
                         drawArc(Color.White, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(headX + 30.dp.toPx(), bodyBaseY - 30.dp.toPx()), size = Size(14.dp.toPx(), 20.dp.toPx()))
                    }
                    else -> {}
                }

                // 4. HEAD
                val headTipY = bodyBaseY - 100.dp.toPx() + headBob
                drawOval(furColor, topLeft = Offset(headX - 22.dp.toPx(), headTipY), size = Size(44.dp.toPx(), 45.dp.toPx()))
                
                // Snout (Protruding)
                drawOval(Color(0xFFFDE68A), topLeft = Offset(headX - 18.dp.toPx(), headTipY + 25.dp.toPx() + chewY), size = Size(36.dp.toPx(), 25.dp.toPx()))
                
                // Nose
                drawOval(Color(0xFF92400E), topLeft = Offset(headX - 6.dp.toPx(), headTipY + 32.dp.toPx() + chewY), size = Size(12.dp.toPx(), 8.dp.toPx()))
                // Mouth Line
                drawLine(Color(0xFF92400E), start = Offset(headX, headTipY + 36.dp.toPx() + chewY), end = Offset(headX, headTipY + 42.dp.toPx() + chewY), strokeWidth = 2.dp.toPx())
                
                // EARS
                val earY = headTipY + 5.dp.toPx()
                val leftEarPath = Path().apply {
                    moveTo(headX - 15.dp.toPx(), earY)
                    quadraticTo(headX - 25.dp.toPx(), earY - 15.dp.toPx(), headX - 20.dp.toPx(), earY - 35.dp.toPx())
                    quadraticTo(headX - 10.dp.toPx(), earY - 15.dp.toPx(), headX - 5.dp.toPx(), earY)
                    close()
                }
                drawPath(leftEarPath, furColor)
                val rightEarPath = Path().apply {
                    moveTo(headX + 15.dp.toPx(), earY)
                    quadraticTo(headX + 25.dp.toPx(), earY - 15.dp.toPx(), headX + 20.dp.toPx(), earY - 35.dp.toPx())
                    quadraticTo(headX + 10.dp.toPx(), earY - 15.dp.toPx(), headX + 5.dp.toPx(), earY)
                    close()
                }
                drawPath(rightEarPath, furColor)
                
                // 5. FACE ELEMENTS
                // Eyes
                if (pose == BrishPose.FOCUS) {
                    // Cool Sunglasses (Black Aviators)
                    drawOval(Color.Black, topLeft = Offset(headX - 22.dp.toPx(), headTipY + 15.dp.toPx()), size = Size(20.dp.toPx(), 14.dp.toPx()))
                    drawOval(Color.Black, topLeft = Offset(headX + 2.dp.toPx(), headTipY + 15.dp.toPx()), size = Size(20.dp.toPx(), 14.dp.toPx()))
                    drawLine(Color.Black, start = Offset(headX - 2.dp.toPx(), headTipY + 18.dp.toPx()), end = Offset(headX + 2.dp.toPx(), headTipY + 18.dp.toPx()), strokeWidth = 2.dp.toPx())
                    // Glint
                    drawLine(Color.White, start = Offset(headX - 18.dp.toPx(), headTipY + 18.dp.toPx()), end = Offset(headX - 12.dp.toPx(), headTipY + 22.dp.toPx()), strokeWidth = 2.dp.toPx())
                } else {
                    // Regular Eyes
                    drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(headX - 15.dp.toPx(), headTipY + 18.dp.toPx()))
                    drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(headX + 15.dp.toPx(), headTipY + 18.dp.toPx()))
                    drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(headX - 15.dp.toPx(), headTipY + 18.dp.toPx()))
                    drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(headX + 15.dp.toPx(), headTipY + 18.dp.toPx()))
                    // Eyelashes/Sassy
                    drawLine(Color.Black, start = Offset(headX - 22.dp.toPx(), headTipY + 15.dp.toPx()), end = Offset(headX - 28.dp.toPx(), headTipY + 12.dp.toPx()), strokeWidth = 2.dp.toPx())
                    drawLine(Color.Black, start = Offset(headX + 22.dp.toPx(), headTipY + 15.dp.toPx()), end = Offset(headX + 28.dp.toPx(), headTipY + 12.dp.toPx()), strokeWidth = 2.dp.toPx())
                }
                
                // 6. HEAD GEAR (Chullo)
                 if (pose == BrishPose.DEFAULT || pose == BrishPose.FINANCE) {
                     // Raised hat base so it doesn't cover eyes
                     val hatY = headTipY - 12.dp.toPx() 
                     val chulloColor = Color(0xFFEF4444) // Red
                     val chulloPath = Path().apply {
                         moveTo(headX - 22.dp.toPx(), hatY + 10.dp.toPx()) 
                         quadraticTo(headX, hatY - 20.dp.toPx(), headX + 22.dp.toPx(), hatY + 10.dp.toPx()) 
                         // Ear flaps (shorter)
                         lineTo(headX + 24.dp.toPx(), hatY + 30.dp.toPx())
                         lineTo(headX + 15.dp.toPx(), hatY + 25.dp.toPx())
                         lineTo(headX - 15.dp.toPx(), hatY + 25.dp.toPx())
                         lineTo(headX - 24.dp.toPx(), hatY + 30.dp.toPx())
                         close()
                     }
                     drawPath(chulloPath, chulloColor)
                     // Patterns
                     drawLine(Color(0xFFF59E0B), start = Offset(headX - 15.dp.toPx(), hatY + 5.dp.toPx()), end = Offset(headX + 15.dp.toPx(), hatY + 5.dp.toPx()), strokeWidth = 3.dp.toPx())
                     // Pompom
                     drawCircle(Color(0xFF3B82F6), radius = 5.dp.toPx(), center = Offset(headX, hatY - 15.dp.toPx()))
                     // Tassels
                     drawLine(chulloColor, start = Offset(headX - 24.dp.toPx(), hatY + 30.dp.toPx()), end = Offset(headX - 28.dp.toPx(), hatY + 45.dp.toPx()), strokeWidth = 2.dp.toPx())
                     drawLine(chulloColor, start = Offset(headX + 24.dp.toPx(), hatY + 30.dp.toPx()), end = Offset(headX + 28.dp.toPx(), hatY + 45.dp.toPx()), strokeWidth = 2.dp.toPx())
                 }

                 // 7. PROPS
                 when(pose) {
                     BrishPose.NOTES -> {
                         // Charango (Small Guitar)
                         val charangoColor = Color(0xFFD4B483)
                         val cX = headX + 30.dp.toPx()
                         val cY = bodyBaseY - 10.dp.toPx()
                         // Rotation
                         rotate(-30f, pivot = Offset(cX, cY)) {
                             // Body
                             drawOval(charangoColor, topLeft = Offset(cX - 15.dp.toPx(), cY), size = Size(30.dp.toPx(), 40.dp.toPx()))
                             drawCircle(Color.Black, radius = 5.dp.toPx(), center = Offset(cX, cY + 20.dp.toPx())) // Hole
                             // Neck
                             drawRect(Color(0xFF5D4037), topLeft = Offset(cX - 4.dp.toPx(), cY - 30.dp.toPx()), size = Size(8.dp.toPx(), 30.dp.toPx()))
                             // Headstock
                             drawRect(charangoColor, topLeft = Offset(cX - 6.dp.toPx(), cY - 40.dp.toPx()), size = Size(12.dp.toPx(), 10.dp.toPx()))
                         }
                         // Hoof playing it
                         drawCircle(furColor, radius = 6.dp.toPx(), center = Offset(headX + 15.dp.toPx(), bodyBaseY))
                     }
                     BrishPose.FINANCE -> {
                         // Sol Coin
                         drawCircle(Color(0xFFFFD700), radius = 12.dp.toPx(), center = Offset(headX + 25.dp.toPx(), bodyBaseY))
                         drawCircle(Color(0xFFF59E0B), radius = 10.dp.toPx(), center = Offset(headX + 25.dp.toPx(), bodyBaseY), style = Stroke(1.dp.toPx()))
                         // "S/." text replacement (Simple lines)
                         drawLine(Color(0xFFF59E0B), start = Offset(headX + 25.dp.toPx(), bodyBaseY - 5.dp.toPx()), end = Offset(headX + 25.dp.toPx(), bodyBaseY + 5.dp.toPx()), strokeWidth = 2.dp.toPx())
                         drawLine(Color(0xFFF59E0B), start = Offset(headX + 20.dp.toPx(), bodyBaseY - 5.dp.toPx()), end = Offset(headX + 30.dp.toPx(), bodyBaseY + 5.dp.toPx()), strokeWidth = 2.dp.toPx())
                     }
                     BrishPose.HABITS -> {
                         // Sweatband
                          drawRect(Color(0xFF3B82F6), topLeft = Offset(headX - 22.dp.toPx(), headTipY + 5.dp.toPx()), size = Size(44.dp.toPx(), 6.dp.toPx()))
                     }
                     else -> {}
                 }
            }

            MascotType.POPPIN -> {
                // --- POPPIN: THE TOXIC COACH (Penguin) ---
                // Vibe: Stern, Professional, Athletic
                val footColor = Color(0xFFF59E0B)
                val accessoryRed = Color(0xFFEF4444)
                
                // Animation Logic
                val whistleScale = if(pose == BrishPose.DEFAULT) poppinWhistle else 0f
                val tickRotation = if(pose == BrishPose.HABITS) poppinTicking else 0f
                
                // --- FEET ---
                val feetOffset = if (jumpAnim.value < 0) jumpAnim.value * 0.5f else 0f
                drawOval(color = footColor, topLeft = Offset(centerX - 35.dp.toPx(), 90.dp.toPx() + bodyOffset - feetOffset + 5.dp.toPx()), size = Size(30.dp.toPx(), 20.dp.toPx()))
                drawOval(color = footColor, topLeft = Offset(centerX + 5.dp.toPx(), 90.dp.toPx() + bodyOffset - feetOffset + 5.dp.toPx()), size = Size(30.dp.toPx(), 20.dp.toPx()))

                // --- BODY ---
                drawOval(color = mainColor, topLeft = Offset(centerX - 50.dp.toPx(), 20.dp.toPx() + bodyOffset), size = Size(100.dp.toPx(), 90.dp.toPx()))
                // --- BELLY ---
                drawOval(color = secondaryColor, topLeft = Offset(centerX - 35.dp.toPx(), 40.dp.toPx() + bodyOffset), size = Size(70.dp.toPx(), 60.dp.toPx()))
                // --- HEAD ---
                drawCircle(color = mainColor, radius = 35.dp.toPx(), center = Offset(centerX, 25.dp.toPx() + bodyOffset))
                
                // --- OUTFITS & ACCESSORIES (Layer 1: Headwear) ---
                when(pose) {
                    BrishPose.HABITS -> {
                        // Coach Headband
                        drawArc(accessoryRed, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(centerX - 36.dp.toPx(), -10.dp.toPx() + bodyOffset), size = Size(72.dp.toPx(), 60.dp.toPx()), style = Stroke(width = 8.dp.toPx()))
                    }
                    BrishPose.FOCUS -> {
                        // Drill Sergeant Hat (Simplified)
                        val hatColor = Color(0xFF111827) // Very dark
                        drawOval(hatColor, topLeft = Offset(centerX - 45.dp.toPx(), -5.dp.toPx() + bodyOffset), size = Size(90.dp.toPx(), 20.dp.toPx())) // Brim
                        drawRect(hatColor, topLeft = Offset(centerX - 25.dp.toPx(), -25.dp.toPx() + bodyOffset), size = Size(50.dp.toPx(), 20.dp.toPx())) // Top
                        drawOval(Color(0xFFF59E0B), topLeft = Offset(centerX - 5.dp.toPx(), -20.dp.toPx() + bodyOffset), size = Size(10.dp.toPx(), 10.dp.toPx())) // Badge
                    }
                    BrishPose.DEFAULT -> {
                        // Classic Bandana
                         val bandanaPath = Path().apply {
                            moveTo(centerX - 36.dp.toPx(), 5.dp.toPx() + bodyOffset)
                            quadraticTo(centerX, -10.dp.toPx() + bodyOffset, centerX + 36.dp.toPx(), 5.dp.toPx() + bodyOffset)
                            lineTo(centerX + 36.dp.toPx(), 20.dp.toPx() + bodyOffset)
                            quadraticTo(centerX, 10.dp.toPx() + bodyOffset, centerX - 36.dp.toPx(), 20.dp.toPx() + bodyOffset)
                            close()
                        }
                        drawPath(path = bandanaPath, color = accessoryRed)
                    }
                    else -> {} // Naked head (Professional)
                }

                // --- FACE ---
                // Eyes
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(centerX - 12.dp.toPx(), 25.dp.toPx() + bodyOffset))
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(centerX + 12.dp.toPx(), 25.dp.toPx() + bodyOffset))
                // Pupils
                drawCircle(color = Color.Black, radius = 4.dp.toPx(), center = Offset(centerX - 12.dp.toPx(), 25.dp.toPx() + bodyOffset))
                // Wink/Blink (Intense stare for Focus)
                if (pose == BrishPose.FOCUS) {
                     drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = Offset(centerX - 12.dp.toPx(), 25.dp.toPx() + bodyOffset)) // Small pupils
                     drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = Offset(centerX + 12.dp.toPx(), 25.dp.toPx() + bodyOffset))
                } else if (pose == BrishPose.CHAT) {
                     drawOval(color = Color.Black, topLeft = Offset(centerX + 8.dp.toPx(), 23.dp.toPx() + bodyOffset), size = Size(8.dp.toPx(), 4.dp.toPx() ))
                } else {
                     drawOval(color = Color.Black, topLeft = Offset(centerX + 8.dp.toPx(), 23.dp.toPx() + bodyOffset), size = Size(8.dp.toPx(), 4.dp.toPx() * eyeScale))
                }
                
                // Eyebrows (Angry/Stern)
                val browY = if(pose == BrishPose.FOCUS) 22.dp.toPx() else 18.dp.toPx()
                drawLine(color = Color.Black, start = Offset(centerX - 20.dp.toPx(), browY + bodyOffset), end = Offset(centerX - 5.dp.toPx(), browY + 4.dp.toPx() + bodyOffset), strokeWidth = 3.dp.toPx())
                drawLine(color = Color.Black, start = Offset(centerX + 5.dp.toPx(), browY + 4.dp.toPx() + bodyOffset), end = Offset(centerX + 20.dp.toPx(), browY + bodyOffset), strokeWidth = 3.dp.toPx())
                
                // Beak
                val beakOffset = if (pose == BrishPose.DEFAULT && handWaveAnim.value == 0f) beakOpen else 0f
                val beakPath = Path().apply {
                    moveTo(centerX - 8.dp.toPx(), 32.dp.toPx() + bodyOffset)
                    lineTo(centerX + 8.dp.toPx(), 32.dp.toPx() + bodyOffset)
                    lineTo(centerX, 40.dp.toPx() + bodyOffset + beakOffset)
                    close()
                }
                drawPath(path = beakPath, color = Color(0xFFF59E0B))

                // --- OUTFITS & ACCESSORIES (Layer 2: Hands/Body) ---
                when(pose) {
                    BrishPose.DEFAULT -> {
                        // Whistle around neck
                        // String
                        drawLine(Color.Gray, start = Offset(centerX - 10.dp.toPx(), 40.dp.toPx() + bodyOffset), end = Offset(centerX, 55.dp.toPx() + bodyOffset), strokeWidth = 2.dp.toPx())
                        drawLine(Color.Gray, start = Offset(centerX + 10.dp.toPx(), 40.dp.toPx() + bodyOffset), end = Offset(centerX, 55.dp.toPx() + bodyOffset), strokeWidth = 2.dp.toPx())
                        // Whistle Body
                        val whistleY = 55.dp.toPx() + bodyOffset
                        drawRect(accessoryRed, topLeft = Offset(centerX - 5.dp.toPx(), whistleY), size = Size(10.dp.toPx(), 12.dp.toPx()))
                        drawCircle(accessoryRed, radius = 6.dp.toPx(), center = Offset(centerX, whistleY + 12.dp.toPx()))
                        // Blowing animation (Air lines)
                        if (whistleScale > 5f) {
                             drawLine(Color.Black, start = Offset(centerX - 10.dp.toPx(), whistleY + 5.dp.toPx()), end = Offset(centerX - 20.dp.toPx(), whistleY), strokeWidth = 2.dp.toPx())
                             drawLine(Color.Black, start = Offset(centerX + 10.dp.toPx(), whistleY + 5.dp.toPx()), end = Offset(centerX + 20.dp.toPx(), whistleY), strokeWidth = 2.dp.toPx())
                        }
                    }
                    
                    BrishPose.HABITS -> {
                        // Stopwatch in Hand
                        val stopwatchColor = Color(0xFFE5E7EB)
                        val handX = centerX + 40.dp.toPx()
                        val handY = 60.dp.toPx() + bodyOffset
                        
                        // Arm
                        drawOval(mainColor, topLeft = Offset(centerX + 25.dp.toPx(), 40.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 30.dp.toPx()))
                        
                        // Stopwatch
                        drawCircle(stopwatchColor, radius = 10.dp.toPx(), center = Offset(handX, handY))
                        drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(handX, handY))
                        // Hand (Tick) - Rotates
                         rotate(degrees = tickRotation, pivot = Offset(handX, handY)) {
                             drawLine(Color.Red, start = Offset(handX, handY), end = Offset(handX, handY - 8.dp.toPx()), strokeWidth = 2.dp.toPx())
                         }
                        // Button on top
                        drawRect(stopwatchColor, topLeft = Offset(handX - 2.dp.toPx(), handY - 13.dp.toPx()), size = Size(4.dp.toPx(), 3.dp.toPx()))
                    }
                    
                    BrishPose.PLANNER -> {
                         // Clipboard
                         val boardColor = Color(0xFF92400E)
                         val paperColor = Color.White
                         
                         // Holding it against chest
                         // Board
                         drawRect(boardColor, topLeft = Offset(centerX - 25.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(30.dp.toPx(), 40.dp.toPx()))
                         // Paper
                         drawRect(paperColor, topLeft = Offset(centerX - 22.dp.toPx(), 55.dp.toPx() + bodyOffset), size = Size(24.dp.toPx(), 32.dp.toPx()))
                         // Clip
                         drawRect(Color.Gray, topLeft = Offset(centerX - 18.dp.toPx(), 48.dp.toPx() + bodyOffset), size = Size(16.dp.toPx(), 6.dp.toPx()))
                         // Pen in other hand
                         drawLine(Color.Black, start = Offset(centerX + 30.dp.toPx(), 60.dp.toPx() + bodyOffset), end = Offset(centerX + 20.dp.toPx(), 70.dp.toPx() + bodyOffset), strokeWidth = 3.dp.toPx())
                    }
                    
                    BrishPose.FINANCE -> {
                         // Calculator
                         val calcColor = Color(0xFF374151)
                         // Holding
                         drawRoundRect(calcColor, topLeft = Offset(centerX - 20.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(40.dp.toPx(), 50.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
                         // Screen
                         drawRect(Color(0xFFD1D5DB), topLeft = Offset(centerX - 15.dp.toPx(), 55.dp.toPx() + bodyOffset), size = Size(30.dp.toPx(), 10.dp.toPx()))
                         // Buttons (Abstract)
                         drawCircle(Color.Red, radius = 3.dp.toPx(), center = Offset(centerX - 10.dp.toPx(), 70.dp.toPx() + bodyOffset))
                         drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(centerX, 70.dp.toPx() + bodyOffset))
                         drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(centerX + 10.dp.toPx(), 70.dp.toPx() + bodyOffset))
                    }
                    
                    BrishPose.NOTES -> {
                         // Teacher Pointer
                         val stickColor = Color(0xFFD4B483)
                         drawLine(stickColor, start = Offset(centerX + 30.dp.toPx(), 50.dp.toPx() + bodyOffset), end = Offset(centerX + 60.dp.toPx(), 20.dp.toPx() + bodyOffset), strokeWidth = 3.dp.toPx())
                         drawCircle(Color.Red, radius = 3.dp.toPx(), center = Offset(centerX + 60.dp.toPx(), 20.dp.toPx() + bodyOffset))
                    }
                    else -> {
                        // Arms crossed (Stern)
                        drawLine(mainColor, start = Offset(centerX - 20.dp.toPx(), 60.dp.toPx() + bodyOffset), end = Offset(centerX + 20.dp.toPx(), 60.dp.toPx() + bodyOffset), strokeWidth = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
            }

            MascotType.PACO -> {
                // --- PACO: KAWAII ORANGE CAT (Chibi Style) ---
                val pacoOrange = Color(0xFFFB923C)
                val pacoLight = Color(0xFFFDBA74) // Belly/Muzzle
                val pacoDark = Color(0xFFEA580C) // Stripes
                
                // Animation Offsets
                val bounceY = if(pose == BrishPose.DEFAULT) breatheAnim else 0f
                val headBob = if(pose == BrishPose.FOCUS) headBobAnim else 0f
                
                // Base Center
                val pacoCx = centerX
                val pacoCy = 90.dp.toPx() + bodyOffset
                
                // 1. TAIL (Wagging)
                val tailX = if (pose == BrishPose.DEFAULT) eyePanAnim * 2 else 0f
                val tailPath = Path().apply {
                    moveTo(pacoCx + 30.dp.toPx(), pacoCy + 20.dp.toPx())
                    quadraticTo(pacoCx + 50.dp.toPx() + tailX, pacoCy - 10.dp.toPx(), pacoCx + 40.dp.toPx() + tailX, pacoCy - 20.dp.toPx())
                }
                drawPath(tailPath, color = pacoOrange, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                
                // 2. BODY (Round Ball)
                drawCircle(color = pacoOrange, radius = 35.dp.toPx(), center = Offset(pacoCx, pacoCy))
                drawCircle(color = pacoLight, radius = 20.dp.toPx(), center = Offset(pacoCx, pacoCy + 10.dp.toPx())) // Belly

                // 3. HEAD (Big & Round)
                val headCy = 50.dp.toPx() + bodyOffset + headBob
                val headRadius = 32.dp.toPx()
                
                // Ears (Rounded Triangles)
                val earPath = Path().apply {
                    moveTo(pacoCx - 25.dp.toPx(), headCy - 20.dp.toPx())
                    quadraticTo(pacoCx - 35.dp.toPx(), headCy - 45.dp.toPx(), pacoCx - 15.dp.toPx(), headCy - 30.dp.toPx())
                    
                    moveTo(pacoCx + 25.dp.toPx(), headCy - 20.dp.toPx())
                    quadraticTo(pacoCx + 35.dp.toPx(), headCy - 45.dp.toPx(), pacoCx + 15.dp.toPx(), headCy - 30.dp.toPx())
                }
                drawPath(earPath, pacoOrange)
                
                // Head Shape
                drawCircle(color = pacoOrange, radius = headRadius, center = Offset(pacoCx, headCy))
                
                // Stripes (Top)
                drawArc(color = pacoDark, startAngle = -110f, sweepAngle = 40f, useCenter = false, topLeft = Offset(pacoCx - 32.dp.toPx(), headCy - 32.dp.toPx()), size = Size(64.dp.toPx(), 64.dp.toPx()), style = Stroke(width = 4.dp.toPx()))

                // 4. FACE
                // Muzzle
                drawOval(pacoLight, topLeft = Offset(pacoCx - 12.dp.toPx(), headCy + 5.dp.toPx()), size = Size(24.dp.toPx(), 14.dp.toPx()))
                // Nose
                drawCircle(Color(0xFFEA580C), radius = 2.dp.toPx(), center = Offset(pacoCx, headCy + 8.dp.toPx()))
                
                // Eyes (Big Kawaii)
                val eyeY = headCy - 5.dp.toPx()
                val eyeOff = if(pose == BrishPose.PLANNER) eyePanAnim else 0f
                
                drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(pacoCx - 14.dp.toPx() + eyeOff, eyeY))
                drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(pacoCx + 14.dp.toPx() + eyeOff, eyeY))
                // Pupils
                drawCircle(Color.Black, radius = 4.dp.toPx(), center = Offset(pacoCx - 14.dp.toPx() + eyeOff, eyeY))
                drawCircle(Color.Black, radius = 4.dp.toPx(), center = Offset(pacoCx + 14.dp.toPx() + eyeOff, eyeY))
                // Sparkle
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(pacoCx - 12.dp.toPx() + eyeOff, eyeY - 2.dp.toPx()))
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(pacoCx + 16.dp.toPx() + eyeOff, eyeY - 2.dp.toPx()))

                // 5. PAWS & PROPS
                val pawColor = pacoLight
                // ALWAYS DRAW BASE PAWS (Standard Feet)
                drawCircle(pawColor, radius = 8.dp.toPx(), center = Offset(pacoCx - 15.dp.toPx(), 115.dp.toPx() + bodyOffset)) // Left Foot
                drawCircle(pawColor, radius = 8.dp.toPx(), center = Offset(pacoCx + 15.dp.toPx(), 115.dp.toPx() + bodyOffset)) // Right Foot

                when(pose) {
                    BrishPose.PLANNER -> {
                         // Holding Pencil
                         val pencilAngle = 45f
                         rotate(pencilAngle, pivot = Offset(pacoCx + 25.dp.toPx(), 80.dp.toPx())) {
                             drawRect(Color(0xFFFFC107), topLeft = Offset(pacoCx + 20.dp.toPx(), 70.dp.toPx()), size = Size(10.dp.toPx(), 30.dp.toPx()))
                             drawRect(Color(0xFFF87171), topLeft = Offset(pacoCx + 20.dp.toPx(), 70.dp.toPx()), size = Size(10.dp.toPx(), 5.dp.toPx())) // Eraser
                         }
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx + 25.dp.toPx(), 85.dp.toPx() + bodyOffset))
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx - 15.dp.toPx(), 85.dp.toPx() + bodyOffset))
                    }
                    BrishPose.HABITS -> {
                        // Yoga Pose (Paws up)
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx - 25.dp.toPx(), 70.dp.toPx() + bodyOffset))
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx + 25.dp.toPx(), 70.dp.toPx() + bodyOffset))
                    }
                    BrishPose.FOCUS -> {
                        // Headphones
                         val hpColor = Color(0xFF3B82F6)
                         drawArc(hpColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(pacoCx - 34.dp.toPx(), headCy - 20.dp.toPx()), size = Size(68.dp.toPx(), 60.dp.toPx()), style = Stroke(6.dp.toPx()))
                         drawOval(hpColor, topLeft = Offset(pacoCx - 36.dp.toPx(), headCy - 5.dp.toPx()), size = Size(10.dp.toPx(), 20.dp.toPx()))
                         drawOval(hpColor, topLeft = Offset(pacoCx + 26.dp.toPx(), headCy - 5.dp.toPx()), size = Size(10.dp.toPx(), 20.dp.toPx()))
                         
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx - 15.dp.toPx(), 90.dp.toPx() + bodyOffset))
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx + 15.dp.toPx(), 90.dp.toPx() + bodyOffset))
                    }
                     else -> {
                         // Default Hands (Nubbins on chest)
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx - 10.dp.toPx(), 90.dp.toPx() + bodyOffset))
                         drawCircle(pawColor, radius = 6.dp.toPx(), center = Offset(pacoCx + 10.dp.toPx(), 90.dp.toPx() + bodyOffset))
                     }
                }
            }

            MascotType.KITTY -> {
                // --- KITTY V4: LA PATRONA (Redesign) ---
                val kittyMain = Color(0xFF9CA3AF) // Cool Gray
                val kittyLight = Color(0xFFE5E7EB) // White/Light Gray
                val kittyPink = Color(0xFFF472B6) // Signature Pink
                val kittyEyes = Color(0xFF10B981) // Teal Eyes

                val kittyCx = centerX
                val bodyCy = 75.dp.toPx() + bodyOffset

                // Animation Offsets
                val headBob = if(pose == BrishPose.FOCUS) headBobAnim else 0f
                val eyeX = if(pose == BrishPose.PLANNER) eyePanAnim else 0f

                // 1. FEET (ELEGANT SIT)
                drawOval(kittyLight, topLeft = Offset(kittyCx - 35.dp.toPx(), bodyCy + 35.dp.toPx()), size = Size(25.dp.toPx(), 20.dp.toPx()))
                drawOval(kittyLight, topLeft = Offset(kittyCx + 10.dp.toPx(), bodyCy + 35.dp.toPx()), size = Size(25.dp.toPx(), 20.dp.toPx()))

                // 2. TAIL (Sassy Curve with WAG)
                val tailPath = Path().apply {
                    // Tip moves left/right based on wag
                    val wagX = tailWagAnim * 2
                    moveTo(kittyCx + 40.dp.toPx(), bodyCy + 20.dp.toPx())
                    quadraticTo(kittyCx + 65.dp.toPx() + wagX, bodyCy - 20.dp.toPx(), kittyCx + 40.dp.toPx() + wagX, bodyCy - 30.dp.toPx())
                }
                drawPath(tailPath, color = kittyMain, style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round))

                // 3. BODY (Sleek Shape)
                drawOval(kittyMain, topLeft = Offset(kittyCx - 40.dp.toPx(), bodyCy - 25.dp.toPx()), size = Size(80.dp.toPx(), 85.dp.toPx()))
                drawOval(kittyLight, topLeft = Offset(kittyCx - 25.dp.toPx(), bodyCy - 5.dp.toPx()), size = Size(50.dp.toPx(), 55.dp.toPx())) // Belly

                // 4. HEAD (Cat Shape)
                val headCy = bodyCy - 35.dp.toPx() + headBob
                
                // Ears (Pointy & Alert with TWITCH)
                // Left Ear (Twitches)
                rotate(degrees = -earTwitchAnim, pivot = Offset(kittyCx - 30.dp.toPx(), headCy - 10.dp.toPx())) {
                    val leftEarPath = Path().apply {
                        moveTo(kittyCx - 30.dp.toPx(), headCy - 10.dp.toPx())
                        lineTo(kittyCx - 45.dp.toPx(), headCy - 50.dp.toPx())
                        lineTo(kittyCx - 10.dp.toPx(), headCy - 25.dp.toPx())
                    }
                    drawPath(leftEarPath, kittyMain)
                    val leftInnerEar = Path().apply {
                        moveTo(kittyCx - 30.dp.toPx(), headCy - 15.dp.toPx())
                        lineTo(kittyCx - 40.dp.toPx(), headCy - 40.dp.toPx())
                        lineTo(kittyCx - 15.dp.toPx(), headCy - 25.dp.toPx())
                    }
                    drawPath(leftInnerEar, Color(0xFFFFC1E3))
                }

                // Right Ear (Static or slight counter-movement)
                val rightEarPath = Path().apply {
                    moveTo(kittyCx + 30.dp.toPx(), headCy - 10.dp.toPx())
                    lineTo(kittyCx + 45.dp.toPx(), headCy - 50.dp.toPx()) 
                    lineTo(kittyCx + 10.dp.toPx(), headCy - 25.dp.toPx())
                }
                drawPath(rightEarPath, kittyMain)
                val rightInnerEar = Path().apply {
                     moveTo(kittyCx + 30.dp.toPx(), headCy - 15.dp.toPx())
                    lineTo(kittyCx + 40.dp.toPx(), headCy - 40.dp.toPx())
                    lineTo(kittyCx + 15.dp.toPx(), headCy - 25.dp.toPx())
                }
                 drawPath(rightInnerEar, Color(0xFFFFC1E3))

                // Head Base
                drawOval(kittyMain, topLeft = Offset(kittyCx - 38.dp.toPx(), headCy - 35.dp.toPx()), size = Size(76.dp.toPx(), 65.dp.toPx()))

                // 5. OUTFITS & ACCESSORIES (Layer 1 - Head)
                when(pose) {
                    BrishPose.HABITS -> {
                        // Yoga Headband
                        drawArc(kittyPink, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(kittyCx - 36.dp.toPx(), headCy - 40.dp.toPx()), size = Size(72.dp.toPx(), 60.dp.toPx()), style = Stroke(width = 6.dp.toPx()))
                    }
                    else -> {}
                }

                // 6. FACE FEATURES
                // Eyes (Teal Cat Eyes - Redesigned: Bored but Cute)
                if (pose == BrishPose.FOCUS) {
                    // Closed Eyes (Concentration)
                     drawArc(Color.Black, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(kittyCx - 22.dp.toPx(), headCy - 5.dp.toPx()), size = Size(16.dp.toPx(), 10.dp.toPx()), style = Stroke(2.dp.toPx()))
                     drawArc(Color.Black, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(kittyCx + 6.dp.toPx(), headCy - 5.dp.toPx()), size = Size(16.dp.toPx(), 10.dp.toPx()), style = Stroke(2.dp.toPx()))
                } else {
                    // Open Eyes (Bored/Half-lidded + Cute)
                    val eyeSize = 22.dp.toPx()
                    val eyeHeight = 18.dp.toPx()
                    
                    // Eye Whites
                    drawOval(Color.White, topLeft = Offset(kittyCx - 26.dp.toPx() + eyeX, headCy - 8.dp.toPx()), size = Size(eyeSize, eyeHeight))
                    drawOval(Color.White, topLeft = Offset(kittyCx + 4.dp.toPx() + eyeX, headCy - 8.dp.toPx()), size = Size(eyeSize, eyeHeight))
                    
                    // Iris (Teal - Bigger/Softer)
                    drawCircle(kittyEyes, radius = 7.dp.toPx(), center = Offset(kittyCx - 15.dp.toPx() + eyeX, headCy + 1.dp.toPx()))
                    drawCircle(kittyEyes, radius = 7.dp.toPx(), center = Offset(kittyCx + 15.dp.toPx() + eyeX, headCy + 1.dp.toPx()))
                    
                    // Pupil (Rounder = Cuter, not slit)
                    drawCircle(Color.Black, radius = 3.5.dp.toPx(), center = Offset(kittyCx - 15.dp.toPx() + eyeX, headCy + 1.dp.toPx()))
                    drawCircle(Color.Black, radius = 3.5.dp.toPx(), center = Offset(kittyCx + 15.dp.toPx() + eyeX, headCy + 1.dp.toPx()))
                    
                    // Highlight (Sparkle)
                    drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(kittyCx - 12.dp.toPx() + eyeX, headCy - 2.dp.toPx()))
                    drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(kittyCx + 18.dp.toPx() + eyeX, headCy - 2.dp.toPx()))

                    // Eyelids (Half-lidded for "Bored/Unimpressed" look)
                    // Draw a rect over the top half of the eye to "cut" it
                    val eyelidColor = kittyMain // Match skin
                    drawRect(eyelidColor, topLeft = Offset(kittyCx - 28.dp.toPx() + eyeX, headCy - 12.dp.toPx()), size = Size(26.dp.toPx(), 11.dp.toPx()))
                    drawRect(eyelidColor, topLeft = Offset(kittyCx + 2.dp.toPx() + eyeX, headCy - 12.dp.toPx()), size = Size(26.dp.toPx(), 11.dp.toPx()))
                    
                    // Eyelid Line (Definition)
                    drawLine(Color.Black, start = Offset(kittyCx - 26.dp.toPx() + eyeX, headCy - 1.dp.toPx()), end = Offset(kittyCx - 4.dp.toPx() + eyeX, headCy - 1.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                    drawLine(Color.Black, start = Offset(kittyCx + 4.dp.toPx() + eyeX, headCy - 1.dp.toPx()), end = Offset(kittyCx + 26.dp.toPx() + eyeX, headCy - 1.dp.toPx()), strokeWidth = 1.5.dp.toPx())

                    // Eyelashes (Pretty/"Bonito")
                    // Left
                    drawLine(Color.Black, start = Offset(kittyCx - 26.dp.toPx() + eyeX, headCy - 1.dp.toPx()), end = Offset(kittyCx - 32.dp.toPx() + eyeX, headCy - 6.dp.toPx()), strokeWidth = 2.dp.toPx())
                    // Right
                    drawLine(Color.Black, start = Offset(kittyCx + 26.dp.toPx() + eyeX, headCy - 1.dp.toPx()), end = Offset(kittyCx + 32.dp.toPx() + eyeX, headCy - 6.dp.toPx()), strokeWidth = 2.dp.toPx())
                    
                    // Glasses (Finance / Planner)
                    if (pose == BrishPose.PLANNER || pose == BrishPose.FINANCE) {
                         drawOval(Color.Black, topLeft = Offset(kittyCx - 28.dp.toPx() + eyeX, headCy - 8.dp.toPx()), size = Size(26.dp.toPx(), 20.dp.toPx()), style = Stroke(2.dp.toPx()))
                         drawOval(Color.Black, topLeft = Offset(kittyCx + 2.dp.toPx() + eyeX, headCy - 8.dp.toPx()), size = Size(26.dp.toPx(), 20.dp.toPx()), style = Stroke(2.dp.toPx()))
                         drawLine(Color.Black, start = Offset(kittyCx - 2.dp.toPx() + eyeX, headCy + 2.dp.toPx()), end = Offset(kittyCx + 2.dp.toPx() + eyeX, headCy + 2.dp.toPx()), strokeWidth = 2.dp.toPx())
                    }
                }

                // Nose & Mouth
                drawTriangle(kittyPink, center = Offset(kittyCx, headCy + 12.dp.toPx()), size = 4.dp.toPx(), pointingDown = true)
                drawLine(Color.White, start = Offset(kittyCx, headCy + 16.dp.toPx()), end = Offset(kittyCx - 5.dp.toPx(), headCy + 22.dp.toPx()), strokeWidth = 2.dp.toPx())
                drawLine(Color.White, start = Offset(kittyCx, headCy + 16.dp.toPx()), end = Offset(kittyCx + 5.dp.toPx(), headCy + 22.dp.toPx()), strokeWidth = 2.dp.toPx())
                
                // Whiskers
                drawLine(Color.White, start = Offset(kittyCx - 15.dp.toPx(), headCy + 12.dp.toPx()), end = Offset(kittyCx - 40.dp.toPx(), headCy + 8.dp.toPx()), strokeWidth = 1.dp.toPx())
                drawLine(Color.White, start = Offset(kittyCx - 15.dp.toPx(), headCy + 16.dp.toPx()), end = Offset(kittyCx - 40.dp.toPx(), headCy + 16.dp.toPx()), strokeWidth = 1.dp.toPx())
                drawLine(Color.White, start = Offset(kittyCx + 15.dp.toPx(), headCy + 12.dp.toPx()), end = Offset(kittyCx + 40.dp.toPx(), headCy + 8.dp.toPx()), strokeWidth = 1.dp.toPx())
                drawLine(Color.White, start = Offset(kittyCx + 15.dp.toPx(), headCy + 16.dp.toPx()), end = Offset(kittyCx + 40.dp.toPx(), headCy + 16.dp.toPx()), strokeWidth = 1.dp.toPx())

                // 7. OUTFITS & PROPS (Layer 2 - Body/Hands)
                when(pose) {
                    BrishPose.DEFAULT -> {
                        // Collar with Bell
                        drawArc(kittyPink, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(kittyCx - 20.dp.toPx(), headCy + 25.dp.toPx()), size = Size(40.dp.toPx(), 20.dp.toPx()), style = Stroke(width = 4.dp.toPx()))
                        drawCircle(Color(0xFFFFD700), radius = 4.dp.toPx(), center = Offset(kittyCx, headCy + 35.dp.toPx())) // Bell
                        
                        // Neat Paws
                        drawCircle(kittyLight, radius = 7.dp.toPx(), center = Offset(kittyCx - 15.dp.toPx(), bodyCy + 15.dp.toPx()))
                        drawCircle(kittyLight, radius = 7.dp.toPx(), center = Offset(kittyCx + 15.dp.toPx(), bodyCy + 15.dp.toPx()))
                    }
                    BrishPose.PLANNER -> {
                         // Holding Book
                         drawRect(Color(0xFF4B5563), topLeft = Offset(kittyCx - 15.dp.toPx(), bodyCy), size = Size(30.dp.toPx(), 25.dp.toPx())) // Book Cover
                         drawRect(Color.White, topLeft = Offset(kittyCx - 12.dp.toPx(), bodyCy + 2.dp.toPx()), size = Size(24.dp.toPx(), 21.dp.toPx())) // Pages
                         // Paws holding it
                         drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx - 18.dp.toPx(), bodyCy + 12.dp.toPx()))
                         drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx + 18.dp.toPx(), bodyCy + 12.dp.toPx()))
                    }
                    BrishPose.FINANCE -> {
                        // Holding Golden Coin
                        drawCircle(Color(0xFFFFD700), radius = 12.dp.toPx(), center = Offset(kittyCx, bodyCy + 10.dp.toPx()))
                        drawCircle(Color(0xFFF59E0B), radius = 12.dp.toPx(), center = Offset(kittyCx, bodyCy + 10.dp.toPx()), style = Stroke(2.dp.toPx()))
                        drawLine(Color(0xFFF59E0B), start = Offset(kittyCx - 4.dp.toPx(), bodyCy + 6.dp.toPx()), end = Offset(kittyCx - 4.dp.toPx(), bodyCy + 14.dp.toPx()), strokeWidth = 2.dp.toPx()) // $ sign equivalent
                        drawLine(Color(0xFFF59E0B), start = Offset(kittyCx + 4.dp.toPx(), bodyCy + 6.dp.toPx()), end = Offset(kittyCx + 4.dp.toPx(), bodyCy + 14.dp.toPx()), strokeWidth = 2.dp.toPx())
                        
                        // Paws
                        drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx - 12.dp.toPx(), bodyCy + 10.dp.toPx()))
                        drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx + 12.dp.toPx(), bodyCy + 10.dp.toPx()))
                    }
                    BrishPose.NOTES -> {
                        // Holding Red Grading Pen
                        val penColor = Color(0xFFE11D48)
                        val angle = 30f // Tilted
                        rotate(degrees = -angle, pivot = Offset(kittyCx + 20.dp.toPx(), bodyCy + 10.dp.toPx())) {
                             drawLine(penColor, start = Offset(kittyCx + 10.dp.toPx(), bodyCy + 20.dp.toPx()), end = Offset(kittyCx + 10.dp.toPx(), bodyCy - 10.dp.toPx()), strokeWidth = 4.dp.toPx())
                             drawRect(penColor, topLeft = Offset(kittyCx + 8.dp.toPx(), bodyCy - 12.dp.toPx()), size = Size(4.dp.toPx(), 4.dp.toPx())) // Cap
                        }
                        // Paws
                        drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx + 10.dp.toPx(), bodyCy + 10.dp.toPx())) // Holding
                        drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx - 15.dp.toPx(), bodyCy + 20.dp.toPx())) // Resting
                    }
                    BrishPose.FOCUS -> {
                        // Noise Canceling Headphones (White/Pink)
                         val hpColor = Color.White
                         drawArc(hpColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(kittyCx - 38.dp.toPx(), headCy - 25.dp.toPx()), size = Size(76.dp.toPx(), 60.dp.toPx()), style = Stroke(width = 6.dp.toPx()))
                         drawOval(hpColor, topLeft = Offset(kittyCx - 40.dp.toPx(), headCy - 10.dp.toPx()), size = Size(14.dp.toPx(), 25.dp.toPx())) // Left Cup
                         drawOval(hpColor, topLeft = Offset(kittyCx + 26.dp.toPx(), headCy - 10.dp.toPx()), size = Size(14.dp.toPx(), 25.dp.toPx())) // Right Cup
                         // Pink accent
                         drawOval(kittyPink, topLeft = Offset(kittyCx - 38.dp.toPx(), headCy - 5.dp.toPx()), size = Size(4.dp.toPx(), 15.dp.toPx())) 
                         drawOval(kittyPink, topLeft = Offset(kittyCx + 34.dp.toPx(), headCy - 5.dp.toPx()), size = Size(4.dp.toPx(), 15.dp.toPx()))

                         // Paws Crossed "Zen"
                         drawOval(kittyLight, topLeft = Offset(kittyCx - 10.dp.toPx(), bodyCy + 10.dp.toPx()), size = Size(20.dp.toPx(), 10.dp.toPx()))
                    }
                    BrishPose.HABITS -> {
                        // Paws in Yoga Mudra or Stretch
                        drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx - 25.dp.toPx(), bodyCy + 5.dp.toPx()))
                        drawCircle(kittyLight, radius = 6.dp.toPx(), center = Offset(kittyCx + 25.dp.toPx(), bodyCy + 5.dp.toPx()))
                    }
                    else -> {
                        // Default Paws
                        drawCircle(kittyLight, radius = 7.dp.toPx(), center = Offset(kittyCx - 15.dp.toPx(), bodyCy + 15.dp.toPx()))
                        drawCircle(kittyLight, radius = 7.dp.toPx(), center = Offset(kittyCx + 15.dp.toPx(), bodyCy + 15.dp.toPx()))
                    }
                }
            }

            MascotType.PANDA -> {
                // --- PANDA V2: THE ZEN MASTER (Detailed & Polished) ---
                val pandaWhite = Color(0xFFFFFFFF)
                val pandaBlack = Color(0xFF1F2937) // Soft Black
                val pandaShadow = Color(0xFFF3F4F6) // For depth
                
                val pandaCx = centerX
                // Base Y Layout - Lower body to ground it
                val bodyCy = 95.dp.toPx() + bodyOffset
                val headCy = 55.dp.toPx() + bodyOffset + pandaHeadNod // Use nod animation
                
                // 1. LEGS (Stubby & Cute)
                // Back legs (sitting)
                drawOval(pandaBlack, topLeft = Offset(pandaCx - 38.dp.toPx(), bodyCy + 20.dp.toPx()), size = Size(25.dp.toPx(), 20.dp.toPx()))
                drawOval(pandaBlack, topLeft = Offset(pandaCx + 13.dp.toPx(), bodyCy + 20.dp.toPx()), size = Size(25.dp.toPx(), 20.dp.toPx()))
                // Paw Pads
                drawCircle(Color(0xFFE5E7EB), radius = 4.dp.toPx(), center = Offset(pandaCx - 25.dp.toPx(), bodyCy + 30.dp.toPx()))
                drawCircle(Color(0xFFE5E7EB), radius = 4.dp.toPx(), center = Offset(pandaCx + 25.dp.toPx(), bodyCy + 30.dp.toPx()))

                // 2. BODY (Soft & Round)
                drawOval(pandaWhite, topLeft = Offset(pandaCx - 40.dp.toPx(), bodyCy - 35.dp.toPx()), size = Size(80.dp.toPx(), 75.dp.toPx()))
                // Belly soft shadow
                drawArc(pandaShadow, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(pandaCx - 30.dp.toPx(), bodyCy), size = Size(60.dp.toPx(), 35.dp.toPx()))

                // 3. ARMS (Black Jacket Look)
                // Shoulder band
                drawArc(pandaBlack, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(pandaCx - 38.dp.toPx(), bodyCy - 40.dp.toPx()), size = Size(76.dp.toPx(), 60.dp.toPx()), style = Stroke(width = 16.dp.toPx()))
                
                // 4. HEAD (Big & Kawaii)
                // Ears
                drawCircle(pandaBlack, radius = 10.dp.toPx(), center = Offset(pandaCx - 32.dp.toPx(), headCy - 20.dp.toPx()))
                drawCircle(pandaBlack, radius = 10.dp.toPx(), center = Offset(pandaCx + 32.dp.toPx(), headCy - 20.dp.toPx()))
                
                // Face
                drawOval(pandaWhite, topLeft = Offset(pandaCx - 42.dp.toPx(), headCy - 35.dp.toPx()), size = Size(84.dp.toPx(), 70.dp.toPx()))
                
                // Eye Patches (Characteristic)
                val patchY = headCy - 2.dp.toPx()
                rotate(-15f, pivot = Offset(pandaCx - 20.dp.toPx(), patchY)) {
                    drawOval(pandaBlack, topLeft = Offset(pandaCx - 32.dp.toPx(), patchY - 8.dp.toPx()), size = Size(24.dp.toPx(), 30.dp.toPx()))
                }
                rotate(15f, pivot = Offset(pandaCx + 20.dp.toPx(), patchY)) {
                    drawOval(pandaBlack, topLeft = Offset(pandaCx + 8.dp.toPx(), patchY - 8.dp.toPx()), size = Size(24.dp.toPx(), 30.dp.toPx()))
                }
                
                // Eyes
                val eyeOff = if(pose == BrishPose.PLANNER) eyePanAnim else 0f
                if (pose == BrishPose.FOCUS) {
                    // Closed (Meditating)
                    drawLine(Color.White, start = Offset(pandaCx - 25.dp.toPx(), patchY + 5.dp.toPx()), end = Offset(pandaCx - 15.dp.toPx(), patchY + 5.dp.toPx()), strokeWidth = 2.dp.toPx())
                    drawLine(Color.White, start = Offset(pandaCx + 15.dp.toPx(), patchY + 5.dp.toPx()), end = Offset(pandaCx + 25.dp.toPx(), patchY + 5.dp.toPx()), strokeWidth = 2.dp.toPx())
                } else {
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(pandaCx - 20.dp.toPx() + eyeOff, patchY + 3.dp.toPx()))
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(pandaCx + 20.dp.toPx() + eyeOff, patchY + 3.dp.toPx()))
                }
                
                // Nose
                drawOval(pandaBlack, topLeft = Offset(pandaCx - 6.dp.toPx(), headCy + 15.dp.toPx()), size = Size(12.dp.toPx(), 8.dp.toPx()))
                
                // Mouth (Small W)
                val mouthPath = Path().apply {
                    moveTo(pandaCx - 4.dp.toPx(), headCy + 23.dp.toPx())
                    quadraticTo(pandaCx, headCy + 26.dp.toPx(), pandaCx + 4.dp.toPx(), headCy + 23.dp.toPx())
                }
                drawPath(mouthPath, pandaBlack, style = Stroke(width = 1.5.dp.toPx()))

                // 5. PROPS & POSES (Real Props = High Quality)
                when(pose) {
                    BrishPose.DEFAULT -> {
                        // Eating Bamboo
                        val bambooColor = Color(0xFF84CC16) // Lime Green
                         // Bamboo Stalk
                        drawLine(bambooColor, start = Offset(pandaCx - 15.dp.toPx(), headCy + 35.dp.toPx()), end = Offset(pandaCx + 30.dp.toPx(), bodyCy + 20.dp.toPx()), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
                        // Leaves
                        drawOval(bambooColor, topLeft = Offset(pandaCx - 20.dp.toPx(), headCy + 30.dp.toPx()), size = Size(10.dp.toPx(), 4.dp.toPx()))
                        // Hands holding it
                        drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx + 10.dp.toPx(), bodyCy))
                        drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx - 5.dp.toPx(), bodyCy - 5.dp.toPx()))
                    }
                    BrishPose.FOCUS -> {
                        // Zen Meditation (Hands on knees)
                        drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx - 35.dp.toPx(), bodyCy + 10.dp.toPx()))
                        drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx + 35.dp.toPx(), bodyCy + 10.dp.toPx()))
                    }
                    BrishPose.PLANNER -> {
                         // Reading Scroll (Master style)
                         val scrollColor = Color(0xFFFEF3C7) // Parchment
                         val woodColor = Color(0xFF78350F)
                         // Scroll Body
                         drawRect(scrollColor, topLeft = Offset(pandaCx - 25.dp.toPx(), bodyCy), size = Size(50.dp.toPx(), 30.dp.toPx()))
                         // Wood Ends
                         drawCircle(woodColor, radius = 4.dp.toPx(), center = Offset(pandaCx - 25.dp.toPx(), bodyCy + 15.dp.toPx()))
                         drawCircle(woodColor, radius = 4.dp.toPx(), center = Offset(pandaCx + 25.dp.toPx(), bodyCy + 15.dp.toPx()))
                         // Reading Glasses
                         drawOval(pandaBlack, topLeft = Offset(pandaCx - 25.dp.toPx(), headCy), size = Size(20.dp.toPx(), 12.dp.toPx()), style = Stroke(2.dp.toPx()))
                         drawOval(pandaBlack, topLeft = Offset(pandaCx + 5.dp.toPx(), headCy), size = Size(20.dp.toPx(), 12.dp.toPx()), style = Stroke(2.dp.toPx()))
                         drawLine(pandaBlack, start = Offset(pandaCx - 5.dp.toPx(), headCy + 6.dp.toPx()), end = Offset(pandaCx + 5.dp.toPx(), headCy + 6.dp.toPx()), strokeWidth = 2.dp.toPx())
                         
                         // Holding scroll
                         drawCircle(pandaBlack, radius = 6.dp.toPx(), center = Offset(pandaCx - 28.dp.toPx(), bodyCy + 15.dp.toPx()))
                         drawCircle(pandaBlack, radius = 6.dp.toPx(), center = Offset(pandaCx + 28.dp.toPx(), bodyCy + 15.dp.toPx()))
                    }
                    BrishPose.FINANCE -> {
                        // Holding Gold Ingot (Yuan Bao)
                        val goldColor = Color(0xFFFFD700)
                        val boatPath = Path().apply {
                            moveTo(pandaCx - 15.dp.toPx(), bodyCy + 10.dp.toPx())
                            quadraticTo(pandaCx, bodyCy + 25.dp.toPx(), pandaCx + 15.dp.toPx(), bodyCy + 10.dp.toPx())
                            lineTo(pandaCx + 20.dp.toPx(), bodyCy + 5.dp.toPx())
                            quadraticTo(pandaCx, bodyCy + 15.dp.toPx(), pandaCx - 20.dp.toPx(), bodyCy + 5.dp.toPx())
                            close()
                        }
                        drawPath(boatPath, goldColor)
                        drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx - 12.dp.toPx(), bodyCy + 10.dp.toPx()))
                         drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx + 12.dp.toPx(), bodyCy + 10.dp.toPx()))
                    }
                    else -> {
                        // Default Relaxation (Arms on belly)
                        rotate(-30f, pivot = Offset(pandaCx - 35.dp.toPx(), bodyCy)) {
                            drawOval(pandaBlack, topLeft = Offset(pandaCx - 45.dp.toPx(), bodyCy - 10.dp.toPx()), size = Size(18.dp.toPx(), 35.dp.toPx()))
                        }
                        rotate(30f, pivot = Offset(pandaCx + 35.dp.toPx(), bodyCy)) {
                            drawOval(pandaBlack, topLeft = Offset(pandaCx + 27.dp.toPx(), bodyCy - 10.dp.toPx()), size = Size(18.dp.toPx(), 35.dp.toPx()))
                        }
                        drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx - 15.dp.toPx(), bodyCy + 15.dp.toPx()))
                        drawCircle(pandaBlack, radius = 8.dp.toPx(), center = Offset(pandaCx + 15.dp.toPx(), bodyCy + 15.dp.toPx()))
                    }
                }
            }
            
            MascotType.CEBRIC -> {
                // --- CEBRIC V4: THE CREATIVE GIRAFFE (Male, Visionario, Clean Design) ---
                val cebricSkin = Color(0xFFFEF3C7) // Creamy Yellow
                val cebricSpot = Color(0xFF92400E) // Darker Brown
                val cebricBelly = Color(0xFFFFF7ED) // Lighter Belly

                val cebricCx = centerX
                // Base Y position for the body
                val bodyBaseY = 110.dp.toPx() + bodyOffset

                // Animation Factors
                val breathe = 1f + (breatheAnim * 0.005f) // Subtle breathing scale
                val headBob = if(pose == BrishPose.FOCUS) headBobAnim else 0f
                val headSway = if(pose == BrishPose.DEFAULT) cebricNeckSway else 0f
                
                // 1. LEGS (Sitting Pose - Clean)
                // Back/Side Legs (visible behind)
                drawOval(cebricSpot, topLeft = Offset(cebricCx - 40.dp.toPx(), bodyBaseY + 15.dp.toPx()), size = Size(20.dp.toPx(), 15.dp.toPx()))
                drawOval(cebricSpot, topLeft = Offset(cebricCx + 20.dp.toPx(), bodyBaseY + 15.dp.toPx()), size = Size(20.dp.toPx(), 15.dp.toPx()))
                
                // Front Legs (Sticking out forward)
                drawOval(cebricSkin, topLeft = Offset(cebricCx - 25.dp.toPx(), bodyBaseY + 10.dp.toPx()), size = Size(15.dp.toPx(), 25.dp.toPx()))
                drawOval(cebricSkin, topLeft = Offset(cebricCx + 10.dp.toPx(), bodyBaseY + 10.dp.toPx()), size = Size(15.dp.toPx(), 25.dp.toPx()))
                // Hooves
                drawOval(Color(0xFF5D4037), topLeft = Offset(cebricCx - 25.dp.toPx(), bodyBaseY + 30.dp.toPx()), size = Size(15.dp.toPx(), 8.dp.toPx()))
                drawOval(Color(0xFF5D4037), topLeft = Offset(cebricCx + 10.dp.toPx(), bodyBaseY + 30.dp.toPx()), size = Size(15.dp.toPx(), 8.dp.toPx()))

                // 2. BODY (Rounded Rectangle/Oval Integration)
                val bodyRect = Rect(
                    left = cebricCx - 35.dp.toPx(),
                    top = bodyBaseY - 30.dp.toPx(),
                    right = cebricCx + 35.dp.toPx(),
                    bottom = bodyBaseY + 25.dp.toPx()
                )
                drawOval(cebricSkin, topLeft = bodyRect.topLeft, size = Size(bodyRect.width, bodyRect.height))
                
                // Belly Patch
                drawOval(cebricBelly, topLeft = Offset(cebricCx - 20.dp.toPx(), bodyBaseY - 15.dp.toPx()), size = Size(40.dp.toPx(), 35.dp.toPx()))
                
                // Body Spots
                drawCircle(cebricSpot, radius = 5.dp.toPx(), center = Offset(cebricCx - 20.dp.toPx(), bodyBaseY))
                drawCircle(cebricSpot, radius = 4.dp.toPx(), center = Offset(cebricCx + 25.dp.toPx(), bodyBaseY - 10.dp.toPx()))
                drawCircle(cebricSpot, radius = 6.dp.toPx(), center = Offset(cebricCx + 15.dp.toPx(), bodyBaseY + 10.dp.toPx()))

                // 3. NECK (Tall and Proud)
                // Calculated Pivot for Head movement
                val neckWidth = 22.dp.toPx()
                val neckHeight = 50.dp.toPx()
                val neckBaseY = bodyBaseY - 25.dp.toPx()
                val neckTopY = neckBaseY - neckHeight
                
                // Draw Neck
                drawRect(
                    cebricSkin, 
                    topLeft = Offset(cebricCx - neckWidth/2 + headSway*0.5f, neckTopY), 
                    size = Size(neckWidth, neckHeight + 10.dp.toPx()) // Overlap slightly
                )
                // Neck Spots
                drawCircle(cebricSpot, radius = 3.dp.toPx(), center = Offset(cebricCx + headSway*0.5f, neckTopY + 15.dp.toPx()))
                drawCircle(cebricSpot, radius = 4.dp.toPx(), center = Offset(cebricCx - 5.dp.toPx() + headSway*0.5f, neckTopY + 35.dp.toPx()))

                // 4. TAIL (Small wag)
                val tailWag = if(pose == BrishPose.DEFAULT) eyePanAnim * 2 else 0f
                drawLine(cebricSpot, start = Offset(cebricCx + 35.dp.toPx(), bodyBaseY), end = Offset(cebricCx + 45.dp.toPx(), bodyBaseY - 10.dp.toPx() + tailWag), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(cebricCx + 45.dp.toPx(), bodyBaseY - 10.dp.toPx() + tailWag)) // Tuft

                // 5. HEAD (Rounded Boxy Shape - Male/Cartoonish)
                val headCy = neckTopY + headBob + (headSway * 1.5f) // Head moves more than neck base
                val headCx = cebricCx + headSway
                
                // Ears
                val earY = headCy - 15.dp.toPx()
                rotate(-20f, pivot = Offset(headCx - 20.dp.toPx(), earY + 5.dp.toPx())) {
                     drawOval(cebricSkin, topLeft = Offset(headCx - 35.dp.toPx(), earY), size = Size(20.dp.toPx(), 12.dp.toPx()))
                }
                rotate(20f, pivot = Offset(headCx + 20.dp.toPx(), earY + 5.dp.toPx())) {
                     drawOval(cebricSkin, topLeft = Offset(headCx + 15.dp.toPx(), earY), size = Size(20.dp.toPx(), 12.dp.toPx()))
                }

                // Ossicones (Horns)
                drawLine(cebricSpot, start = Offset(headCx - 8.dp.toPx(), headCy - 15.dp.toPx()), end = Offset(headCx - 10.dp.toPx(), headCy - 35.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Color(0xFF5D4037), radius = 4.dp.toPx(), center = Offset(headCx - 10.dp.toPx(), headCy - 35.dp.toPx()))
                drawLine(cebricSpot, start = Offset(headCx + 8.dp.toPx(), headCy - 15.dp.toPx()), end = Offset(headCx + 10.dp.toPx(), headCy - 35.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Color(0xFF5D4037), radius = 4.dp.toPx(), center = Offset(headCx + 10.dp.toPx(), headCy - 35.dp.toPx()))

                // Main Head Shape
                drawRoundRect(
                    color = cebricSkin,
                    topLeft = Offset(headCx - 22.dp.toPx(), headCy - 25.dp.toPx()),
                    size = Size(44.dp.toPx(), 50.dp.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
                
                // Snout (Lighter)
                drawRoundRect(
                    color = cebricBelly,
                    topLeft = Offset(headCx - 22.dp.toPx(), headCy + 5.dp.toPx()),
                    size = Size(44.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )

                // 6. FACE FEATURES
                val eyeY = headCy - 5.dp.toPx()
                val eyeOff = if(pose == BrishPose.PLANNER) eyePanAnim else 0f
                
                // Eyes (Circles)
                drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(headCx - 10.dp.toPx() + eyeOff, eyeY))
                drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(headCx + 10.dp.toPx() + eyeOff, eyeY))
                // Pupils
                drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(headCx - 10.dp.toPx() + eyeOff, eyeY))
                drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(headCx + 10.dp.toPx() + eyeOff, eyeY))
                
                // Nostrils
                drawCircle(cebricSpot, radius = 2.dp.toPx(), center = Offset(headCx - 6.dp.toPx(), headCy + 15.dp.toPx()))
                drawCircle(cebricSpot, radius = 2.dp.toPx(), center = Offset(headCx + 6.dp.toPx(), headCy + 15.dp.toPx()))
                
                // Smile (Confident/Creative Side Smirk)
                val smilePath = Path().apply {
                    moveTo(headCx - 5.dp.toPx(), headCy + 18.dp.toPx())
                    quadraticTo(headCx, headCy + 22.dp.toPx(), headCx + 8.dp.toPx(), headCy + 16.dp.toPx())
                }
                drawPath(smilePath, Color(0xFF5D4037), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))


                // 7. OUTFITS & PROPS (El Visionario)
                when(pose) {
                    BrishPose.DEFAULT -> {
                        // Orange Bow Tie (Clean)
                        val tieY = headCy + 30.dp.toPx() // Base of neck
                        val tieColor = Color(0xFFFF9800)
                        
                        // Left
                        drawPath(Path().apply {
                            moveTo(headCx, tieY)
                            lineTo(headCx - 12.dp.toPx(), tieY - 6.dp.toPx())
                            lineTo(headCx - 12.dp.toPx(), tieY + 6.dp.toPx())
                            close()
                        }, tieColor)
                        // Right
                        drawPath(Path().apply {
                            moveTo(headCx, tieY)
                            lineTo(headCx + 12.dp.toPx(), tieY - 6.dp.toPx())
                            lineTo(headCx + 12.dp.toPx(), tieY + 6.dp.toPx())
                            close()
                        }, tieColor)
                        // Knot
                        drawCircle(Color(0xFFE65100), radius = 3.dp.toPx(), center = Offset(headCx, tieY))
                    }
                    
                    BrishPose.PLANNER -> {
                        // Artist Scarf & Glasses (Hipster Creative)
                        val scarfColor = Color(0xFF3B82F6)
                        
                        // Scarf
                        drawRect(scarfColor, topLeft = Offset(headCx - 15.dp.toPx(), headCy + 25.dp.toPx()), size = Size(30.dp.toPx(), 8.dp.toPx()))
                        // Tail
                        drawRect(scarfColor, topLeft = Offset(headCx + 5.dp.toPx(), headCy + 33.dp.toPx()), size = Size(8.dp.toPx(), 20.dp.toPx()))
                        
                        // Thick Black Glasses
                        val glassColor = Color.Black
                        drawCircle(color = glassColor, radius = 9.dp.toPx(), center = Offset(headCx - 10.dp.toPx(), eyeY), style = Stroke(width = 2.5.dp.toPx()))
                        drawCircle(color = glassColor, radius = 9.dp.toPx(), center = Offset(headCx + 10.dp.toPx(), eyeY), style = Stroke(width = 2.5.dp.toPx()))
                        drawLine(color = glassColor, start = Offset(headCx - 1.dp.toPx(), eyeY), end = Offset(headCx + 1.dp.toPx(), eyeY), strokeWidth = 2.5.dp.toPx())
                    }
                    
                    BrishPose.NOTES -> {
                         // Beret & Paintbrush behind ear
                         val beretColor = Color(0xFFE11D48)
                         // Beret
                         drawOval(beretColor, topLeft = Offset(headCx - 30.dp.toPx(), headCy - 30.dp.toPx()), size = Size(50.dp.toPx(), 18.dp.toPx()))
                         drawCircle(beretColor, radius = 2.dp.toPx(), center = Offset(headCx, headCy - 30.dp.toPx()))
                         
                         // Paintbrush
                         drawLine(Color(0xFFD4B483), start = Offset(headCx + 22.dp.toPx(), headCy), end = Offset(headCx + 40.dp.toPx(), headCy - 20.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                         drawCircle(Color.Blue, radius = 3.dp.toPx(), center = Offset(headCx + 40.dp.toPx(), headCy - 20.dp.toPx())) // Paint tip
                    }
                    
                    BrishPose.FINANCE -> {
                        // Monocle & Top Hat (Rich Uncle Pennybags Vibe)
                        // Top Hat
                        val hatY = headCy - 35.dp.toPx()
                        drawRect(Color.Black, topLeft = Offset(headCx - 10.dp.toPx(), hatY - 20.dp.toPx()), size = Size(20.dp.toPx(), 20.dp.toPx()))
                        drawRect(Color.Black, topLeft = Offset(headCx - 18.dp.toPx(), hatY), size = Size(36.dp.toPx(), 4.dp.toPx())) // Brim
                        
                        // Monocle
                        drawCircle(Color(0xFFFFD700), radius = 7.dp.toPx(), center = Offset(headCx + 10.dp.toPx(), eyeY), style = Stroke(width = 2.dp.toPx()))
                        drawLine(Color(0xFFFFD700), start = Offset(headCx + 17.dp.toPx(), eyeY), end = Offset(headCx + 17.dp.toPx(), eyeY + 10.dp.toPx()), strokeWidth = 1.dp.toPx())
                    }
                    
                    BrishPose.FOCUS -> {
                         // Noise Cancelling Headphones
                         val hpColor = Color(0xFF8B5CF6)
                         drawArc(hpColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(headCx - 35.dp.toPx(), headCy - 25.dp.toPx()), size = Size(70.dp.toPx(), 50.dp.toPx()), style = Stroke(width = 5.dp.toPx()))
                         drawRoundRect(hpColor, topLeft = Offset(headCx - 38.dp.toPx(), headCy - 10.dp.toPx()), size = Size(10.dp.toPx(), 25.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
                         drawRoundRect(hpColor, topLeft = Offset(headCx + 28.dp.toPx(), headCy - 10.dp.toPx()), size = Size(10.dp.toPx(), 25.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
                    }
                    
                    else -> {}
                }
            }
                

        }

        // --- ARMS & PROPS (Based on Pose) for Generic Mascots (Poppin ONLY) ---
        // Other mascots have their own custom blocks.
        if (mascotType == MascotType.POPPIN) {
            
            // Jacket Base (Poppin only)
            drawLine(color = Color(0xFF374151), start = Offset(centerX, 50.dp.toPx() + bodyOffset), end = Offset(centerX, 100.dp.toPx() + bodyOffset), strokeWidth = 2.dp.toPx())

            when(pose) {
                BrishPose.DEFAULT -> {
                    // Holding Fish (Right Hand)
                    rotate(degrees = currentArmRotation, pivot = Offset(centerX + 45.dp.toPx(), 55.dp.toPx() + bodyOffset)) {
                         // Hand/Arm
                         drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                         
                         // Fish
                         val fishY = if(currentArmRotation < -40f) 45.dp.toPx() else 50.dp.toPx()
                         
                         val fishPath = Path().apply {
                            moveTo(centerX + 50.dp.toPx(), fishY + bodyOffset)
                            lineTo(centerX + 40.dp.toPx(), fishY - 10.dp.toPx() + bodyOffset)
                            lineTo(centerX + 60.dp.toPx(), fishY - 10.dp.toPx() + bodyOffset)
                            close()
                        }
                        drawPath(path = fishPath, color = Color(0xFF60A5FA))
                        drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 42.dp.toPx(), fishY - 5.dp.toPx() + bodyOffset), size = Size(16.dp.toPx(), 25.dp.toPx()))
                    }
                }
                BrishPose.PLANNER -> {
                    val batColor = Color(0xFFD4B483)
                    rotate(degrees = -20f + currentArmRotation, pivot = Offset(centerX + 45.dp.toPx(), 60.dp.toPx() + bodyOffset)) {
                        drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                        drawOval(color = batColor, topLeft = Offset(centerX + 40.dp.toPx(), 30.dp.toPx() + bodyOffset), size = Size(10.dp.toPx(), 50.dp.toPx()))
                    }
                }
                BrishPose.FINANCE -> {
                     val bagColor = Color(0xFF10B981)
                     rotate(degrees = currentArmRotation, pivot = Offset(centerX + 45.dp.toPx(), 55.dp.toPx() + bodyOffset)) {
                         drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                         drawCircle(color = bagColor, radius = 15.dp.toPx(), center = Offset(centerX + 47.dp.toPx(), 75.dp.toPx() + bodyOffset))
                         drawLine(color = Color.White, start = Offset(centerX + 47.dp.toPx(), 70.dp.toPx() + bodyOffset), end = Offset(centerX + 47.dp.toPx(), 80.dp.toPx() + bodyOffset), strokeWidth = 2.dp.toPx())
                     }
                }
                BrishPose.NOTES -> {
                    rotate(degrees = currentArmRotation, pivot = Offset(centerX + 45.dp.toPx(), 55.dp.toPx() + bodyOffset)) {
                        drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                        drawRect(color = Color(0xFFE11D48), topLeft = Offset(centerX + 40.dp.toPx(), 60.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 25.dp.toPx()))
                        drawRect(color = Color.Gray, topLeft = Offset(centerX + 42.dp.toPx(), 57.dp.toPx() + bodyOffset), size = Size(11.dp.toPx(), 3.dp.toPx()))
                    }
                }
                BrishPose.HABITS -> {
                    val dumbbellColor = Color(0xFF52525B)
                    rotate(degrees = -10f + currentArmRotation, pivot = Offset(centerX + 50.dp.toPx(), 60.dp.toPx() + bodyOffset)) {
                        drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                        drawLine(color = Color.Gray, start = Offset(centerX + 35.dp.toPx(), 70.dp.toPx() + bodyOffset), end = Offset(centerX + 65.dp.toPx(), 70.dp.toPx() + bodyOffset), strokeWidth = 4.dp.toPx())
                        drawRoundRect(color = dumbbellColor, topLeft = Offset(centerX + 30.dp.toPx(), 65.dp.toPx() + bodyOffset), size = Size(8.dp.toPx(), 10.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
                        drawRoundRect(color = dumbbellColor, topLeft = Offset(centerX + 62.dp.toPx(), 65.dp.toPx() + bodyOffset), size = Size(8.dp.toPx(), 10.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
                    }
                }
                BrishPose.FOCUS -> {
                    val headsetColor = Color(0xFF3B82F6)
                    drawArc(color = headsetColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(centerX - 40.dp.toPx(), 5.dp.toPx() + bodyOffset), size = Size(80.dp.toPx(), 60.dp.toPx()), style = Stroke(width = 6.dp.toPx()))
                    drawOval(color = headsetColor, topLeft = Offset(centerX - 42.dp.toPx(), 25.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 25.dp.toPx())) 
                    drawOval(color = headsetColor, topLeft = Offset(centerX + 27.dp.toPx(), 25.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 25.dp.toPx())) 
                    if (isWaving) {
                         rotate(degrees = currentArmRotation, pivot = Offset(centerX + 45.dp.toPx(), 55.dp.toPx() + bodyOffset)) {
                            drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                         }
                    }
                }
                BrishPose.CHAT -> {
                    if (isWaving) {
                         rotate(degrees = currentArmRotation, pivot = Offset(centerX + 45.dp.toPx(), 55.dp.toPx() + bodyOffset)) {
                            drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                         }
                    } else {
                        drawLine(color = Color(0xFF1F2937), start = Offset(centerX - 20.dp.toPx(), 60.dp.toPx() + bodyOffset), end = Offset(centerX + 20.dp.toPx(), 60.dp.toPx() + bodyOffset), strokeWidth = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
                BrishPose.GARDENING -> {
                     // Holding Watering Can
                     // Right Arm holding can
                     rotate(degrees = currentArmRotation, pivot = Offset(centerX + 45.dp.toPx(), 55.dp.toPx() + bodyOffset)) {
                         // 1. Arm
                         drawOval(color = Color(0xFF60A5FA), topLeft = Offset(centerX + 40.dp.toPx(), 50.dp.toPx() + bodyOffset), size = Size(15.dp.toPx(), 35.dp.toPx()))
                         
                         // 2. Watering Can
                         val canColor = Color(0xFF16A34A) // Green Can
                         val poutStart = Offset(centerX + 65.dp.toPx(), 70.dp.toPx() + bodyOffset)
                         
                         // Can Body
                         drawOval(color = canColor, topLeft = Offset(centerX + 45.dp.toPx(), 65.dp.toPx() + bodyOffset), size = Size(25.dp.toPx(), 20.dp.toPx()))
                         // Handle
                         drawArc(color = canColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(centerX + 45.dp.toPx(), 60.dp.toPx() + bodyOffset), size = Size(10.dp.toPx(), 15.dp.toPx()), style = Stroke(width = 3.dp.toPx()))
                         // Spout
                         drawLine(color = canColor, start = Offset(centerX + 65.dp.toPx(), 70.dp.toPx() + bodyOffset), end = Offset(centerX + 80.dp.toPx(), 65.dp.toPx() + bodyOffset), strokeWidth = 4.dp.toPx())
                         
                         // 3. Water Droplets
                         // Only draw if tilted enough (simulated by rotation angle > 10)
                         if (currentArmRotation > 10f) {
                             val dropColor = Color(0xFF60A5FA)
                             for (i in 0..2) {
                                 val offset = (dropletOffset + i * 30f) % 100f
                                 drawCircle(
                                     color = dropColor, 
                                     radius = 2.dp.toPx(), 
                                     center = Offset(centerX + 80.dp.toPx() + (i*2).dp.toPx(), 65.dp.toPx() + bodyOffset + offset)
                                 )
                             }
                         }
                     }
                }
            }
        }
        
        // --- LEVEL CROWN (Level 20+) ---
        if (level >= 20) {
             val crownY = bodyOffset // Follow body movement
             val crownPath = Path().apply {
                 moveTo(centerX - 15.dp.toPx(), 10.dp.toPx() + crownY)
                 lineTo(centerX - 15.dp.toPx(), 0.dp.toPx() + crownY)
                 lineTo(centerX - 5.dp.toPx(), 10.dp.toPx() + crownY)
                 lineTo(centerX, 0.dp.toPx() + crownY)
                 lineTo(centerX + 5.dp.toPx(), 10.dp.toPx() + crownY)
                 lineTo(centerX + 15.dp.toPx(), 0.dp.toPx() + crownY)
                 lineTo(centerX + 15.dp.toPx(), 10.dp.toPx() + crownY)
                 close()
             }
             drawPath(crownPath, Color(0xFFFFD700)) // Gold
        } else if (level >= 5) {
             // Level 5+: Bronze Star on chest/corner
             drawCircle(Color(0xFFCD7F32), radius = 5.dp.toPx(), center = Offset(size.width - 10.dp.toPx(), size.height - 10.dp.toPx()))
        }
    }
}

@Composable
fun BrishGardeningScene(
    modifier: Modifier = Modifier,
    mascotType: MascotType = MascotType.POPPIN
) {
    Box(
        modifier = modifier.fillMaxWidth().height(350.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ... (Canvas background same)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            // ... Hills ...
            val hillPath = Path().apply {
                moveTo(0f, height) // Bottom left
                lineTo(0f, height * 0.7f) // Start of hill (left)
                quadraticTo(
                    width * 0.5f, height * 0.5f, // Control point (Peak of hill)
                    width, height * 0.7f // End of hill (right)
                )
                lineTo(width, height) // Bottom right
                close()
            }
            drawPath(path = hillPath, color = Color(0xFF4ADE80)) // Vibrant Green
            
            // ... Flower ...
            val flowerX = width * 0.75f
            val flowerY = height * 0.65f // On the hill slope roughly
            
            // Stem
            drawLine(color = Color(0xFF166534), start = Offset(flowerX, flowerY), end = Offset(flowerX, flowerY - 40.dp.toPx()), strokeWidth = 4.dp.toPx())
            
            // Petals
            drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(flowerX - 8.dp.toPx(), flowerY - 45.dp.toPx()))
            drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(flowerX + 8.dp.toPx(), flowerY - 45.dp.toPx()))
            drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(flowerX, flowerY - 53.dp.toPx()))
            drawCircle(color = Color.White, radius = 10.dp.toPx(), center = Offset(flowerX, flowerY - 37.dp.toPx()))
            // Center
            drawCircle(color = Color(0xFFFACC15), radius = 6.dp.toPx(), center = Offset(flowerX, flowerY - 45.dp.toPx()))
        }
        
        // --- MASCOT ---
        // Positioned on the hill
        BrishMascotAnimation(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-40).dp, y = (-80).dp), // Adjust to stand on hill
            pose = BrishPose.GARDENING,
            mascotType = mascotType
        )
    }
}

// Helper for triangles
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTriangle(color: Color, center: Offset, size: Float, pointingDown: Boolean = false) {
    val path = Path().apply {
        if (pointingDown) {
            moveTo(center.x - size, center.y - size)
            lineTo(center.x + size, center.y - size)
            lineTo(center.x, center.y + size)
        } else {
            moveTo(center.x, center.y - size)
            lineTo(center.x - size, center.y + size)
            lineTo(center.x + size, center.y + size)
        }
        close()
    }
    drawPath(path, color)
}
