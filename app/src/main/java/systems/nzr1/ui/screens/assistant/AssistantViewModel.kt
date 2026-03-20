package systems.nzr1.ui.screens.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import systems.nzr1.data.local.dao.ChatDao
import systems.nzr1.data.local.entity.ChatMessageEntity
import systems.nzr1.data.preferences.UserPreferences
import systems.nzr1.domain.model.ChatMessage
import systems.nzr1.domain.model.MessageRole
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val prefs: UserPreferences,
    private val client: OkHttpClient,
) : ViewModel() {

    private val gson = Gson()

    val messages: StateFlow<List<ChatMessage>> = chatDao.getMessages()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apiKey: StateFlow<String> = prefs.aiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun sendMessage(userText: String) {
        val key = apiKey.value
        if (userText.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessage(content = userText, role = MessageRole.USER)
            chatDao.insert(userMsg.toEntity())
            _isLoading.value = true
            _error.value = null

            try {
                val history = messages.value.takeLast(20).map {
                    mapOf("role" to it.role.name.lowercase(), "content" to it.content)
                } + listOf(mapOf("role" to "user", "content" to userText))

                val body = gson.toJson(mapOf(
                    "model"      to "claude-3-haiku-20240307",
                    "max_tokens" to 1024,
                    "system"     to "You are NZRI, an advanced AI assistant integrated into the Systems device management app. Be concise, technical, and helpful. Use markdown for code.",
                    "messages"   to history,
                ))

                val request = Request.Builder()
                    .url("https://api.anthropic.com/v1/messages")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .header("x-api-key", key)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { resp ->
                    val respBody = resp.body?.string() ?: ""
                    if (resp.isSuccessful) {
                        val json    = gson.fromJson(respBody, Map::class.java)
                        val content = (json["content"] as? List<*>)?.firstOrNull()
                        val text    = (content as? Map<*, *>)?.get("text") as? String ?: "No response"
                        val aiMsg   = ChatMessage(content = text, role = MessageRole.ASSISTANT)
                        chatDao.insert(aiMsg.toEntity())
                    } else {
                        val aiMsg = ChatMessage(
                            content = "Error ${resp.code}: ${resp.message}",
                            role    = MessageRole.ASSISTANT,
                            isError = true,
                        )
                        chatDao.insert(aiMsg.toEntity())
                    }
                }
            } catch (e: Exception) {
                val errMsg = ChatMessage(
                    content = "Connection failed: ${e.message}",
                    role    = MessageRole.ASSISTANT,
                    isError = true,
                )
                chatDao.insert(errMsg.toEntity())
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() { viewModelScope.launch { chatDao.clearAll() } }
    fun saveApiKey(key: String) { viewModelScope.launch { prefs.setAiApiKey(key) } }

    private fun ChatMessage.toEntity() = ChatMessageEntity(
        id = id, content = content, role = role.name, timestamp = timestamp, isError = isError)
    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id, content = content, role = MessageRole.valueOf(role), timestamp = timestamp, isError = isError)
}
