import test from "node:test";
import assert from "node:assert/strict";
import { createPinia, setActivePinia } from "pinia";
import { useAuth } from "../src/stores/auth.js";
import { http, clearCsrf } from "../src/utils/request.js";

const user = { id: "2", username: "user", role: "USER" };

function setup({ failCsrfAt, failLogin = false } = {}) {
  setActivePinia(createPinia());
  clearCsrf();
  const calls = [];
  let csrfCalls = 0;
  http.defaults.adapter = async (config) => {
    calls.push(config.url);
    if (config.url === "/auth/csrf") {
      csrfCalls++;
      if (csrfCalls === failCsrfAt) throw new Error("offline");
    }
    if (config.url === "/auth/login" && failLogin) {
      const error = new Error("unauthorized");
      error.config = config;
      error.response = { status: 401, data: { message: "用户名或密码错误" } };
      throw error;
    }
    return {
      config,
      status: 200,
      data: { code: 0, data: config.url === "/auth/login" ? user :
        { headerName: "X-CSRF-TOKEN", token: "fresh-token" } },
    };
  };
  return { auth: useAuth(), calls };
}

test("登录后 CSRF 失败仍保留认证成功，下一次写请求重新获取 token", async () => {
  const { auth, calls } = setup({ failCsrfAt: 2 });
  const result = await auth.login("user", "password");
  assert.deepEqual(auth.user, user);
  assert.match(result.securityWarning, /登录成功.*安全校验初始化失败/);
  await http.post("/records", { title: "test" });
  assert.deepEqual(calls, ["/auth/csrf", "/auth/login", "/auth/csrf", "/auth/csrf", "/records"]);
});

test("错误密码保持未登录，不刷新登录后的 CSRF", async () => {
  const { auth, calls } = setup({ failLogin: true });
  await assert.rejects(auth.login("user", "wrong"), /用户名或密码错误/);
  assert.equal(auth.user, null);
  assert.deepEqual(calls, ["/auth/csrf", "/auth/login"]);
});

test("登录前 CSRF 失败不提交认证请求", async () => {
  const { auth, calls } = setup({ failCsrfAt: 1 });
  await assert.rejects(auth.login("user", "password"));
  assert.equal(auth.user, null);
  assert.deepEqual(calls, ["/auth/csrf"]);
});

test("登录及 CSRF 初始化均成功时没有警告", async () => {
  const { auth } = setup();
  assert.deepEqual(await auth.login("user", "password"), { securityWarning: "" });
  assert.deepEqual(auth.user, user);
});
