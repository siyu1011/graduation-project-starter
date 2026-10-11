import { defineStore } from "pinia";
import { ref } from "vue";
import { http, clearCsrf, refreshCsrf } from "../utils/request.js";

export const useAuth = defineStore("auth", () => {
  const user = ref(null);
  const initialized = ref(false);
  const loading = ref(false);
  const initializationError = ref("");
  let pending = null;
  function clear() {
    user.value = null;
    clearCsrf();
  }
  async function initialize() {
    if (initialized.value) return;
    if (pending) return pending;
    loading.value = true;
    initializationError.value = "";
    pending = (async () => {
      try {
        await refreshCsrf();
        try {
          user.value = await http.get("/auth/me", { skipAuthError: true });
        } catch (error) {
          if (error.response?.status !== 401) throw error;
          user.value = null;
        }
        initialized.value = true;
      } catch (error) {
        initializationError.value = error.message;
        throw error;
      } finally {
        loading.value = false;
        pending = null;
      }
    })();
    return pending;
  }
  async function login(username, password) {
    await refreshCsrf();
    user.value = await http.post(
      "/auth/login",
      new URLSearchParams({ username, password }),
      { skipAuthError: true },
    );
    try {
      await refreshCsrf();
      return { securityWarning: "" };
    } catch {
      return {
        securityWarning:
          "登录成功，但安全校验初始化失败。下次提交操作时会重新获取，请检查网络后重试。",
      };
    }
  }
  async function logout() {
    try {
      await http.post("/auth/logout", null, { skipAuthError: true });
    } catch (error) {
      if (error.response?.status !== 401) throw error;
    }
    clear();
    // 即使退出后的网络中断，也已退出；下次提交时重新获取token。
    try {
      await refreshCsrf();
    } catch {
      /* 下次请求允许手动重试 */
    }
  }
  return {
    user,
    initialized,
    loading,
    initializationError,
    initialize,
    login,
    logout,
    clear,
  };
});
