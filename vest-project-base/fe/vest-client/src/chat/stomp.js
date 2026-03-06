// src/chat/stomp.js
import { Client } from "@stomp/stompjs";

const API = "http://localhost:8080";

export function createStompClient() {
  const wsUrl = API.replace(/^http/, "ws") + "/ws"; // ws://localhost:8080/ws

  return new Client({
    brokerURL: wsUrl,
    reconnectDelay: 2000,
    debug: () => {},
  });
}