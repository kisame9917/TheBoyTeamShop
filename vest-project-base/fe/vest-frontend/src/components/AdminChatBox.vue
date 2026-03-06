<template>
  <div style="border:1px solid #ccc; width:520px; padding:12px;">
    <div style="display:flex; gap:8px; align-items:center; margin-bottom:8px;">
      <div style="font-weight:600;">Admin Chat</div>
      <input v-model="convInput" placeholder="conversationId" style="padding:6px; width:140px;" />
      <button @click="join" style="padding:6px 10px;">Join</button>
      <span style="font-size:12px; opacity:0.7;">WS: {{ wsStatus }}</span>
    </div>

    <div style="height:320px; overflow:auto; border:1px solid #eee; padding:8px; margin-bottom:8px;">
      <div v-for="m in messages" :key="m.id" style="margin:6px 0;">
        <div style="font-size:12px; opacity:0.7;">
          {{ m.senderType }} • {{ formatTime(m.createdAt) }}
        </div>
        <div>{{ m.content }}</div>
      </div>
    </div>

    <div style="display:flex; gap:6px;">
      <input v-model="input" @keyup.enter="send" placeholder="Nhập trả lời..." style="flex:1; padding:8px;" />
      <button @click="send" style="padding:8px 12px;">Gửi</button>
    </div>
  </div>
</template>

<script>
import { createStompClient } from "@/chat/stomp";
import { getRecentMessages } from "@/chat/chatApi";

export default {
  name: "AdminChatBox",
  data() {
    return {
      adminId: "ADMIN_001",
      convInput: "",
      conversationId: null,
      messages: [],
      input: "",
      stomp: null,
      sub: null,
      wsStatus: "DISCONNECTED"
    };
  },

  mounted() {
    this.stomp = createStompClient();

    this.stomp.onConnect = () => (this.wsStatus = "CONNECTED");
    this.stomp.onStompError = () => (this.wsStatus = "ERROR");
    this.stomp.onWebSocketClose = () => (this.wsStatus = "DISCONNECTED");

    this.stomp.activate();
  },

  beforeUnmount() {
    if (this.sub) this.sub.unsubscribe();
    if (this.stomp) this.stomp.deactivate();
  },

  methods: {
    async join() {
      const id = Number(this.convInput);
      if (!id || !this.stomp?.connected) return;

      // unsubscribe old
      if (this.sub) this.sub.unsubscribe();

      this.conversationId = id;
      this.messages = await getRecentMessages(id);

      this.sub = this.stomp.subscribe(`/topic/conversations/${id}`, (frame) => {
        const msg = JSON.parse(frame.body);
        this.messages.push(msg);
      });
    },

    send() {
      const content = this.input.trim();
      if (!content || !this.conversationId || !this.stomp?.connected) return;

      this.stomp.publish({
        destination: "/app/chat.send",
        body: JSON.stringify({
          conversationId: this.conversationId,
          senderType: "ADMIN",
          senderId: this.adminId,
          content
        })
      });

      this.input = "";
    },

    formatTime(iso) {
      if (!iso) return "";
      return new Date(iso).toLocaleTimeString();
    }
  }
};
</script>