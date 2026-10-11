import axios from "axios";

export const http = axios.create({
  baseURL: "/api",
  withCredentials: true,
  timeout: 15000,
});
let csrf = null;
let csrfPromise = null;
export const sessionEvents = { unauthorized: null };
export function clearCsrf() {
  csrf = null;
}
export async function refreshCsrf() {
  csrf = null;
  if (!csrfPromise) {
    csrfPromise = http
      .get("/auth/csrf", { skipAuthError: true })
      .then((data) => {
        csrf = data;
        return data;
      })
      .finally(() => {
        csrfPromise = null;
      });
  }
  return csrfPromise;
}
http.interceptors.request.use(async (config) => {
  if (["post", "put", "patch", "delete"].includes(config.method)) {
    const token = csrf || (await refreshCsrf());
    config.headers.set(token.headerName, token.token);
  }
  return config;
});
http.interceptors.response.use(
  (response) => {
    if (response.data.code !== 0)
      return Promise.reject(new Error(response.data.message || "请求失败"));
    return response.data.data;
  },
  (error) => {
    const status = error.response?.status;
    if (status === 401 && !error.config?.skipAuthError) {
      clearCsrf();
      sessionEvents.unauthorized?.();
    }
    if (status === 403) clearCsrf(); // 下一次手动提交获取新token，不重放本次写请求。
    error.message =
      error.response?.data?.message ||
      (error.code === "ECONNABORTED"
        ? "请求超时，请稍后重试"
        : "网络连接失败，请检查服务后重试");
    return Promise.reject(error);
  },
);
