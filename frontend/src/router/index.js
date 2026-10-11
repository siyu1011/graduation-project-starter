import { createRouter, createWebHistory } from "vue-router";
import { useAuth } from "../stores/auth";
import PortalLayout from "../portal/layouts/PortalLayout.vue";
import AdminLayout from "../admin/layouts/AdminLayout.vue";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      component: PortalLayout,
      children: [
        { path: "", component: () => import("../portal/pages/HomePage.vue") },
        {
          path: "portal/records",
          component: () => import("../portal/pages/RecordsPage.vue"),
          meta: { auth: true },
        },
      ],
    },
    { path: "/login", component: () => import("../views/LoginPage.vue") },
    {
      path: "/admin",
      component: AdminLayout,
      meta: { auth: true, admin: true },
      children: [
        {
          path: "",
          component: () => import("../admin/pages/DashboardPage.vue"),
        },
        {
          path: "records",
          component: () => import("../admin/pages/RecordsPage.vue"),
        },
      ],
    },
    {
      path: "/403",
      component: () => import("../views/ErrorPage.vue"),
      props: { status: 403 },
    },
    {
      path: "/:pathMatch(.*)*",
      component: () => import("../views/ErrorPage.vue"),
      props: { status: 404 },
    },
  ],
});
router.beforeEach(async (to) => {
  const auth = useAuth();
  try {
    await auth.initialize();
  } catch {
    return false;
  }
  if (to.meta.auth && !auth.user)
    return { path: "/login", query: { redirect: to.fullPath } };
  if (to.meta.admin && auth.user?.role !== "ADMIN") return "/403";
  if (to.path === "/login" && auth.user)
    return auth.user.role === "ADMIN" ? "/admin" : "/portal/records";
});
