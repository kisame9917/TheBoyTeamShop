import axios from "axios";
import { CHAT_API_BASE } from "@/config/chat";

export async function getOrCreateConversation(customerId) {
  const res = await axios.post(`${CHAT_API_BASE}/api/chat/conversation`, null, {
    params: { customerId }
  });
  return res.data; // { id, ... }
}

export async function getRecentMessages(conversationId) {
  const res = await axios.get(`${CHAT_API_BASE}/api/chat/messages`, {
    params: { conversationId }
  });
  return res.data; // list message
}