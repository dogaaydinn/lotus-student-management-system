<template>
  <div class="ai-chatbot-container">
    <!-- Chatbot Toggle Button -->
    <button
      v-if="!isOpen"
      @click="toggleChatbot"
      class="chatbot-toggle-btn"
      aria-label="Open AI Assistant"
    >
      <span class="bot-icon">🤖</span>
      <span class="pulse-ring"></span>
    </button>

    <!-- Chatbot Window -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chatbot-window">
        <!-- Header -->
        <div class="chatbot-header">
          <div class="header-content">
            <span class="bot-avatar">🤖</span>
            <div class="header-text">
              <h3>Lotus AI Assistant</h3>
              <span class="status">
                <span class="status-dot" :class="{ online: isOnline }"></span>
                {{ isOnline ? 'Online' : 'Offline' }}
              </span>
            </div>
          </div>
          <button @click="toggleChatbot" class="close-btn" aria-label="Close">&times;</button>
        </div>

        <!-- Messages Container -->
        <div ref="messagesContainer" class="messages-container">
          <!-- Welcome Message -->
          <div v-if="messages.length === 0" class="welcome-message">
            <h4>👋 Welcome!</h4>
            <p>I'm your AI assistant. I can help you with:</p>
            <ul>
              <li>📚 Internship applications</li>
              <li>📄 Document uploads</li>
              <li>🎓 Academic records</li>
              <li>💼 Job opportunities</li>
              <li>🔍 System navigation</li>
            </ul>
            <p class="tip">Try asking: "How do I apply for an internship?"</p>
          </div>

          <!-- Messages -->
          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message"
            :class="message.isUser ? 'user-message' : 'bot-message'"
          >
            <div class="message-avatar">
              {{ message.isUser ? '👤' : '🤖' }}
            </div>
            <div class="message-content">
              <p>{{ message.text }}</p>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
          </div>

          <!-- Typing Indicator -->
          <div v-if="isTyping" class="message bot-message">
            <div class="message-avatar">🤖</div>
            <div class="message-content typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div v-if="messages.length === 0" class="quick-actions">
          <button @click="sendQuickMessage('How do I apply for an internship?')" class="quick-btn">
            💼 Apply for Internship
          </button>
          <button @click="sendQuickMessage('How do I upload documents?')" class="quick-btn">
            📄 Upload Documents
          </button>
          <button @click="sendQuickMessage('How do I check my grades?')" class="quick-btn">
            🎓 Check Grades
          </button>
        </div>

        <!-- Input Area -->
        <div class="input-container">
          <form @submit.prevent="sendMessage">
            <input
              v-model="userInput"
              type="text"
              placeholder="Ask me anything..."
              class="chat-input"
              :disabled="isTyping"
              ref="chatInput"
            />
            <button
              type="submit"
              class="send-btn"
              :disabled="!userInput.trim() || isTyping"
              aria-label="Send message"
            >
              <span v-if="!isTyping">📤</span>
              <span v-else class="sending-spinner">⏳</span>
            </button>
          </form>
        </div>

        <!-- Powered By -->
        <div class="powered-by">
          <small>Powered by OpenAI GPT-4 | Lotus SMS v3.0</small>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import axios from 'axios'

// Reactive state
const isOpen = ref(false)
const isOnline = ref(true)
const isTyping = ref(false)
const userInput = ref('')
const messages = ref([])
const messagesContainer = ref(null)
const chatInput = ref(null)

// Toggle chatbot
const toggleChatbot = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => {
      chatInput.value?.focus()
    })
  }
}

// Send message
const sendMessage = async () => {
  const text = userInput.value.trim()
  if (!text || isTyping.value) return

  // Add user message
  messages.value.push({
    text,
    isUser: true,
    timestamp: new Date()
  })

  userInput.value = ''
  isTyping.value = true

  // Scroll to bottom
  scrollToBottom()

  try {
    // Call AI API
    const response = await axios.post('/api/v1/ai/chatbot/query', {
      query: text,
      context: 'general'
    })

    // Add bot response
    messages.value.push({
      text: response.data.response || 'I apologize, but I couldn\'t process that request.',
      isUser: false,
      timestamp: new Date()
    })
  } catch (error) {
    console.error('Chatbot error:', error)
    messages.value.push({
      text: '⚠️ Sorry, I\'m having trouble connecting right now. Please try again later.',
      isUser: false,
      timestamp: new Date()
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

// Send quick message
const sendQuickMessage = (message) => {
  userInput.value = message
  sendMessage()
}

// Format time
const formatTime = (date) => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// Scroll to bottom
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// Watch messages for auto-scroll
watch(() => messages.value.length, () => {
  scrollToBottom()
})
</script>

<style scoped>
.ai-chatbot-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

/* Toggle Button */
.chatbot-toggle-btn {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.chatbot-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.6);
}

.bot-icon {
  font-size: 32px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid rgba(102, 126, 234, 0.6);
  border-radius: 50%;
  animation: pulse 2s ease-out infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

/* Chatbot Window */
.chatbot-window {
  width: 400px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Header */
.chatbot-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-avatar {
  font-size: 32px;
}

.header-text h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  opacity: 0.9;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
}

.status-dot.online {
  background: #4CAF50;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 32px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
  line-height: 1;
}

.close-btn:hover {
  opacity: 1;
}

/* Messages Container */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

/* Welcome Message */
.welcome-message {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.welcome-message h4 {
  margin: 0 0 12px 0;
  color: #667eea;
  font-size: 20px;
}

.welcome-message p {
  margin: 8px 0;
  color: #666;
}

.welcome-message ul {
  margin: 12px 0;
  padding-left: 20px;
  color: #444;
}

.welcome-message ul li {
  margin: 6px 0;
}

.tip {
  background: #f0f4ff;
  padding: 12px;
  border-radius: 8px;
  color: #667eea;
  font-weight: 500;
  margin-top: 12px !important;
}

/* Messages */
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-avatar {
  font-size: 24px;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
}

.user-message .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-left: auto;
  border-bottom-right-radius: 4px;
}

.bot-message .message-content {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-content p {
  margin: 0;
  line-height: 1.5;
}

.message-time {
  font-size: 10px;
  opacity: 0.7;
  margin-top: 6px;
  display: block;
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-10px); }
}

/* Quick Actions */
.quick-actions {
  padding: 12px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: white;
  border-top: 1px solid #e0e0e0;
}

.quick-btn {
  background: white;
  border: 1px solid #667eea;
  color: #667eea;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  text-align: left;
}

.quick-btn:hover {
  background: #667eea;
  color: white;
}

/* Input Container */
.input-container {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
}

.input-container form {
  display: flex;
  gap: 8px;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: border 0.2s;
}

.chat-input:focus {
  border-color: #667eea;
}

.chat-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: scale(1);
}

.sending-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}

/* Powered By */
.powered-by {
  padding: 8px 20px;
  background: #f9f9f9;
  text-align: center;
  border-top: 1px solid #e0e0e0;
}

.powered-by small {
  color: #999;
  font-size: 11px;
}

/* Transitions */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* Responsive */
@media (max-width: 480px) {
  .chatbot-window {
    position: fixed;
    width: 100vw;
    height: 100vh;
    bottom: 0;
    right: 0;
    border-radius: 0;
  }

  .ai-chatbot-container {
    bottom: 16px;
    right: 16px;
  }

  .chatbot-toggle-btn {
    width: 56px;
    height: 56px;
  }

  .bot-icon {
    font-size: 28px;
  }
}
</style>
