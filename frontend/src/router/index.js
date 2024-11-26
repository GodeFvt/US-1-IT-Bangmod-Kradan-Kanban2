import { createRouter, createWebHistory } from "vue-router";
import TaskBoardView from "../views/TaskBoardView.vue";
import NotFound from "../views/NotFound.vue";
import TaskStatusView from "../views/TaskStatusView.vue";
import BoardView from "../views/BoardView.vue";
import Login from "../views/Login.vue";
import { useUserStore } from "../stores/user.js";
import {useBoardStore } from "../stores/boards.js";
import { getBoardsById } from "../lib/fetchUtill.js";
import {isTokenValid, isNotDisable,refreshTokenAndReturn } from "../lib/utill.js";
import ManageCollabView from "../views/ManageCollabView.vue";
import Invitations from "../views/Invitations.vue";
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
    name: "ManageCollabView",
    component: ManageCollabView,
  },
  {
    path: "/board/:boardId/collab/invitations",
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
  const boardStore = useBoardStore();
  if (!boardId) return null;

  if (boardStore.currentBoard?.id === boardId) {
    return boardStore.currentBoard;
  }

  const authToken = localStorage.getItem("authToken")
  const refresh_token = localStorage.getItem("refresh_token")
  const graphToken = localStorage.getItem("graphAPI_token")
  let board;
  try{
    if(!authToken && (refresh_token || userStore.isMicroSoftLogin)){
      await refreshTokenAndReturn()
    }
    else if(refresh_token || userStore.isMicroSoftLogin){
      await isTokenValid(authToken);
    }
  }catch(error){
    console.log("error");
  }finally{
    board = await getBoardsById(boardId);
  }
  

  if (board === 401 || board === 403 || board === 404) {
    return board;
  } else {
    boardStore.setCurrentBoard(board);
    return board;
  }
};

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const boardStore = useBoardStore();
  const boardId = to.params.boardId;

  // ถ้าไปที่หน้า Login และมี token อยู่แล้ว ให้ไปที่หน้า board
  if (to.name === "Login" && userStore.encodeToken !== null) {
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
      "ManageCollabView",
     
    ].includes(to.name)
  ) {
    const board = await cachedGetBoardsById(boardId);
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
      boardStore.updatevIsibilityPublic(board.visibility === "PUBLIC");
      const oidByGet = board.owner.id;
      const oidByToken = userStore.authToken?.oid;
      const collaBorator = board.collaborators.find(c => c.oid === userStore.authToken?.oid);
      boardStore.setCurrentBoard(board);
      boardStore.updatevIsCanEdit(
        isNotDisable(boardStore.visibilityPublic, oidByToken, oidByGet,collaBorator)
      );

      const isOwner = userStore.authToken?.oid === board.owner.id;
      const isEditAction = [
        "EditTask",
        "AddTask",
        "EditStatus",
        "AddStatus",
      ].includes(to.name);

        //ไม่ใช่ทั้ง owner และ collaborator และไม่ได้เป็น write หรือมี pending และเข้าไปใน edit actionให้ 403
      if (board.visibility === "PUBLIC" && ((collaBorator?.accessRight !== "WRITE" || collaBorator?.isPending === true) && !isOwner) && isEditAction) {
        return next({
          name: "TaskNotFound",
          params: { boardId, page: "authorizAccess" },
        });
      }

      if (
        board.visibility === "PRIVATE" &&
        (
          ((!isOwner && !collaBorator) || !userStore.authToken || collaBorator?.isPending === true ) || 
          ((collaBorator?.accessRight !== "WRITE" && !isOwner) && isEditAction) || (collaBorator?.isPending === true && isEditAction)

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
