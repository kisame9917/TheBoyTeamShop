<template>

        </div>
      </div>
    </div>

      <input
        v-model="input"
        @keyup.enter="send"
        placeholder="Nhập tin nhắn..."
      />
    </div>
  </div>
</template>




    // 2) load history


          const msg = JSON.parse(frame.body);
        }





        destination: "/app/chat.send",
        body: JSON.stringify({
          senderType: "CLIENT",
      });



</script>