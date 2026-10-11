import test from "node:test";
import assert from "node:assert/strict";
import { safeRedirect } from "../src/router/redirect.js";
test("登录返回地址只允许同源路径，用户不能返回管理端", () => {
  for (const value of [
    "https://evil.test",
    "//evil.test",
    "/\\evil.test",
    "/%2f%2fevil.test",
    "/login",
    "/admin",
    "/admin/records",
    "/%61dmin/records",
    "/a/../admin",
  ]) {
    assert.equal(safeRedirect(value, "USER"), "/portal/records", value);
  }
  assert.equal(
    safeRedirect("/portal/records?status=ENABLED", "USER"),
    "/portal/records?status=ENABLED",
  );
  assert.equal(safeRedirect("/admin/records", "ADMIN"), "/admin/records");
  assert.equal(safeRedirect(undefined, "ADMIN"), "/admin");
});
