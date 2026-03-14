package com.app.aiexamples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.aiexamples.geminichatapp.ChatScreen
import com.app.aiexamples.core.AppHeader
import com.app.aiexamples.core.StatusBadge
import com.app.aiexamples.core.theme.AccentPink
import com.app.aiexamples.core.theme.AccentPurple
import com.app.aiexamples.core.theme.Background
import com.app.aiexamples.core.theme.Border
import com.app.aiexamples.core.theme.Card
import com.app.aiexamples.core.theme.TextMuted
import com.app.aiexamples.core.theme.TextPrimary
import com.app.aiexamples.mlkitfacedetection.FaceDetectionScreen
import com.app.aiexamples.mlkitimagelabeling.ImageLabelingScreen
import com.app.aiexamples.mlkitqrscanner.ScannerScreen
import com.app.aiexamples.mlkittextrecognition.TextRecognitionScreen
import com.app.aiexamples.geminiphotoexplainer.PhotoExplainerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        setContent {
            MaterialTheme {
                NavigationStack()
            }
        }
    }
}


@Composable
fun HomeScreen(navController: NavController) {

    val modules = listOf(
        AIModule("01", "QR / Barcode Scanner", "ML Kit", "Scan any QR code or barcode", AccentPurple),
        AIModule("02", "Face Detection", "ML Kit", "Detect faces and emotions", AccentPink),
        AIModule("03", "Text Recognition", "ML Kit", "Read text from any image", Color(0xFF6AFFDA)),
        AIModule("04", "Image Labeling", "ML Kit", "Identify objects in photos", Color(0xFFFFB347)),
        AIModule("05", "Photo Explainer", "Gemini", "AI describes any image", AccentPurple),
        AIModule("06", "AI Chat", "Gemini", "Full chatbot with memory", AccentPink),
        AIModule("07", "Voice Assistant", "Gemini", "Talk to AI with your voice", Color(0xFF6AFFDA)),
        AIModule("08", "PDF Summarizer", "Gemini", "Summarize any document", Color(0xFFFFB347)),
        AIModule("09", "Custom AI Model", "TFLite", "Run your own AI offline", AccentPurple),
        AIModule("10", "Complete AI App", "All", "Full production AI app", AccentPink),
    )

    Scaffold(topBar = {
        AppHeader(title = "Android AI Examples", subtitle = "10 examples • Jetpack Compose")
    }, containerColor = Background) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(modules) { module ->
                ModuleCard(module = module) {
                    when (module.number) {
                        "01" -> navController.navigate(route = Screen.QrScanner.route) {
                            popUpTo(Screen.Main.route)
                        }

                        "02" -> navController.navigate(route = Screen.FaceDetector.route) {
                            popUpTo(Screen.Main.route)
                        }

                        "03" -> navController.navigate(route = Screen.TextDetector.route) {
                            popUpTo(Screen.Main.route)
                        }

                        "04" -> navController.navigate(route = Screen.ImageLabeling.route) {
                            popUpTo(Screen.Main.route)
                        }

                        "05" -> navController.navigate(route = Screen.PhotoExplainer.route) {
                            popUpTo(Screen.Main.route)
                        }

                        "06" -> navController.navigate(route = Screen.Chat.route) {
                            popUpTo(Screen.Main.route)
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }
}

data class AIModule(val number: String, val title: String, val tech: String, val description: String, val color: Color)

@Composable
fun ModuleCard(module: AIModule, onClick: () -> Unit) {

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Card), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Number circle
            Box(modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(module.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(text = module.number, color = module.color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(text = module.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = module.description, color = TextMuted, fontSize = 12.sp)
            }

            // Tech badge
            StatusBadge(text = module.tech, color = module.color)
        }
    }
}

@Composable
fun NavigationStack() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(route = Screen.Main.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.QrScanner.route) {
            ScannerScreen(onBackCallback = {
                navController.navigate(Screen.Main.route)
            })
        }
        composable(route = Screen.FaceDetector.route) {
            FaceDetectionScreen(onBackCallback = {
                navController.navigate(Screen.Main.route)
            })
        }
        composable(route = Screen.TextDetector.route) {
            TextRecognitionScreen(onBackCallback = {
                navController.navigate(Screen.Main.route)
            })
        }
        composable(route = Screen.ImageLabeling.route) {
            ImageLabelingScreen(onBackCallback = {
                navController.navigate(Screen.Main.route)
            })
        }
        composable(route = Screen.PhotoExplainer.route) {
            PhotoExplainerScreen(onBackCallback = {
                navController.navigate(Screen.Main.route)
            })
        }
        composable(route = Screen.Chat.route) {
            ChatScreen(onBackCallback = {
                navController.navigate(Screen.Main.route)
            })
        }
    }
}

// Screen.kt
sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object QrScanner : Screen("qr_scanner_screen")
    object FaceDetector : Screen("face_detector_screen")
    object TextDetector : Screen("text_detector_screen")
    object ImageLabeling : Screen("image_labeling_screen")
    object PhotoExplainer : Screen("photo_explainer_screen")
    object Chat : Screen("chat_screen")
}