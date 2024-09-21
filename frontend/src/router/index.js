import { createRouter, createWebHistory } from "vue-router";
import TaskBoardView from "../views/TaskBoardView.vue";
import NotFound from "../views/NotFound.vue";
import TaskStatusView from "../views/TaskStatusView.vue";
import BoardView from "../views/BoardView.vue";
import Login from "../views/Login.vue";
import { useUserStore } from "../stores/user.js";

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

// router.beforeEach((to, from) => {
//   if (to.name === "EditStatus" && to.params.statusId === "1") {
//     return { name: "ManageStatus" };
//   }
// });

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  if (userStore.isAuthenticated === false && to.name !== "Login") {
    next({ name: "Login" });
  } else if (to.name === "Login" && userStore.isAuthenticated === true) {
    next({ name: "board" });
  } else {
    next();
  }
});

export default router;
