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
        "live" -> Text("Live Updates Screen")
        "timer" -> Text("Timer Screen")
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
fun LoginScreen(onBackClick: () -> Unit) {
    var nameText by remember { mutableStateOf("Loading name...") }
    var serverTimeText by remember { mutableStateOf("Loading server time...")}
    var clientTimeText by remember { mutableStateOf("") }
    var serverIpText by remember { mutableStateOf("Loading server IP...") }
    var clientIpText by remember { mutableStateOf("Loading client IP...") }
    var googleUserName by remember { mutableStateOf("Not signed in") }
    var googleStatus by remember { mutableStateOf("") }

    val context = LocalContext.current
    val credentialManager = remember {
        CredentialManager.create(context)
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        nameText = fetchName(BuildConfig.API_BASE_URL)
        serverTimeText = fetchServerTime(BuildConfig.API_BASE_URL)
        serverIpText = fetchServerIp(BuildConfig.API_BASE_URL)
        clientIpText = fetchClientIp(BuildConfig.API_BASE_URL)
        clientTimeText = ZonedDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss 'GMT'xxx")
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(nameText)
        Text(serverTimeText)
        Text("Client time: $clientTimeText")
        Text(serverIpText)
        Text(clientIpText)

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