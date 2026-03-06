import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { CHAT_API_BASE } from "@/config/chat";

export function createStompClient() {
  const socket = new SockJS(`${CHAT_API_BASE}/ws`);

  const client = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 2000,
    debug: () => {} // tắt log
  });

  return client;
}