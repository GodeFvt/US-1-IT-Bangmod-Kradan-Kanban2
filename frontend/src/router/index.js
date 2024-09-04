import { createRouter, createWebHistory } from "vue-router";
import TaskBoardView from "../views/TaskBoardView.vue";
import NotFound from "../views/NotFound.vue";
import TaskStatusView from "../views/TaskStatusView.vue";
import BoardView from "../views/BoardView.vue";
import Login from "../views/Login.vue";
import VueJwtDecode from "vue-jwt-decode";


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

router.beforeEach((to, from) => {
  if (to.name === "EditStatus" && to.params.statusId === "1") {
    return { name: "ManageStatus" };
  }
});

router.beforeEach((to, from,next) => {
  const isAuthenticated = !!localStorage.getItem('authToken');
  let token = localStorage.getItem("authToken") || null;
  let tokenExp = false
    if (token) {
        const decodeToken = VueJwtDecode.decode(token);
        const currentTime = Math.floor(Date.now() / 1000); //แปลงจาก milisec to sec
        if (decodeToken.exp < currentTime) {
          localStorage.removeItem("authToken");
          tokenExp = true
        }
      }
  if (isAuthenticated === false && to.name !== 'Login') {
    next({ name: 'Login' });
  }
  else if(to.name ==='Login' && isAuthenticated ===true){
    //ถ้า user อยากไปหน้า login แต่ login แล้ว 
     localStorage.removeItem("authToken");
    next({ name: 'Login' });
  }
  // else if(to.name !=='Login' && tokenExp ===true){
  //   //ถ้า user อยากไปหน้า login แต่ login แล้ว 
  //   console.log("หมด");
  //    next({ name: 'Login' });
  //   //localStorage.removeItem("authToken");
    
  // }
   else {
    next();
  }
});



export default router;
