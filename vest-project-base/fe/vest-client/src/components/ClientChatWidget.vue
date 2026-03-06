<template>
  <div style="border:1px solid #ccc; width:320px; padding:12px;">
    <div style="font-weight:600; margin-bottom:8px;">Chat hỗ trợ</div>

    <div style="height:260px; overflow:auto; border:1px solid #eee; padding:8px; margin-bottom:8px;">
      <div v-for="m in messages" :key="m.id" style="margin:6px 0;">
        <div style="font-size:12px; opacity:0.7;">
          {{ m.senderType }} • {{ formatTime(m.createdAt) }}
        </div>
        <div>{{ m.content }}</div>
      </div>
    </div>

    <div style="display:flex; gap:6px;">
      <input
        v-model="input"
        @keyup.enter="send"
        placeholder="Nhập tin nhắn..."
        style="flex:1; padding:8px;"
      />
      <button @click="send" style="padding:8px 12px;">Gửi</button>
    </div>

    <div style="margin-top:8px; font-size:12px; opacity:0.7;">
      Conversation: {{ conversationId ?? "..." }} • WS: {{ wsStatus }}
    </div>
  </div>
</template>

<script>
import { createStompClient } from "@/chat/stomp";
import { getOrCreateConversation, getRecentMessages } from "@/chat/chatApi";

export default {
  name: "ClientChatWidget",
  data() {
    return {
      customerId: "CUST_001", // TODO: lấy từ auth/user hiện tại
      conversationId: null,
      messages: [],
      input: "",
      stomp: null,
      sub: null,
      wsStatus: "DISCONNECTED"
    };
  },

  async mounted() {
    // 1) get / create conversation
    const cv = await getOrCreateConversation(this.customerId);
    this.conversationId = cv.id;

    // 2) load history
    this.messages = await getRecentMessages(this.conversationId);

    // 3) connect ws
    this.stomp = createStompClient();

    this.stomp.onConnect = () => {
      this.wsStatus = "CONNECTED";
      this.sub = this.stomp.subscribe(
        `/topic/conversations/${this.conversationId}`,
        (frame) => {
          const msg = JSON.parse(frame.body);
          this.messages.push(msg);
          this.$nextTick(() => this.scrollBottom());
        }
      );
    };

    this.stomp.onStompError = () => (this.wsStatus = "ERROR");
    this.stomp.onWebSocketClose = () => (this.wsStatus = "DISCONNECTED");

    this.stomp.activate();
  },

  beforeUnmount() {
    if (this.sub) this.sub.unsubscribe();
    if (this.stomp) this.stomp.deactivate();
  },

  methods: {
    send() {
      const content = this.input.trim();
      if (!content || !this.conversationId || !this.stomp?.connected) return;

      this.stomp.publish({
        destination: "/app/chat.send",
        body: JSON.stringify({
          conversationId: this.conversationId,
          senderType: "CLIENT",
          senderId: this.customerId,
          content
        })
      });

      this.input = "";
    },

    scrollBottom() {
      const box = this.$el.querySelector("div[style*='height:260px']");
      if (box) box.scrollTop = box.scrollHeight;
    },

    formatTime(iso) {
      if (!iso) return "";
      const d = new Date(iso);
      return d.toLocaleTimeString();
    }
  }
};
</script>