import { createRouter, createWebHistory } from "vue-router";
import TaskBoardView from "../views/TaskBoardView.vue";
import NotFound from "../views/NotFound.vue";
import TaskStatusView from "@/views/TaskStatusView.vue";
import Login from "../views/Login.vue"

const routes = [
  {
    path: "/",
    redirect: { name: "Login" },
  },
  {
    path: "/task",
    name: "task",
    component: TaskBoardView,
  },
  {
    path: "/task/:taskId",
    name: "TaskDetail",
    component: TaskBoardView,
  },
  {
    path: "/task/:taskId/edit",
    name: "EditTask",
    component: TaskBoardView,
  },
  {
    path: "/task/add",
    name: "AddTask",
    component: TaskBoardView,
  },
  {
    path: "/TaskNotFound:page",
    name: "TaskNotFound",
    component: NotFound,
  },
  {
    path: "/:catchAll(.*)",
    name: "404",
    component: NotFound,
  },
  {
    path: "/status",
    name: "ManageStatus",
    component: TaskStatusView,
  },
  {
    path: "/status/:statusId",
    name: "StatusDetail",
    component: TaskStatusView,
  },
  {
    path: "/status/:statusId/edit",
    name: "EditStatus",
    component: TaskStatusView,
  },
  {
    path: "/status/add",
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

router.beforeEach((to, from) => {
  if (to.name === "EditStatus" && to.params.statusId === "1") {
    return { name: "ManageStatus" };
  }
});

router.beforeEach((to, from,next) => {
  
  const isAuthenticated = !!localStorage.getItem('authToken');
  if (isAuthenticated === false && to.name !== 'Login') {
    next({ name: 'Login' });
  }
  else if(to.name ==='Login' && isAuthenticated ===true){
    next({ name: 'task' });
  }
  else {
    next();
  }
});

export default router;
