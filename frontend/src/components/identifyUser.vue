<script setup>
import { ref, onMounted,onBeforeUnmount } from "vue";
import { getBoardsById } from "../lib/fetchUtill.js";
import { useUserStore } from "../stores/user.js";
import { useRoute, useRouter } from "vue-router";

const props = defineProps({
  boardId: {
    type: String,
    required: true,
  },
});
const userStore = useUserStore();
const router = useRouter();

async function IsOwner (boardId) {
    const oidByToken = userStore.authToken.oid;
    const res = await getBoardsById(boardId);
    if (res === 404 || res === 400 || res === 500) {
        router.push({ name: "TaskNotFound", params: { page: "Board" } });
      }
      else if(res === 401){
        return
      }
      else{
       
       const oidByGet = res.owner.id;
       if(oidByGet !== oidByToken){
        router.push({ name: "TaskNotFound", params: { page: "Board" } });
       }
       
      }
    
}


onMounted(() => {  
IsOwner(props.boardId);
});
</script>
<template>

</template>
<style scoped>

</style>