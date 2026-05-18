import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuthStore } from '@/stores/authStore'

export function useWebSocket(topics: string[], onMessage: (topic: string, body: any) => void) {
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        topics.forEach((topic) => {
          client.subscribe(topic, (message) => {
            try {
              const body = JSON.parse(message.body)
              onMessage(topic, body)
            } catch { /* ignore parse errors */ }
          })
        })
      },
      onStompError: (frame) => console.error('STOMP error:', frame.headers['message']),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [])

  return clientRef
}
