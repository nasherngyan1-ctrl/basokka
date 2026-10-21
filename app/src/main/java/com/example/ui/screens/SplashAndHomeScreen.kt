package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BasoProjectEntity
import com.example.ui.theme.BasoBgDark
import com.example.ui.theme.BasoBorderCyan
import com.example.ui.theme.BasoCardElevated
import com.example.ui.theme.BasoCardSurface
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkOnAccent
import com.example.ui.theme.BasoGold
import com.example.ui.theme.BasoSuccessGreen
import com.example.ui.theme.BasoTextMuted
import com.example.ui.theme.BasoTextWhite
import kotlin.math.cos
import kotlin.math.sin

/**
 * Luxury Typographic Letter Monogram Badge ("B" + Kurdish "ب" crest)
 * Used in the top app bar, profile header, builder header, and chat avatars.
 */
@Composable
fun BasoLetterMonogramBadge(
    size: Dp = 44.dp,
    primaryLetter: String = "B",
    subLetter: String? = "ب",
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val baseMod = modifier
        .size(size)
        .clip(RoundedCornerShape(size * 0.30f))
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(
        contentAlignment = Alignment.Center,
        modifier = baseMod
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF061320),
                        Color(0xFF0F263C),
                        Color(0xFF050E18)
                    )
                )
            )
            .border(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    colors = listOf(BasoGold, BasoCyan, BasoGold)
                ),
                shape = RoundedCornerShape(size * 0.30f)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = w * 0.42f

            // Subtle inner hexagon geometry
            val hexPath = Path().apply {
                for (i in 0..5) {
                    val angle = Math.toRadians((60.0 * i - 30.0))
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = hexPath,
                color = BasoCyan.copy(alpha = 0.25f),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Top-right gold sparkle dot
            drawCircle(
                color = BasoGold,
                radius = w * 0.055f,
                center = Offset(w * 0.78f, h * 0.22f)
            )
        }

        // Cyan 3D Depth Offset Letter
        Text(
            text = primaryLetter,
            color = BasoCyan.copy(alpha = 0.70f),
            fontSize = (size.value * 0.52f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.offset(x = 1.5.dp, y = 1.5.dp)
        )

        // Metallic Gold Foreground Sculpted Letter
        Text(
            text = primaryLetter,
            color = BasoGold,
            fontSize = (size.value * 0.52f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif
        )

        // Corner Kurdish letter micro-emblem if size allows
        if (subLetter != null && size >= 38.dp) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .size(size * 0.30f)
                    .clip(CircleShape)
                    .background( Color(0xFF071624))
                    .border(0.8.dp, BasoCyan, CircleShape)
            ) {
                Text(
                    text = subLetter,
                    color = BasoCyan,
                    fontSize = (size.value * 0.18f).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Large Hero 3D Typographic Crest for the Splash Screen ("BASO" Sculpted Letter Emblem)
 */
@Composable
fun BasoHeroLetterCrest(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(216.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Radial ambient cyan & gold aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BasoCyan.copy(alpha = 0.24f),
                        BasoGold.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = w * 0.5f
                ),
                radius = w * 0.5f,
                center = Offset(cx, cy)
            )

            // Outer Hexagonal Architectural Frame
            val outerHex = Path().apply {
                val r = w * 0.44f
                for (i in 0..5) {
                    val angle = Math.toRadians(60.0 * i - 90.0)
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = outerHex,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0A1D2E), Color(0xFF06111B))
                )
            )
            drawPath(
                path = outerHex,
                brush = Brush.linearGradient(
                    colors = listOf(BasoCyan, BasoGold, BasoCyan)
                ),
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Inner Rotated Diamond / Octagon Gold Frame
            val innerHex = Path().apply {
                val r = w * 0.37f
                for (i in 0..5) {
                    val angle = Math.toRadians(60.0 * i - 90.0)
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = innerHex,
                color = BasoGold.copy(alpha = 0.45f),
                style = Stroke(width = 1.4.dp.toPx())
            )

            // Corner nodes
            val nodeR = w * 0.44f
            for (i in 0..5) {
                val angle = Math.toRadians(60.0 * i - 90.0)
                val px = cx + (nodeR * cos(angle)).toFloat()
                val py = cy + (nodeR * sin(angle)).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) BasoGold else BasoCyan,
                    radius = 4.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Center Sculpted 3D Letter "B" with layered depth + "باسۆ" monogram ribbon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Deep Cyan Neon Extrusion Layer
                Text(
                    text = "B",
                    color = BasoCyan.copy(alpha = 0.85f),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.offset(x = 4.dp, y = 4.dp)
                )
                // Dark core separator
                Text(
                    text = "B",
                    color = Color(0xFF040B12),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                )
                // Primary Gold Crown Letter
                Text(
                    text = "B",
                    color = BasoGold,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif
                )
            }

            Surface(
                color = Color(0xFF071624),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.7f)),
                modifier = Modifier.offset(y = (-10).dp)
            ) {
                Text(
                    text = "باسۆ • AI",
                    color = BasoCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun SplashWelcomeScreen(
    isKurdish: Boolean,
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040A10),
                        Color(0xFF071522),
                        Color(0xFF040B12)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Centerpiece Luxury Typographic Letter Emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasoHeroLetterCrest()

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "BASO",
                    color = BasoTextWhite,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isKurdish) "ستۆدیۆی فریلانسەر" else "Freelancer AI Studio",
                    color = BasoGold,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3 Glowing Pill Badges: دیزاین | وێبسایت | ئەپ
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tags = if (isKurdish) {
                        listOf("✦ دیزاین ✦", "✦ وێبسایت ✦", "✦ ئەپ ✦")
                    } else {
                        listOf("✦ Design ✦", "✦ Website ✦", "✦ App ✦")
                    }
                    tags.forEach { label ->
                        Surface(
                            color = BasoCardSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = label,
                                color = BasoTextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Tagline + Gold CTA Button + Page Indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isKurdish) "هێزی خەلاقیت بە دیجیتاڵ دەکات" else "Transforming Creativity Into Digital Reality",
                    color = BasoTextWhite.copy(alpha = 0.88f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onGetStarted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BasoGold,
                        contentColor = BasoDarkOnAccent
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("splash_get_started_button")
                ) {
                    Text(
                        text = if (isKurdish) "دەست پێ بکە" else "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(BasoCyan)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BasoTextMuted.copy(alpha = 0.35f))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BasoTextMuted.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDashboardScreen(
    isKurdish: Boolean,
    projects: List<BasoProjectEntity>,
    activeProviderBadge: String,
    configuredKeyCount: Int,
    autoSwitchEnabled: Boolean,
    onOpenNewCreation: () -> Unit,
    onSelectWebBuilder: () -> Unit,
    onSelectAppBuilder: () -> Unit,
    onSeeAllProjects: () -> Unit,
    onOpenProject: (BasoProjectEntity) -> Unit,
    onOpenAiKeyVault: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BasoBgDark)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Top Bar: Menu on left, AI Key Vault Pill in center, Gold "باسۆ" + Typographic Letter Monogram on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.testTag("home_menu_language_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu / Language",
                            tint = BasoTextWhite
                        )
                    }

                    // Quick AI Key Vault & Auto-Router Pill
                    Surface(
                        color = BasoCardSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (autoSwitchEnabled) BasoCyan.copy(alpha = 0.65f) else BasoGold.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier
                            .clickable { onOpenAiKeyVault() }
                            .testTag("home_ai_keys_vault_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "AI Keys",
                                tint = BasoGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isKurdish) {
                                    "کلیلەکانی AI ($configuredKeyCount) • $activeProviderBadge"
                                } else {
                                    "AI Keys ($configuredKeyCount) • $activeProviderBadge"
                                },
                                color = BasoTextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isKurdish) "باسۆ" else "BASO",
                            color = BasoGold,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "AI STUDIO",
                            color = BasoCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    BasoLetterMonogramBadge(
                        size = 44.dp,
                        primaryLetter = "B",
                        subLetter = "ب",
                        onClick = onOpenAiKeyVault
                    )
                }
            }
        }

        // Greeting: سڵاو، 👋 / بەخێربێیت بۆ BASO
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isKurdish) "سڵاو، باسۆ 👋" else "Hello, Baso 👋",
                    color = BasoTextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isKurdish) "بەخێربێیت بۆ ستۆدیۆی زیرەکی تایبەت بە خۆت" else "Welcome to BASO Personal AI Studio",
                    color = BasoTextWhite.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Multi-AI Auto-Switch Banner Card on Home Screen
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1B2A)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BasoGold.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiKeyVault() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = BasoCardElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BasoGold),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BasoGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isKurdish) {
                                    "سیستەمی زیرەکی کلیلەکان و گۆڕینی خۆکاری AI"
                                } else {
                                    "Multi-AI Key Vault & Auto-Failover Router"
                                },
                                color = BasoTextWhite,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isKurdish) {
                                    "پاڵپشتی Gemini • OpenAI • Claude • DeepSeek • Groq • OpenRouter"
                                } else {
                                    "Supports Gemini, OpenAI, Claude, DeepSeek, Groq & Auto-Switch"
                                },
                                color = BasoCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = BasoGold
                    )
                }
            }
        }

        // Hero Banner Card matching Screen 2
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.38f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isKurdish) "دروستکردنی ئەپ و وێبسایت" else "Build Apps & Websites",
                            color = BasoTextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isKurdish) "بە چاتی ڕاستەوخۆ و چەندین دیزاینی جیاواز" else "Live conversational editing & multi-style UI",
                            color = BasoTextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenNewCreation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BasoGold,
                                contentColor = BasoDarkOnAccent
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("hero_create_new_button")
                        ) {
                            Text(
                                text = if (isKurdish) "بەرهەمی نوێ دروست بکە" else "Create New Project",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Image(
                        painter = painterResource(id = R.drawable.img_baso_hero_devices),
                        contentDescription = "Devices Preview Illustration",
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Section: بەشەکان (Web Builder & App Builder)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isKurdish) "بەشەکان" else "Categories",
                    color = BasoTextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Web Builder Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectWebBuilder() }
                            .testTag("card_web_builder")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Web Builder",
                                tint = BasoCyan,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Web Builder",
                                color = BasoTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isKurdish) "وێبسایت و وێب ئەپ" else "Websites & Web Apps",
                                color = BasoTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // App Builder Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectAppBuilder() }
                            .testTag("card_app_builder")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "App Builder",
                                tint = BasoCyan,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "App Builder",
                                color = BasoTextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isKurdish) "ئەپلیکەیشن مۆبایل" else "Mobile Applications",
                                color = BasoTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Section Header: پڕۆژەکانت + بینین هەموو
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isKurdish) "پڕۆژەکانت" else "Your Projects",
                    color = BasoTextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isKurdish) "بینین هەموو" else "See All",
                    color = BasoCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onSeeAllProjects() }
                        .padding(4.dp)
                        .testTag("see_all_projects_button")
                )
            }
        }

        items(projects.take(4), key = { it.id }) { project ->
            HomeProjectRowCard(
                project = project,
                onClick = { onOpenProject(project) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HomeProjectRowCard(
    project: BasoProjectEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BasoBorderCyan),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = BasoCardElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.35f)),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                project.title.contains("Shop", ignoreCase = true) -> Icons.Default.ShoppingBag
                                project.platform == "Android" -> Icons.Default.PhoneAndroid
                                else -> Icons.Default.Web
                            },
                            contentDescription = project.title,
                            tint = if (project.title.contains("Shop", ignoreCase = true)) BasoSuccessGreen else BasoCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = project.title,
                        color = BasoTextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = project.updatedLabelEn,
                        color = BasoTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = BasoCyan
            )
        }
    }
}

@Composable
fun NewCreationSelectorScreen(
    isKurdish: Boolean,
    onBack: () -> Unit,
    onSelectMobileApp: () -> Unit,
    onSelectWebsite: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasoBgDark)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("new_creation_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BasoTextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isKurdish) "دروستکردنی نوێ" else "Create New Project",
                color = BasoTextWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isKurdish) "هەڵبژاردنی جۆری پڕۆژەکەت" else "Choose your project type",
                color = BasoTextMuted,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1: ئەپلیکەیشن مۆبایل
            Card(
                colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, BasoCyan.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectMobileApp() }
                    .testTag("selector_mobile_app_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = BasoCardElevated,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, BasoCyan),
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = BasoCyan,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isKurdish) "ئەپلیکەیشن مۆبایل" else "Mobile Application",
                                color = BasoTextWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Android (Kotlin)", color = BasoTextMuted, fontSize = 13.sp)
                            Text("• Material 3 UI", color = BasoTextMuted, fontSize = 13.sp)
                            Text("• APK Export", color = BasoTextMuted, fontSize = 13.sp)
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = BasoCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Card 2: وێبسایت / وێب ئەپ
            Card(
                colors = CardDefaults.cardColors(containerColor = BasoCardSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, BasoCyan.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWebsite() }
                    .testTag("selector_website_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = BasoCardElevated,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, BasoCyan),
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = BasoCyan,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isKurdish) "وێبسایت / وێب ئەپ" else "Website / Web App",
                                color = BasoTextWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• HTML / CSS / JS", color = BasoTextMuted, fontSize = 13.sp)
                            Text("• Responsive Design", color = BasoTextMuted, fontSize = 13.sp)
                            Text("• ZIP Export", color = BasoTextMuted, fontSize = 13.sp)
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = BasoCyan
                    )
                }
            }
        }

        // Bottom Gold-bordered AI Tip Card
        Surface(
            color = BasoCardSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.2.dp, BasoGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "AI Tip",
                    tint = BasoGold,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = if (isKurdish) {
                        "دەتوانیت لەگەڵ چەندین مۆدێلی AI (Gemini, OpenAI, Claude, DeepSeek) پڕۆژەکەت دروست بکەیت."
                    } else {
                        "Build with multiple AI engines (Gemini, OpenAI, Claude, DeepSeek) with automatic failover."
                    },
                    color = BasoTextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
