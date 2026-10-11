<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import BrandLogo from "../components/BrandLogo.vue";
import { useAuth } from "../stores/auth";
import { safeRedirect } from "../router/redirect";
const auth = useAuth(),
  route = useRoute(),
  router = useRouter();
const form = reactive({ username: "", password: "" });
const busy = ref(false),
  error = ref(""),
  formRef = ref();
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};
async function login() {
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  busy.value = true;
  error.value = "";
  try {
    await auth.login(form.username, form.password);
    await router.replace(safeRedirect(route.query.redirect, auth.user.role));
  } catch (e) {
    error.value = e.message;
  } finally {
    busy.value = false;
  }
}
</script>
<template>
  <div class="login-shell">
    <header>
      <BrandLogo /><RouterLink to="/" class="back-link">返回首页 ↗</RouterLink>
    </header>
    <main class="login-main">
      <section class="login-copy">
        <p class="eyebrow">A SPACE FOR YOUR IDEAS</p>
        <h1>每一个项目，<br />都始于一个<span>想法。</span></h1>
        <p>登录你的工作空间，<br />从第一条记录，开始下一步。</p>
        <div class="login-decoration" aria-hidden="true">
          <span></span><span></span><span></span>
        </div>
      </section>
      <section class="login-card">
        <p class="eyebrow">WELCOME BACK</p>
        <h2>登录工作空间</h2>
        <p class="login-intro">使用你的账号继续。</p>
        <el-alert
          v-if="error"
          :title="error"
          type="error"
          :closable="false"
          show-icon
          class="form-error"
        /><el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          :disabled="busy"
          @submit.prevent="login"
          ><el-form-item label="用户名" prop="username"
            ><el-input
              id="login-username"
              v-model="form.username"
              size="large"
              placeholder="请输入用户名"
              autocomplete="username" /></el-form-item
          ><el-form-item label="密码" prop="password"
            ><el-input
              id="login-password"
              v-model="form.password"
              size="large"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password" /></el-form-item
          ><el-button
            class="login-submit"
            type="primary"
            native-type="submit"
            size="large"
            :loading="busy"
            >登录 <span aria-hidden="true">↗</span></el-button
          ></el-form
        >
        <p class="login-footnote">你的记录，将在这里继续。</p>
      </section>
    </main>
    <footer class="footer">
      <span>STARTER / 从记录开始</span><span>毕业设计 · 第一版</span>
    </footer>
  </div>
</template>
