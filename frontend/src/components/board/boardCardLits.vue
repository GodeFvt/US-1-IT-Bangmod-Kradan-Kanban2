<script setup>
import boardCard from "./boardCard.vue";
import MoreActionIcon from "../icon/MoreActionIcon.vue";

defineEmits(["removeBoard" ,"openBoard"]);

const props = defineProps({
  allBoard: {
    type: Array,
    required: true,
  },
  showErrorMSG: {
    type: Boolean,
    default: false,
  },
  showLoading: {
    type: Boolean,
    default: true,
  },
});
</script>
 
<template>      
          <!-- Board Cards -->
          <boardCard
            v-for="board in allBoard"
            :key="board.id"
            customClass="relative group" 
          >
            <!-- Menu Icon -->
            <div
              class="itbkk-button-action dropdown dropdown-hover flex justify-center items-center absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <div
                :tabindex="board.id"
                role="button"
                class="p-1 rounded-lg hover:bg-gray-100 focus:ring-4 focus:outline-none focus:ring-gray-50"
              >
                <MoreActionIcon />
              </div>
              <ul
                :tabindex="board.id"
                class="dropdown-content menu px-1 py-1 shadow bg-base-100 rounded-box w-26 z-50 opacity-0 top-full group-hover:opacity-100 transition-opacity"
              >   
               <li>
                  <router-link
                    :to="{ name: 'ManageStatus', params: { boardId: board.id } }"
                  >
                    <span class="itbkk-button-edit cursor-pointer"> ManageStatus</span>
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="{ name: 'EditBoard', params: { boardId: board.id } }"
                  >
                    <span class="itbkk-button-edit cursor-pointer"> Edit </span>
                  </router-link>
                </li>
             
                <li>
                  <span
                    class="itbkk-button-delete cursor-pointer"
                    @click="$emit('removeBoard', board.id)"
                  >
                    Delete
                  </span>
                </li>
              </ul>
            </div>

            <template #header>
              <h3 class="slide-right text-lg font-semibold">{{ board.name }}</h3>
            </template>
            <template #content>
              <p class="slide-right text-sm text-muted-foreground">{{ board.description }}</p>
            </template>
            <button
              @click="$emit('openBoard',board.id )"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2 w-full">
              Open Board
            </button>
          </boardCard>

          <!-- Create New Board Card -->
       

     
</template>
 
<style scoped>

</style>