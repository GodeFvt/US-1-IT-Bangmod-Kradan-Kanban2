<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import BaseCard from "../components/modal/BaseCard.vue";
import AddButton from "../components/icon/AddButton.vue";
import MoreActionIcon from "../components/icon/MoreActionIcon.vue";

const isMenuOpen=ref(false)
const boards=ref([
{ id: 1, title: "Work Tasks", description: "Manage your work-related tasks" },
{ id: 1, title: "Work Tasks", description: "Manage your work-related tasks" },
{ id: 1, title: "Work Tasks", description: "Manage your work-related tasks" },
{ id: 1, title: "Work Tasks", description: "Manage your work-related tasks" },
{ id: 1, title: "Work Tasks", description: "Manage your work-related tasks" },

])
function createNewBoard (){

}

function openBoard(boardId){

}



</script>
<template>
  <div class="flex flex-col min-h-screen bg-background w-full">
    <main class="flex-1 py-6">
      <div class="container px-4 mx-auto">
        <h2 class="slide-right mb-6 text-2xl font-bold">Your Boards</h2>
        <div class="border-b border-gray-300 mb-12"></div>

        <div class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          
          <!-- Board Cards -->
          <BaseCard
            v-for="board in boards"
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
                    <span class="itbkk-button-edit cursor-pointer"> ManageStatus </span>
                  </router-link>
                </li>
                <li>
                  <span
                    class="itbkk-button-delete cursor-pointer"
                    @click="$emit('removeTask', index)"
                  >
                    Delete
                  </span>
                </li>
              </ul>
            </div>

            <template #header>
              <h3 class="slide-right text-lg font-semibold">{{ board.title }}</h3>
            </template>
            <template #content>
              <p class="slide-right text-sm text-muted-foreground">{{ board.description }}</p>
            </template>
            <button
              @click="openBoard(board.id)"
              class="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2 w-full">
              Open Board
            </button>
          </BaseCard>

          <!-- Create New Board Card -->
          <div 
            @click="createNewBoard"
            class="cursor-pointer rounded-lg border-2 border-dashed border-gray-300 bg-gray-100 text-card-foreground shadow-sm transition-shadow hover:shadow-md p-6 flex flex-col items-center justify-center h-full relative"
          >
            <AddButton/>
            <h3 class="text-lg font-semibold mb-2 text-gray-400">Create New Board</h3>
          </div>

        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.tracking-in-expand {
  -webkit-animation: tracking-in-expand 0.7s cubic-bezier(0.215, 0.61, 0.355, 1)
    both;
  animation: tracking-in-expand 0.7s cubic-bezier(0.215, 0.61, 0.355, 1) both;
}
.slide-right {
  -webkit-animation: slide-right 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
  animation: slide-right 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}
@-webkit-keyframes slide-right {
  0% {
    -webkit-transform: translateX(-100px);
    transform: translateX(-100px);
  }
  100% {
    -webkit-transform: translateX(0);
    transform: translateX(0);
  }
}
@keyframes slide-right {
  0% {
    -webkit-transform: translateX(-100px);
    transform: translateX(-100px);
  }
  100% {
    -webkit-transform: translateX(0);
    transform: translateX(0);
  }
}
@-webkit-keyframes tracking-in-expand {
  0% {
    letter-spacing: -0.5em;
    opacity: 0;
  }
  40% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}
@keyframes tracking-in-expand {
  0% {
    letter-spacing: -0.5em;
    opacity: 0;
  }
  40% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}
</style>
