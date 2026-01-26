import { useAuthStore } from "@/stores/auth";
import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/auth/LoginView.vue"),
    meta: { requiresAuth: false },
  },
  {
    path: "/admin/dashboard",
    name: "AdminDashboard",
    component: () => import("@/views/dashboard/AdminDashboardView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/mentor/dashboard",
    name: "MentorDashboard",
    component: () => import("@/views/dashboard/MentorDashboardView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/admin/mentors",
    name: "MentorList",
    component: () => import("@/views/mentor-management/MentorListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/mentors/:id/edit",
    name: "MentorEdit",
    component: () => import("@/views/mentor-management/MentorListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/mentors/:id/lock",
    name: "MentorLock",
    component: () => import("@/views/mentor-management/MentorListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/hrs",
    name: "HRList",
    component: () => import("@/views/hr-management/HRListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/interns",
    name: "InternList",
    component: () => import("@/views/intern-management/InternListView.vue"),
    component: () => import("@/views/intern-management/InternListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/interns/:id",
    name: "AdminInternDetail",
    component: () => import("@/views/intern-management/InternDetailView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/interns/:id/edit",
    name: "AdminInternEdit",
    component: () => import("@/views/intern-management/InternEditView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/profile",
    name: "AdminProfile",
    component: () => import("@/views/profile/ProfileView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/mentor/profile",
    name: "MentorProfile",
    component: () => import("@/views/profile/ProfileView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/mentor/my-interns",
    name: "MentorInternList",
    component: () => import("@/views/my-interns/MyInternListView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/mentor/my-interns/:id",
    name: "MentorInternDetail",
    component: () => import("@/views/intern-management/InternDetailView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/mentor/my-interns/:id/edit",
    name: "MentorInternEdit",
    component: () => import("@/views/intern-management/InternEditView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/admin/departments",
    name: "DepartmentList",
    component: () => import("@/views/system-config/DepartmentListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/positions",
    name: "PositionList",
    component: () => import("@/views/system-config/PositionListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/evaluation-criteria",
    name: "EvaluationCriteriaList",
    component: () => import("@/views/evaluation-criteria/EvaluationCriteriaListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/evaluation-criteria/create",
    name: "EvaluationCriteriaCreate",
    component: () => import("@/views/evaluation-criteria/EvaluationCriteriaDetailView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/evaluation-criteria/:id",
    name: "EvaluationCriteriaDetail",
    component: () => import("@/views/evaluation-criteria/EvaluationCriteriaDetailView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/admin/evaluation-sessions",
    name: "EvaluationSessionList",
    component: () => import("@/views/evaluation-session/EvaluationSessionListView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/mentor/evaluation-sessions",
    name: "MentorEvaluationSessionList",
    component: () => import("@/views/evaluation-session/EvaluationSessionListView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/admin/evaluation-sessions/create",
    name: "EvaluationSessionCreate",
    component: () => import("@/views/evaluation-session/EvaluationSessionCreateView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/mentor/evaluation-sessions/create",
    name: "MentorEvaluationSessionCreate",
    component: () => import("@/views/evaluation-session/EvaluationSessionCreateView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/admin/evaluation-sessions/:id",
    name: "EvaluationSessionDetail",
    component: () => import("@/views/evaluation-session/EvaluationSessionDetailView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/mentor/evaluation-sessions/:id",
    name: "MentorEvaluationSessionDetail",
    component: () => import("@/views/evaluation-session/EvaluationSessionDetailView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/admin/evaluation-sessions/:id/edit",
    name: "EvaluationSessionEdit",
    component: () => import("@/views/evaluation-session/EvaluationSessionEditView.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
  },
  {
    path: "/mentor/evaluation-sessions/:id/edit",
    name: "MentorEvaluationSessionEdit",
    component: () => import("@/views/evaluation-session/EvaluationSessionEditView.vue"),
    meta: { requiresAuth: true, role: "MENTOR" },
  },
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFoundView.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const isAuthenticated = authStore.isAuthenticated;
  const userRole = authStore.userRole;

  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: "Login" });
    return;
  }

  if (to.meta.role && to.meta.role !== userRole) {
    if (userRole === "ADMIN") {
      next({ name: "AdminDashboard" });
    } else if (userRole === "MENTOR") {
      next({ name: "MentorDashboard" });
    } else {
      next({ name: "Login" });
    }
    return;
  }

  if (to.name === "Login" && isAuthenticated) {
    if (userRole === "ADMIN") {
      next({ name: "AdminDashboard" });
    } else if (userRole === "MENTOR") {
      next({ name: "MentorDashboard" });
    } else {
      next();
    }
    return;
  }

  next();
});

export default router;
