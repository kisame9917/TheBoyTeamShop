// src/chat/chatApi.js
import axios from "axios";

const API = "http://localhost:8080";

export async function getOrCreateConversation(customerId) {
  const res = await axios.post(`${API}/api/chat/conversation`, null, {
    params: { customerId },
  });
  return res.data; // { id: ... }
}

export async function getRecentMessages(conversationId) {
  const res = await axios.get(`${API}/api/chat/messages`, {
    params: { conversationId },
  });
  return res.data || [];
}