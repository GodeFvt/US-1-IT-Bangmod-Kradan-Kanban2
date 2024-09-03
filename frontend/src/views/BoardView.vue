<script setup>
import { ref, onMounted, watch, computed ,onUnmounted} from "vue";
import {
  deleteBoard,
  getTaskById,
  createBoard,
  updateBoard,
  getAllStatus,
  getFilteredTask,
  toggleLimitTask,
  getLimit,
  getAllBoards,
  getBoardsById,
} from "../lib/fetchUtill.js";
import { useRoute, useRouter } from "vue-router";
import AddButton from "../components/icon/AddButton.vue";
import { useUserStore } from "../stores/user.js";
import PopUp from "../components/modal/PopUp.vue";
import boardCardLits from "../components/board/boardCardLits.vue";
import boardDetail from "../components/board/boardDetail.vue";
import Toast from "../components/modal/Toasts.vue";
import ConfirmModal from "../components/modal/ConfirmModal.vue";
import AlertSquareIcon from "../components/icon/AlertSquareIcon.vue"

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

const allBoard=ref([])
const board = ref({});
const typeToast = ref("");
const messageToast = ref("");
const boardIdForDelete = ref("")


const timeCount = ref(3);

//show component
const showPopUp = ref(false);
const showBoardModal = ref(false);
const isEdit  = ref(false);
const showToast = ref(false);
const showDeleteModal = ref(false);

console.log(showBoardModal.value  );

// set value for allBoard
onMounted(async () => {
  const resBoard = await getAllBoards();
  if(resBoard === 401){
    tokenPass()
  } else if(resBoard.length===1) {
     //ไป task นั้นเลย /board/:boardId TaskBoardView
  }
  else {
    userStore.setAllBoard(resBoard);
    allBoard.value = userStore.boards;
    console.log(allBoard.value)
  }

});



//for 401 token
function tokenPass() { 
   showPopUp.value = true
    intervals.push(
    setTimeout(() => {
        router.push({ name: "Login" });
    }, 3000)  
    
  );
intervals.push(
    setInterval(() => {
      timeCount.value--;
    }, 1000)
  );
  }

onUnmounted(() => {
  clearAllInterval();
});
function clearAllInterval() {
  intervals.map((interval) => clearInterval(interval));
  intervals = [];
}
//end for 401 token


// add newBoard
watch(
  () => route.path,
  (newPath, oldPath) => {
    if (newPath === "/board/add") {
      ClickAdd();
    }
  },
  { immediate: true }
);

watch(
  () => route.params.boardId,
  async (newId, oldId) => {
    if (newId !== undefined) {
        if (route.path === `/board/${newId}/edit`) {     
          console.log("edit eiei");
          board.value =  allBoard.value.find((board) => board.id === newId)  
          console.log(board.value);

          showBoardModal.value = true;
          isEdit.value = true;
        } else if (route.path === `/board/${newId}/edit`) { 

        } else {
          isEdit.value = false;
        }
      }
      // showLoading.value = false;
  },
  { immediate: true }
);


function ClickAdd() {
  // showLoading.value = false;
  isEdit.value = true; 
  showBoardModal.value = true;
  console.log(  board.value );
  board.value = {
    name: "",
    description: "",
  };
}


// function createNewBoard (){

// }

async function addEditBoard(newBoard) {
  const indexToCheck = allBoard.value.findIndex(
    (board) => board.id === newBoard.id
  );

  if (indexToCheck !== -1 && indexToCheck !== undefined) {
    console.log(indexToCheck);
    console.log(board.value.id);
     await editBoard( board.value.id, newBoard);
    console.log("edit");
  } else {
    console.log("add");
    await addBoard(newBoard);
  }
}

async function addBoard(newBoard) {
  console.log("addBoard");
  console.log(newBoard);
  if (newBoard.name === null || newBoard.name === "") {
    typeToast.value = "warning";
    messageToast.value = `The name is required`;
    showToast.value = true;
  } else {
    newBoard.name = newBoard.name.trim();
    newBoard.description = newBoard.description?.trim();
    const res = await createBoard(newBoard);
    if (res === 422 || res === 400 || res === 500 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the board could not be added`;
    }  else if (res === 401) {
       // go login 
       tokenPass();
    } else {
      // if res.status = 200
      typeToast.value = "success";
      userStore.addBoard(res);
      messageToast.value = `The board has been added`;
    }
    showToast.value = true;
  }
}

async function editBoard(boardId,editedBoard) {
  const res = await updateBoard(boardId,editedBoard);
  if (res === 422 || res === 400 || res === 500 || res === 404) {
    typeToast.value = "warning";
    messageToast.value = `An error has occurred, the board does not exist`;
  }  else if (res === 401) {
       // go login 
       tokenPass();
    }  
  else {
    typeToast.value = "success";
    const indexToUpdate = allBoard.value.findIndex(
      (board) => board.id === editedBoard.id
    );
    userStore.editBoard(indexToUpdate, res);
    messageToast.value = `The board has been updated`;
  }
  showToast.value = true;
}


function closeStatus(action) {
  showBoardModal.value = action;
  // showAddModal.value = action;
  router.push({ name: "board" });
}


async function removeBoard(boardId ,  confirmDelete = false) {
  showDeleteModal.value = true
  console.log(boardId);
console.log(typeof boardId);
  if (typeof boardId==="string") {
  boardIdForDelete.value = boardId
  console.log(boardIdForDelete.value);

  console.log(showDeleteModal.value);
  console.log(boardId);
  board.value =  allBoard.value.find((board) => board.id === boardId) 
  console.log(confirmDelete,"before confirm");
  }
  // console.log(boardIdForDelete.value);
  if (confirmDelete) {
    console.log(boardIdForDelete.value);
    console.log(confirmDelete,"after confirm");
    const res = await deleteBoard(boardIdForDelete.value);
    if (res === 422 || res === 400 || res === 500 || res === 404) {
      typeToast.value = "warning";
      messageToast.value = `An error has occurred, the board could not be added`;

    }  else if (res === 401) {
       // go login 
       tokenPass();
    } else {
      // if res.status = 200
      typeToast.value = "success";
      userStore.deleteBoard(boardId);
      messageToast.value = `The board has been deleted`;
    }
    showDeleteModal.value = false
  }

}


function openBoard(boardId){
  console.log("go to task page");
  router.push({ name: "task" ,params : {boardId: boardId }});
}

</script>
<template>
  <div class="flex flex-col min-h-screen bg-background w-full">
    <main class="flex-1 py-6">
      <div class="container px-4 mx-auto">
        <h2 class="slide-right mb-6 text-2xl font-bold">Your Boards</h2>
        <div class="border-b border-gray-300 mb-12"></div>
        <div class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
     <router-link :to="{ name: 'AddBoard' }">
         <div 
            class="itbkk-button-create cursor-pointer rounded-lg border-2 border-dashed border-gray-300 bg-gray-100 text-card-foreground shadow-sm transition-shadow hover:shadow-md p-6 flex flex-col items-center justify-center h-full relative"
          >
            <AddButton/>
            <h3 class="text-lg font-semibold mb-2 text-gray-400">Create New Board</h3>
          </div>
        </router-link>
        <!-- board card list -->
         <boardCardLits
         :allBoard="allBoard"
         @removeBoard="removeBoard"
         @openBoard="openBoard"
         >
        </boardCardLits>
        </div>
    
  
      </div>
      
    </main>
    <div class="ml-auto">
    <Toast :toast="typeToast" @close-toast="showToast = false">
        <template #message>
          <span class="itbkk-message break-all">{{ messageToast }}</span>
        </template>
      </Toast>
    </div>
  </div>

  

   <boardDetail
   v-if="showBoardModal"
   @user-action="closeStatus"
   @addEdit="addEditBoard"
   :board="board"
   :isEdit="isEdit"
   >
   </boardDetail>  

   
   <ConfirmModal
          v-if="showDeleteModal"
          @user-action="showDeleteModal = false"
          @confirm="removeBoard"
          :index="allBoard.findIndex((board) => board.id === boardIdForDelete)"
          class="z-50"
          width="w-[42vh]"
        >
          <template #header>
            <div class="flex justify-center">
              <AlertSquareIcon class="w-16 h-16 opacity-40" />
            </div>
          </template>
          <template #body>
            <span class="itbkk-message">
              Do you want to delete this  "<span class="font-semibold">{{ board.name }}</span>"
            </span>
          </template>
        </ConfirmModal>

  <PopUp v-if="showPopUp">
          <template #message>  
            <p class="text-lg	 text-gray-700">
            Oops! something went wrong. please try Login again. 
            </p>
          </template>
          <template #button>    
            <router-link :to="{ name: 'Login' }">
            <button class="mt-4 bg-gray-800 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded">
              try Login again
            </button>
            </router-link> 
            <p class="text-sm	 text-gray-500">
                 Redirecting to home in <span class="text-red-700">{{ timeCount }}</span> seconds... </p>
          </template>
        </PopUp>

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
