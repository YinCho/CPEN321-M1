package com.example.cpen321application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import android.os.SystemClock
import kotlin.random.Random
import kotlinx.coroutines.delay
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            CPEN321ApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        apiBaseUrl = BuildConfig.API_BASE_URL,
//                        modifier = Modifier.padding(innerPadding)
//                    )
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("main")}

    when (currentScreen) {
        "main" -> MainScreen(
            onLoginClick = {currentScreen = "login" },
            onLiveClick = { currentScreen = "live" },
            onTimerClick = { currentScreen = "timer" }
        )

        "login" -> LoginScreen(onBackClick = { currentScreen = "main" })
        "live" -> LiveUpdatesScreen(onBackClick = { currentScreen = "main" })
        "timer" -> TimerScreen(onBackClick = { currentScreen = "main" })
    }
}

@Composable
fun MainScreen(onLoginClick: () -> Unit,
               onLiveClick: () -> Unit,
               onTimerClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

        Button(onClick = onLoginClick) {
            Text("Login + Server")
        }
        Button(onClick = onLiveClick) {
            Text("Live Updates")
        }
        Button(onClick = onTimerClick) {
            Text("Timer and Surprise")
        }
    }
}

@Composable
fun TimerScreen(onBackClick: () -> Unit) {

    var minutes by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }

    var timeRemaining by remember { mutableStateOf(0) }

    // setup, countdown, waiting, ready, result
    var phase by remember { mutableStateOf("setup") }

    var reactionStartTime by remember { mutableStateOf(0L) }

    var round by remember { mutableStateOf(1) }

    var reactionScores by remember {
        mutableStateOf(listOf<Long>())
    }

    var targetPosition by remember {
        mutableStateOf(0)
    }

    var errorText by remember { mutableStateOf("") }

    LaunchedEffect(phase) {

        if (phase == "countdown") {

            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
            }

            phase = "waiting"
        }

        if (phase == "waiting") {

            val randomDelay = Random.nextLong(
                1000,
                4001
            )

            delay(randomDelay)

            targetPosition =
                Random.nextInt(0, 9)

            reactionStartTime =
                SystemClock.elapsedRealtime()

            phase = "ready"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (phase) {

            "setup" -> {

                Text("Set Timer")

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = minutes,
                    onValueChange = {
                        if (it.all { character ->
                                character.isDigit()
                            }) {
                            minutes = it
                        }
                    },
                    label = {
                        Text("Minutes")
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = seconds,
                    onValueChange = {
                        if (it.all { character ->
                                character.isDigit()
                            }) {
                            seconds = it
                        }
                    },
                    label = {
                        Text("Seconds")
                    }
                )

                if (errorText.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(errorText)
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {

                        val mins =
                            minutes.toIntOrNull() ?: 0

                        val secs =
                            seconds.toIntOrNull() ?: 0

                        if (mins == 0 && secs == 0) {

                            errorText =
                                "Enter a time greater than 0."

                        } else if (secs > 59) {

                            errorText =
                                "Seconds must be between 0 and 59."

                        } else {

                            errorText = ""

                            timeRemaining =
                                mins * 60 + secs

                            round = 1

                            reactionScores =
                                emptyList()

                            phase = "countdown"
                        }
                    }
                ) {
                    Text("Start Timer")
                }
            }

            "countdown" -> {

                Text("Time Remaining")

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    "${timeRemaining / 60}:" +
                            String.format(
                                "%02d",
                                timeRemaining % 60
                            )
                )
            }

            "waiting" -> {

                Text("Timer finished!")

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text("Round $round of 3")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text("Get ready...")

                Text("Wait for the button!")
            }

            "ready" -> {

                Text("Round $round of 3")

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                val positions = listOf(
                    Alignment.TopStart,
                    Alignment.TopCenter,
                    Alignment.TopEnd,
                    Alignment.CenterStart,
                    Alignment.Center,
                    Alignment.CenterEnd,
                    Alignment.BottomStart,
                    Alignment.BottomCenter,
                    Alignment.BottomEnd
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {

                    Button(
                        modifier = Modifier.align(
                            positions[targetPosition]
                        ),
                        onClick = {

                            val currentReaction =
                                SystemClock.elapsedRealtime() -
                                        reactionStartTime

                            reactionScores =
                                reactionScores +
                                        currentReaction

                            if (round < 3) {

                                round++

                                phase = "waiting"

                            } else {

                                phase = "result"
                            }
                        }
                    ) {
                        Text("TAP!")
                    }
                }
            }

            "result" -> {

                Text("Reaction Challenge Complete!")

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                reactionScores.forEachIndexed {
                        index,
                        score ->

                    Text(
                        "Round ${index + 1}: " +
                                "$score ms"
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                if (reactionScores.isNotEmpty()) {

                    val best =
                        reactionScores.minOrNull()
                            ?: 0

                    val average =
                        reactionScores
                            .average()
                            .toLong()

                    Text(
                        "Best: $best ms"
                    )

                    Text(
                        "Average: $average ms"
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {

                        minutes = ""
                        seconds = ""

                        timeRemaining = 0

                        round = 1

                        reactionScores =
                            emptyList()

                        errorText = ""

                        phase = "setup"
                    }
                ) {
                    Text("Play Again")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onBackClick
        ) {
            Text("Back")
        }
    }
}

@Composable
fun LiveUpdatesScreen(onBackClick: () -> Unit) {

    var connectionStatus by remember {
        mutableStateOf("Connecting...")
    }

    val pixels = remember {
        mutableStateListOf<Color>().apply {
            repeat(16 * 16) {
                add(Color.White)
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val client = remember {
        OkHttpClient()
    }

    DisposableEffect(Unit) {

        val request = Request.Builder()
            .url("wss://34.169.187.216/ws")
            .build()

        val listener = object : WebSocketListener() {

            override fun onOpen(
                webSocket: WebSocket,
                response: Response
            ) {
                coroutineScope.launch {
                    connectionStatus = "Connected"
                }
            }

            override fun onMessage(
                webSocket: WebSocket,
                text: String
            ) {
                try {
                    val json = JSONObject(text)

                    val x = json.getInt("x")
                    val y = json.getInt("y")
                    val hexColor = json.getString("color")

                    val pixelColor = Color(
                        android.graphics.Color.parseColor(hexColor)
                    )

                    if (x in 0..15 && y in 0..15) {
                        coroutineScope.launch {
                            pixels[y * 16 + x] = pixelColor
                        }
                    }

                } catch (e: Exception) {
                    coroutineScope.launch {
                        connectionStatus =
                            "Pixel error: ${e.message}"
                    }
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                coroutineScope.launch {
                    connectionStatus =
                        "Error: ${t.message}"
                }
            }
        }

        val webSocket =
            client.newWebSocket(request, listener)

        onDispose {
            webSocket.close(1000, "Leaving screen")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("WebSocket: $connectionStatus")

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .size(320.dp)
                .border(1.dp, Color.Black)
        ) {
            val cellWidth = size.width / 16
            val cellHeight = size.height / 16

            for (y in 0 until 16) {
                for (x in 0 until 16) {

                    val color = pixels[y * 16 + x]

                    drawRect(
                        color = color,
                        topLeft = Offset(
                            x * cellWidth,
                            y * cellHeight
                        ),
                        size = Size(
                            cellWidth,
                            cellHeight
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackClick) {
            Text("Back")
        }
    }
}

@Composable
fun LoginScreen(onBackClick: () -> Unit) {
    var nameText by remember { mutableStateOf("Loading name...") }
    var serverTimeText by remember { mutableStateOf("Loading server time...")}
    var clientTimeText by remember { mutableStateOf("") }
    var serverIpText by remember { mutableStateOf("Loading server IP...") }
    var clientIpText by remember { mutableStateOf("Loading client IP...") }
    var googleUserName by remember { mutableStateOf("Not signed in") }
    var googleStatus by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val credentialManager = remember {
        CredentialManager.create(context)
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {

            nameText = fetchName(BuildConfig.API_BASE_URL)
            serverTimeText = fetchServerTime(BuildConfig.API_BASE_URL)
            serverIpText = fetchServerIp(BuildConfig.API_BASE_URL)
            clientIpText = fetchClientIp(BuildConfig.API_BASE_URL)

            clientTimeText = ZonedDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss 'GMT'xxx")
            )
        }

    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAuthenticated) {
            Text("Your name: $nameText")
            Text("Server time: $serverTimeText")
            Text("Client time: $clientTimeText")
            Text("Server IP: $serverIpText")
            Text("Client IP: $clientIpText")
        } else {
            Text("Sign in with Google to continue")
        }

        Button(onClick = onBackClick) {
            Text("Back")
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    googleStatus = "Signing in..."

                    val googleOption =
                        GetSignInWithGoogleOption.Builder(
                            context.getString(R.string.google_web_client_id)
                        ).build()

                    val request =
                        GetCredentialRequest.Builder()
                            .addCredentialOption(googleOption)
                            .build()

                    try {
                        val result = credentialManager.getCredential(
                            context = context,
                            request = request
                        )

                        val credential = result.credential

                        if (
                            credential is CustomCredential &&
                            credential.type ==
                            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleCredential =
                                GoogleIdTokenCredential.createFrom(
                                    credential.data
                                )

                            val firstName =
                                googleCredential.givenName ?: ""

                            val lastName =
                                googleCredential.familyName ?: ""

                            googleUserName =
                                "$firstName $lastName".trim()

                            if (googleUserName.isBlank()) {
                                googleUserName =
                                    googleCredential.displayName
                                        ?: googleCredential.id
                            }

                            googleStatus = ""
                            isAuthenticated = true
                        } else {
                            googleStatus =
                                "Unexpected credential type"
                        }

                    } catch (e: GetCredentialException) {
                        googleStatus =
                            "Sign in failed: ${e.message}"
                    } catch (e: Exception) {
                        googleStatus =
                            "Sign in error: ${e.message}"
                    }
                }
            }
        ) {
            Text("Sign in with Google")
        }

        Text("Logged-in user: $googleUserName")

        if (googleStatus.isNotBlank()) {
            Text(googleStatus)
        }
    }


}



@Composable
fun Greeting(apiBaseUrl: String, modifier: Modifier = Modifier) {
    var statusText by remember { mutableStateOf("Checking backend at $apiBaseUrl/health...") }

    LaunchedEffect(apiBaseUrl) {
        statusText = fetchHealthStatus(apiBaseUrl)
    }

    Text(
        text = statusText,
        modifier = modifier
    )
}

private suspend fun fetchServerTime(apiBaseUrl: String): String =
    withContext(Dispatchers.IO) {

        val url = "${apiBaseUrl.trimEnd('/')}/server-time"

        try {
            val connection =
                URL(url).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            val body = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            body
        } catch (e: Exception) {
            "Could not load server time: ${e.message}"
        }
    }
private suspend fun fetchName(apiBaseUrl: String): String =
    withContext(Dispatchers.IO) {

        val url = "${apiBaseUrl.trimEnd('/')}/name"

        try {
            val connection =
                URL(url).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            val body = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            body
        } catch (e: Exception) {
            "Could not load name: ${e.message}"
        }
    }
private suspend fun fetchHealthStatus(apiBaseUrl: String): String = withContext(Dispatchers.IO) {
    val healthUrl = "${apiBaseUrl.trimEnd('/')}/health"
    try {
        val connection = (URL(healthUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
        }

        when (val code = connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                "Backend healthy ($healthUrl): $body"
            }
            else -> {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                "Backend error ($healthUrl): HTTP $code${errorBody?.let { " — $it" } ?: ""}"
            }
        }
    } catch (e: Exception) {
        "Backend unreachable ($healthUrl): ${e.message ?: e.javaClass.simpleName}"
    }
}

private suspend fun fetchServerIp(apiBaseUrl: String): String =
    withContext(Dispatchers.IO) {

        val url = "${apiBaseUrl.trimEnd('/')}/server-ip"

        try {
            val connection =
                URL(url).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            connection.inputStream
                .bufferedReader()
                .use { it.readText() }

        } catch (e: Exception) {
            "Could not load server IP: ${e.message}"
        }
    }

private suspend fun fetchClientIp(apiBaseUrl: String): String =
    withContext(Dispatchers.IO) {

        val url = "${apiBaseUrl.trimEnd('/')}/client-ip"

        try {
            val connection =
                URL(url).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            connection.inputStream
                .bufferedReader()
                .use { it.readText() }

        } catch (e: Exception) {
            "Could not load client IP: ${e.message}"
        }
    }