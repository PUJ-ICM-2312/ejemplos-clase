package com.icm2510.appfirebase.presentation.chat // Asegúrate que el paquete sea correcto

import android.annotation.SuppressLint
import androidx.compose.foundation.background // Asegúrate de tener este import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.icm2510.appfirebase.data.model.Message
import com.icm2510.appfirebase.data.model.User // Importa el modelo User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.clickable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.icm2510.appfirebase.R
import androidx.compose.runtime.DisposableEffect // Para eventos de ciclo de vida
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

// Quita el @SuppressLint si ya no es necesario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // --- Recolecta todos los estados necesarios ---
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val messageText by chatViewModel.messageText.collectAsStateWithLifecycle()
    val error by chatViewModel.error.collectAsStateWithLifecycle()
    val userDetails by chatViewModel.userDetails.collectAsStateWithLifecycle() // Estado para nombres/emails

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    // --- NUEVO: Observar ciclo de vida para actualizar isChatScreenActive ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                chatViewModel.setChatScreenActive(true)
            } else if (event == Lifecycle.Event.ON_STOP) {
                chatViewModel.setChatScreenActive(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Opcional: También puedes ponerlo en false aquí si quieres ser muy explícito
            // chatViewModel.setChatScreenActive(false)
        }
    }
    // --- FIN NUEVO ---


    // Scroll automático
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Snackbar para errores
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            chatViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chat Room") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        // --- Input en el bottomBar para manejar insets ---
        bottomBar = {
            MessageInput(
                text = messageText,
                onTextChange = chatViewModel::onMessageTextChange,
                onSendClick = { chatViewModel.sendMessage() },
                // Aplica padding para barra de navegación y teclado
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            )
        }
    ) { scaffoldPadding -> // Recibe el padding del Scaffold

        // --- Columna principal aplica el padding del Scaffold ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding) // Evita solapamiento con TopAppBar
        ) {
            MessageList(
                messages = messages,
                currentUserId = currentUserId,
                userDetails = userDetails, // Pasa los detalles del usuario
                modifier = Modifier.weight(1f), // Ocupa espacio restante
                listState = listState
            )
        }
    }
}

@Composable
fun MessageList(
    messages: List<Message>,
    currentUserId: String?,
    userDetails: Map<String, User>, // Recibe detalles
    modifier: Modifier = Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp), // Padding lateral
        contentPadding = PaddingValues(vertical = 8.dp), // Padding superior/inferior interno
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.timestamp?.time ?: it.text }) { message ->
            MessageBubble(
                message = message,
                isSentByCurrentUser = message.senderId == currentUserId,
                senderDetails = userDetails[message.senderId] // Pasa el User o null
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isSentByCurrentUser: Boolean,
    senderDetails: User? // Recibe detalles (puede ser null)
) {
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    // --- Usa alineaciones 2D para el Box ---
    val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isSentByCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .align(alignment) // Alinea dentro del Box
                .widthIn(max = 300.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // --- Muestra Nombre/Email si NO es el usuario actual ---
                if (!isSentByCurrentUser) {
                    Text(
                        text = senderDetails?.name?.takeIf { it.isNotBlank() }
                            ?: senderDetails?.email?.takeIf { it.isNotBlank() }
                            ?: "User ${message.senderId.take(6)}...", // Fallback
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 2.dp)
                    )
                }
                // --- Fin Nombre/Email ---

                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp?.let { sdf.format(it) } ?: "...",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}


@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- Lottie Animation Setup ---
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sendmessage))
    var isPlaying by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = true
    )

    // Resetea isPlaying a false cuando la animación termina (progress llega a 1.0f)
    LaunchedEffect(progress) {
        if (progress == 1f) {
            isPlaying = false
        }
    }
    // --- Fin Lottie Setup ---

    val isSendEnabled = text.isNotBlank() // Determina si el botón/animación debe estar activo

    Surface(tonalElevation = 3.dp, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp), // Ajusta padding vertical si es necesario
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
            Spacer(modifier = Modifier.width(8.dp))


            Box(
                modifier = Modifier
                    .size(68.dp) // Tamaño similar a un IconButton
                    .clickable(
                        enabled = isSendEnabled, // Solo clickable si hay texto
                        onClick = {
                            if (!isPlaying) { // Evita iniciar múltiples envíos si la animación aún corre
                                onSendClick()     // Llama a la función para enviar mensaje
                                isPlaying = true // Inicia la animación
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    // Si está reproduciendo (isPlaying=true), usa el progreso calculado.
                    // Si no (isPlaying=false, porque terminó o es el estado inicial),
                    // fuerza el progreso a 0f (el primer frame).
                    progress = { if (isPlaying) progress else 0f },
                    modifier = Modifier.size(160.dp) // Tamaño de la animación dentro del Box
                )
            }

        }
    }
}