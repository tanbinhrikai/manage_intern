<template>
    <div class="chat-container">
        <!-- Chat Header -->
        <div class="chat-header">
            <div class="header-content">
                <el-icon class="agent-icon"><ChatDotRound /></el-icon>
                <div class="header-text">
                    <h1>{{ t('agentChat.title') }}</h1>
                    <span class="subtitle">{{ t('agentChat.subtitle') }}</span>
                </div>
            </div>
        </div>

        <!-- Chat Messages Area -->
        <div class="chat-messages" ref="messagesContainer">
            <div v-if="messages.length === 0" class="welcome-message">
                <el-icon class="welcome-icon"><ChatDotRound /></el-icon>
                <h2>{{ t('agentChat.welcome.title') }}</h2>
                <p>{{ t('agentChat.welcome.description') }}</p>
                <div class="suggestions">
                    <p class="suggestions-title">{{ t('agentChat.welcome.suggestions') }}</p>
                    <div class="suggestion-chips">
                        <el-button 
                            v-for="suggestion in suggestions" 
                            :key="suggestion.key"
                            class="suggestion-chip"
                            @click="sendSuggestion(suggestion.text)"
                        >
                            {{ t(suggestion.key) }}
                        </el-button>
                    </div>
                </div>
            </div>

            <div 
                v-for="(message, index) in messages" 
                :key="index" 
                :class="['message', message.role]"
            >
                <div class="message-avatar">
                    <el-icon v-if="message.role === 'assistant'"><ChatDotRound /></el-icon>
                    <el-icon v-else><User /></el-icon>
                </div>
                <div class="message-content">
                    <div class="message-header">
                        <span class="message-sender">
                            {{ message.role === 'assistant' ? t('agentChat.agent') : t('agentChat.you') }}
                        </span>
                        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                    </div>
                    <div class="message-text" v-html="formatMessage(message.content)"></div>
                </div>
            </div>

            <!-- Typing Indicator -->
            <div v-if="isLoading" class="message assistant">
                <div class="message-avatar">
                    <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="message-content">
                    <div class="typing-indicator">
                        <span></span>
                        <span></span>
                        <span></span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Chat Input Area -->
        <div class="chat-input-container">
            <el-input
                v-model="inputMessage"
                :placeholder="t('agentChat.inputPlaceholder')"
                :disabled="isLoading"
                class="chat-input"
                @keyup.enter="sendMessageHandler"
                :rows="1"
                type="textarea"
                resize="none"
                autosize
            />
            <el-button 
                type="primary" 
                :icon="Promotion" 
                :loading="isLoading"
                :disabled="!inputMessage.trim() || isLoading"
                class="send-button"
                @click="sendMessageHandler"
            >
                {{ t('agentChat.send') }}
            </el-button>
        </div>
    </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ChatDotRound, User, Promotion } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/locales/locale'
import { sendMessage } from '@/api/agent'
import { ElMessage } from 'element-plus'

const localeStore = useLocaleStore()
const t = localeStore.t

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)

const suggestions = [
    { key: 'agentChat.suggestions.internsCount', text: 'How many interns do we have?' },
    { key: 'agentChat.suggestions.mentorsList', text: 'Show me the list of mentors' },
    { key: 'agentChat.suggestions.weeklyReports', text: 'What are the status of weekly reports?' },
    { key: 'agentChat.suggestions.evaluations', text: 'Tell me about intern evaluations' }
]

const scrollToBottom = async () => {
    await nextTick()
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
}

const formatTime = (timestamp) => {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const formatMessage = (content) => {
    // Basic markdown-like formatting
    if (!content) return ''
    return content
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
}

const sendSuggestion = (text) => {
    inputMessage.value = text
    sendMessageHandler()
}

const sendMessageHandler = async () => {
    const message = inputMessage.value.trim()
    if (!message || isLoading.value) return

    // Add user message
    messages.value.push({
        role: 'user',
        content: message,
        timestamp: new Date()
    })
    
    inputMessage.value = ''
    isLoading.value = true
    scrollToBottom()

    try {
        const response = await sendMessage(message)
        const data = response.data

        if (data.success && data.data) {
            messages.value.push({
                role: 'assistant',
                content: data.data.message,
                timestamp: data.data.timestamp || new Date()
            })
        } else {
            throw new Error(data.message || 'Unknown error')
        }
    } catch (error) {
        console.error('Chat error:', error)
        messages.value.push({
            role: 'assistant',
            content: t('agentChat.error'),
            timestamp: new Date()
        })
    } finally {
        isLoading.value = false
        scrollToBottom()
    }
}

onMounted(() => {
    scrollToBottom()
})
</script>

<style scoped>
.chat-container {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 60px);
    max-width: 900px;
    margin: 0 auto;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.chat-header {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    padding: 20px 24px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-content {
    display: flex;
    align-items: center;
    gap: 16px;
}

.agent-icon {
    font-size: 40px;
    color: white;
    background: rgba(255, 255, 255, 0.2);
    padding: 12px;
    border-radius: 12px;
}

.header-text h1 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
    color: white;
}

.header-text .subtitle {
    font-size: 0.875rem;
    color: rgba(255, 255, 255, 0.7);
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    background: #f8fafc;
    scroll-behavior: smooth;
}

.welcome-message {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 60px 20px;
    color: #64748b;
}

.welcome-icon {
    font-size: 64px;
    color: #667eea;
    margin-bottom: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.welcome-message h2 {
    margin: 0 0 12px 0;
    font-size: 1.5rem;
    font-weight: 600;
    color: #1e293b;
}

.welcome-message p {
    margin: 0 0 32px 0;
    font-size: 1rem;
    max-width: 400px;
}

.suggestions {
    width: 100%;
    max-width: 600px;
}

.suggestions-title {
    font-size: 0.875rem;
    font-weight: 500;
    color: #475569;
    margin-bottom: 16px;
}

.suggestion-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
}

.suggestion-chip {
    background: white;
    border: 1px solid #e2e8f0;
    color: #475569;
    border-radius: 20px;
    padding: 8px 16px;
    font-size: 0.875rem;
    transition: all 0.2s ease;
}

.suggestion-chip:hover {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-color: transparent;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.message {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.message.user {
    flex-direction: row-reverse;
}

.message-avatar {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.message.assistant .message-avatar {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.message.user .message-avatar {
    background: #10b981;
    color: white;
}

.message-content {
    max-width: 70%;
    background: white;
    border-radius: 16px;
    padding: 12px 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message.user .message-content {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
}

.message-sender {
    font-size: 0.75rem;
    font-weight: 600;
    color: #64748b;
}

.message.user .message-sender {
    color: rgba(255, 255, 255, 0.8);
}

.message-time {
    font-size: 0.7rem;
    color: #94a3b8;
}

.message.user .message-time {
    color: rgba(255, 255, 255, 0.6);
}

.message-text {
    font-size: 0.9375rem;
    line-height: 1.5;
    color: #334155;
    word-wrap: break-word;
}

.message.user .message-text {
    color: white;
}

.typing-indicator {
    display: flex;
    gap: 4px;
    padding: 8px 0;
}

.typing-indicator span {
    width: 8px;
    height: 8px;
    background: #94a3b8;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
    animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
    animation-delay: -0.16s;
}

@keyframes bounce {
    0%, 80%, 100% {
        transform: scale(0);
    }
    40% {
        transform: scale(1);
    }
}

.chat-input-container {
    display: flex;
    gap: 12px;
    padding: 20px 24px;
    background: white;
    border-top: 1px solid #e2e8f0;
}

.chat-input {
    flex: 1;
}

.chat-input :deep(.el-textarea__inner) {
    border-radius: 12px;
    border: 2px solid #e2e8f0;
    padding: 12px 16px;
    font-size: 0.9375rem;
    transition: all 0.2s ease;
    resize: none;
    min-height: 48px;
    max-height: 120px;
}

.chat-input :deep(.el-textarea__inner:focus) {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.send-button {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 12px;
    padding: 12px 24px;
    font-weight: 600;
    transition: all 0.2s ease;
    height: 48px;
}

.send-button:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* Scrollbar Styling */
.chat-messages::-webkit-scrollbar {
    width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
    background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
    background: #94a3b8;
}
</style>
