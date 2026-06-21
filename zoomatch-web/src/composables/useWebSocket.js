import { ref, onUnmounted } from 'vue'

export function useWebSocket(interlocutorIdGetter, tokenGetter) {
  const ws = ref(null)
  const connected = ref(false)
  const lastMessage = ref(null)

  let reconnectTimer = null
  let reconnectAttempts = 0
  const MAX_RECONNECT = 10

  function connect() {
    const interlocutorId = typeof interlocutorIdGetter === 'function' ? interlocutorIdGetter() : interlocutorIdGetter
    const token = typeof tokenGetter === 'function' ? tokenGetter() : tokenGetter
    if (!interlocutorId || !token) return

    if (ws.value) {
      ws.value.onclose = null
      ws.value.close()
    }

    const baseUrl = import.meta.env.VITE_WS_URL
    const url = `${baseUrl}/ws/chats/${interlocutorId}/?token=${token}`

    ws.value = new WebSocket(url)

    ws.value.onopen = () => {
      connected.value = true
      reconnectAttempts = 0
    }

    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        lastMessage.value = data
      } catch {
        // ignore
      }
    }

    ws.value.onclose = () => {
      connected.value = false
      scheduleReconnect()
    }

    ws.value.onerror = () => {
      ws.value?.close()
    }
  }

  function scheduleReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT) return
    const delay = Math.min(1000 * 2 ** reconnectAttempts, 30000)
    reconnectTimer = setTimeout(() => {
      reconnectAttempts++
      connect()
    }, delay)
  }

  function send(data) {
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify(data))
      return true
    }
    return false
  }

  function sendMessage(text, receiverId) {
    send({ type: 'message', text, receiver_id: receiverId })
  }

  function sendMediaMessage(messageId) {
    send({ type: 'media_message', message_id: messageId })
  }

  function sendRead(messageId) {
    send({ type: 'read', message_id: messageId })
  }

  function sendDelivered(messageId) {
    send({ type: 'delivered', message_id: messageId })
  }

  function disconnect() {
    clearTimeout(reconnectTimer)
    reconnectAttempts = MAX_RECONNECT
    ws.value?.close()
  }

  onUnmounted(disconnect)

  return {
    connected,
    lastMessage,
    connect,
    disconnect,
    send,
    sendMessage,
    sendMediaMessage,
    sendRead,
    sendDelivered,
  }
}
