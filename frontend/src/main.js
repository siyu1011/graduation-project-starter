import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import "element-plus/dist/index.css";
import "./assets/style.css";
import App from "./App.vue";
import { router } from "./router";
import { useAuth } from "./stores/auth";
import { sessionEvents } from "./utils/request";
const app = createApp(App);
app.use(createPinia());
sessionEvents.unauthorized = () => {
  useAuth().clear();
  const current = router.currentRoute.value;
  if (current.path !== "/login")
    router.replace({ path: "/login", query: { redirect: current.fullPath } });
};
app.use(router).use(ElementPlus, { locale: zhCn }).mount("#app");
