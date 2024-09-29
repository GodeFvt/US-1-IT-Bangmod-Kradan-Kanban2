import { createRouter, createWebHistory } from "vue-router";
import TaskBoardView from "../views/TaskBoardView.vue";
import NotFound from "../views/NotFound.vue";
import TaskStatusView from "../views/TaskStatusView.vue";
import BoardView from "../views/BoardView.vue";
import Login from "../views/Login.vue";
import { useUserStore } from "../stores/user.js";
import { getBoardsById } from "../lib/fetchUtill.js";
import { useStatusStore } from "../stores/statuses.js";
import { isNotDisable } from "../lib/utill.js";

const routes = [
  {
    path: "/",
    redirect: { name: "board" },
  },
  {
    path: "/board",
    name: "board",
    component: BoardView,
  },
  {
    path: "/board/add",
    name: "AddBoard",
    component: BoardView,
  },
  {
    path: "/board/:boardId/edit",
    name: "EditBoard",
    component: BoardView,
  },
  {
    path: "/board/:boardId",
    name: "task",
    component: TaskBoardView,
  },
  {
    path: "/board/:boardId/task/:taskId",
    name: "TaskDetail",
    component: TaskBoardView,
  },
  {
    path: "/board/:boardId/task/:taskId/edit",
    name: "EditTask",
    component: TaskBoardView,
  },
  {
    path: "/board/:boardId/task/add",
    name: "AddTask",
    component: TaskBoardView,
  },
  {
    path: "/board/:boardId/TaskNotFound:page",
    name: "TaskNotFound",
    component: NotFound,
  },
  {
    path: "/:catchAll(.*)",
    name: "404",
    component: NotFound,
  },
  {
    path: "/board/:boardId/status",
    name: "ManageStatus",
    component: TaskStatusView,
  },
  {
    path: "/board/:boardId/status/:statusId",
    name: "StatusDetail",
    component: TaskStatusView,
  },
  {
    path: "/board/:boardId/status/:statusId/edit",
    name: "EditStatus",
    component: TaskStatusView,
  },
  {
    path: "/board/:boardId/status/add",
    name: "AddStatus",
    component: TaskStatusView,
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

const boardCache = new Map();
const cachedGetBoardsById = async (boardId) => {
  const userStore = useUserStore();
  if (boardId === undefined) {
    return;
  }
  if (userStore.currentBoard.id === boardId) {
    return userStore.currentBoard;
  }
  // if (boardCache.has(boardId)) {
  //   return boardCache.get(boardId);
  // }
  console.log("getBo by id");
  const board = await getBoardsById(boardId);
  userStore.setCurrentBoard(board)
  // boardCache.set(boardId, board);
  return board;
};

//ยังไม่เสร็จดี
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const boardId = to.params.boardId;
  let board = await cachedGetBoardsById(boardId);
  
  if (!board) {
    board = 404;
  }
  
  console.log("board", board);
  console.log("to.name", to.name);

  if ((to.name === "task" || to.name === "ManageStatus") && to.params.boardId) {
    if (typeof board === "object") {
      userStore.updatevIsibilityPublic(
        board.visibility === "PUBLIC" ? true : false
      );
      const oidByGet = board.owner.id;
      const oidByToken = userStore.authToken?.oid;
      userStore.setCurrentBoard(board);
      userStore.updatevIsCanEdit(
        isNotDisable(userStore.visibilityPublic, oidByToken, oidByGet)
      );
    }
    if (board === 404 || board === 400 || board === 500) {
      next({ name: "TaskNotFound", params: { boardId, page: "Board" } });
    } else if (board === 403) {
      console.log("private , ไม่ใช่เจ้าของ ");
      next({
        name: "TaskNotFound",
        params: { boardId, page: "authorizAccess" },
      });
    } else {
      console.log("next1");
      next();
    }
  } else if (board?.visibility === "PUBLIC") {
    console.log("PUBLIC");
    console.log(userStore.isCanEdit); // จะเป็น true เปิดจากเจ้าของ board มาตัวเอง
    console.log(userStore.authToken?.oid);
    console.log(userStore.authToken?.oid === board.owner.id);
    if (
      userStore.authToken?.oid !== board.owner.id &&
      (to.name === "EditTask" ||
        to.name === "AddTask" ||
        to.name === "EditStatus" ||
        to.name === "AddStatus")
    ) {
      console.log("authorizAccess");
      next({
        name: "TaskNotFound",
        params: { boardId, page: "authorizAccess" },
      });
    } else {
      console.log("next2");
      next();
    }
  } else if (board?.visibility === "PRIVATE") {
    console.log("PRIVATE");
    // ตรงนี้ต้องเช็คว่าเป็นเจ้าของ board ไหม
    if (
      userStore.authToken?.oid === board.owner.id &&
      userStore.authToken !== null
    ) {
      console.log("private , เป็นเจ้าของ , login");
      next();
    } else {
      console.log("private , ไม่ใช่เจ้าของ ");
      next({
        name: "TaskNotFound",
        params: { boardId, page: "authorizAccess" },
      });
    }
  } else if (
    to.name === "EditTask" ||
    to.name === "AddTask" ||
    to.name === "EditStatus" ||
    to.name === "AddStatus"
  ) {
    console.log("authorizAccess");
    if (board === 404 || board === 400 || board === 500) {
      next({ name: "TaskNotFound", params: { boardId, page: "Board" } });
      return;
    } else if (board === 403) {
      console.log("private , ไม่ใช่เจ้าของdddd ");
      next({
        name: "TaskNotFound",
        params: { boardId, page: "authorizAccess" },
      });
      return;
    } else {
      console.log("next1");
      next();
    }
  } else {
    if (to.name === "Login" && userStore.authToken !== null) {
      console.log("to.name ===  && userStore.authToken !== null");
      next({ name: "board" });
    } else {
      console.log("next3");
      next();
    }
  }
});

export default router;
