<script setup>
import {computed } from "vue";
import boardCard from "./boardCard.vue";
import MoreActionIcon from "../icon/MoreActionIcon.vue";
import DeleteIcon from "../icon/DeleteIcon.vue";
import { useUserStore } from "../../stores/user.js";
defineEmits(["removeBoard", "openBoard", "leaveBoard"]);

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
  boardType: {
    type: String,
    default: "personal",
  },
});
const userStore = useUserStore();
const indexCo = computed(() => {
  return props.allBoard.findIndex((board) =>
    board.collaborators.some((collaborator) => collaborator.oid === userStore.authToken.oid)
  );
});

</script>

<template>
  <!-- Board Cards -->
  <boardCard
    v-for="board in allBoard"
    :key="board.id"
    customClass="relative group"
    :class="
      boardType === 'personal' ? 'itbkk-personal-item' : 'itbkk-collab-item'
    "
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
      <h3 class="itbkk-board-name slide-right text-lg font-semibold">
        {{ board.name }}
      </h3>
    </template>
    <!-- <template #content>
              <p class="slide-right text-sm text-muted-foreground">{{ board.description }}</p>
            </template> -->
    <template #collab v-if="boardType === 'collab'">
      <div
        class="flex flex-col w-[75%] text-center mt-1 place-items-start ml-3 mb-2"
      >
        <p class="itbkk-owner-name text-sm font-medium">Board Owner :: {{ board.owner.username }}</p>
        <p class="itbkk-access-right text-sm font-medium">
          Access Rignt :: {{ board.collaborators[indexCo].access}}
        </p>
      </div>
    </template>
    <template #action>
      <div class="flex flex-row gap-1">
        <button
          @click="$emit('openBoard', board.id)"
          class="inline-flex items-center justify-center mt-3 rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2 w-full"
        >
          Open Board
        </button>
        <div
        @click="$emit('leaveBoard', board.id)"
        v-if="boardType === 'collab'"
        class="itbkk-leave-board inline-flex items-center justify-center cursor-pointer p-0 mt-3  "
      >
        <div
          class="tooltip tooltip-bottom tooltip-hover "
          data-tip="Leave Collab Board"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="32"
            height="32"
            viewBox="0 0 26 26"
          >
            <g fill="none">
              <defs>
                <mask id="pepiconsPencilLeaveCircleFilled0">
                  <path fill="#fff" d="M0 0h26v26H0z" />
                  <g fill="#000" fill-rule="evenodd" clip-rule="evenodd">
                    <path
                      d="M18.347 10.116a.5.5 0 0 1 .704.064l2.083 2.5a.5.5 0 0 1-.768.64l-2.084-2.5a.5.5 0 0 1 .064-.704"
                    />
                    <path
                      d="M18.347 15.884a.5.5 0 0 1-.065-.704l2.084-2.5a.5.5 0 1 1 .768.64l-2.083 2.5a.5.5 0 0 1-.704.064"
                    />
                    <path
                      d="M20.5 13a.5.5 0 0 1-.5.5h-7.5a.5.5 0 0 1 0-1H20a.5.5 0 0 1 .5.5m-14-7a.5.5 0 0 1 .5-.5h9a.5.5 0 0 1 0 1H7a.5.5 0 0 1-.5-.5m0 14a.5.5 0 0 1 .5-.5h9a.5.5 0 0 1 0 1H7a.5.5 0 0 1-.5-.5"
                    />
                    <path
                      d="M16 5.5a.5.5 0 0 1 .5.5v4a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m0 10a.5.5 0 0 1 .5.5v4a.5.5 0 0 1-1 0v-4a.5.5 0 0 1 .5-.5m-9-10a.5.5 0 0 1 .5.5v14a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5"
                    />
                  </g>
                </mask>
              </defs>
              <circle
                cx="13"
                cy="13"
                r="13"
                fill=""
                class="fill-rose-300 hover:fill-rose-400"
                mask="url(#pepiconsPencilLeaveCircleFilled0)"
              />
            </g>
          </svg>
        </div>
      </div>
      </div>

   
    </template>

    <template #visibility v-if="boardType === 'personal'">
      <div
        class="rounded-md w-[25%] text-center mt-1"
        :class="
          board.visibility === 'PRIVATE'
            ? 'bg-blue-500 text-white'
            : 'bg-green-600 text-white'
        "
      >
        <p class="itbkk-board-visibility text-sm font-semibold">
          {{ board.visibility }}
        </p>
      </div>
    </template>
  </boardCard>
  <!-- Create New Board Card -->
</template>

<style scoped></style>
