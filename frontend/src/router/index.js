import { createRouter, createWebHistory } from "vue-router";
import TaskBoardView from "../views/TaskBoardView.vue";
import NotFound from "../views/NotFound.vue";
import TaskStatusView from "../views/TaskStatusView.vue";
import BoardView from "../views/BoardView.vue";
import Login from "../views/Login.vue";
import { useUserStore } from "../stores/user.js";
import { getBoardsById } from "../lib/fetchUtill.js";
import {isTokenValid, isNotDisable,refreshTokenAndReturn } from "../lib/utill.js";
import ManageCollab from "../views/ManageCollab.vue"
import Invitations from "../views/Invitations.vue"
const routes = [
  {
    path: "/",
    redirect: { name: "Login" },
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
  {
    path: "/board/:boardId/collab",
    name: "ManageCollab",
    component: ManageCollab,
  },
  {
    path: "/board/:boardId/invitations",
    name: "Invitations",
    component: Invitations,
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

const cachedGetBoardsById = async (boardId) => {
  const userStore = useUserStore();
  if (!boardId) return null;

  if (userStore.currentBoard?.id === boardId) {
    return userStore.currentBoard;
  }

  console.log("Fetching board by id:", boardId);
  const authToken = localStorage.getItem("authToken")
  const refresh_token = localStorage.getItem("refresh_token")
  let board;
  try{
    if(!authToken && refresh_token){
      await refreshTokenAndReturn()
    }
    else if(refresh_token){
      await isTokenValid(authToken);
    }
  }catch(error){
    console.log("error");
  }finally{
    board = await getBoardsById(boardId);
  }
  

  if (board === 401 || board === 403 || board === 404) {
    console.log(`Error: ${board}`);
    return board;
  } else {
    userStore.setCurrentBoard(board);
    return board;
  }
};

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const boardId = to.params.boardId;

  // ถ้าไปที่หน้า Login และมี token อยู่แล้ว ให้ไปที่หน้า board
  if (to.name === "Login" && userStore.authToken !== null) {
    return next({ name: "board" });
  }

  // ถ้าไม่มี boardId หรือกำลังไปที่หน้า NotFound อยู่แล้ว ให้ผ่านไปได้เลย
  if (!boardId || to.name === "TaskNotFound") {
    return next();
  }

  // ถ้ากำลังจะไปที่หน้าที่ต้องการ board
  if (
    [
      "task",
      "ManageStatus",
      "EditTask",
      "AddTask",
      "EditStatus",
      "AddStatus",
      "TaskDetail",
      "StatusDetail",
      "ManageCollab",
    ].includes(to.name)
  ) {
    const board = await cachedGetBoardsById(boardId);
    
    // if (
    //   userStore.authToken == null &&
    //   board === 404 &&
    //   !["task", "ManageStatus", "TaskDetail", "StatusDetail"].includes(to.name)
    // ) {
    //   next();
    // }

    if (board === 404 || board === 400 || board === 500) {
      return next({ name: "TaskNotFound", params: { boardId, page: "Board" } });
    }

    if (board === 401) {
      next();
    }

    if (board === 403) {
      return next({
        name: "TaskNotFound",
        params: { boardId, page: "authorizAccess" },
      });
    }

    if (typeof board === "object") {
      userStore.updatevIsibilityPublic(board.visibility === "PUBLIC");
      const oidByGet = board.owner.id;
      const oidByToken = userStore.authToken?.oid;
      const collaBorator = board.collaborators.find(c => c.oid === userStore.authToken?.oid);
      userStore.setCurrentBoard(board);
      userStore.updatevIsCanEdit(
        isNotDisable(userStore.visibilityPublic, oidByToken, oidByGet,collaBorator)
      );

      const isOwner = userStore.authToken?.oid === board.owner.id;
      const isEditAction = [
        "EditTask",
        "AddTask",
        "EditStatus",
        "AddStatus",
      ].includes(to.name);

      if (board.visibility === "PUBLIC" && !isOwner && ((collaBorator?.accessRight !== "WRITE" && !isOwner) && isEditAction) ) {
        return next({
          name: "TaskNotFound",
          params: { boardId, page: "authorizAccess" },
        });
      }

      if (
        board.visibility === "PRIVATE" &&
        (
          ((!isOwner && !collaBorator) || !userStore.authToken ) || ((collaBorator?.accessRight !== "WRITE" && !isOwner) && isEditAction)

        )
      ) {
        return next({ 
          name: "TaskNotFound",
          params: { boardId, page: "authorizAccess" },
        });
      }
    }
  }

  // ถ้าผ่านการตรวจสอบทั้งหมดแล้ว
  next();
});

export default router;
