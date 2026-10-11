<script setup>
import { onMounted } from "vue";
import { useAuth } from "./stores/auth";
import { router } from "./router";
const auth = useAuth();
async function retry() {
  try {
    await auth.initialize();
    await router.replace(location.pathname + location.search + location.hash);
  } catch {
    /* inline error */
  }
}
onMounted(() => {
  auth.initialize().catch(() => {});
});
</script>
<template>
  <RouterView v-if="auth.initialized" />
  <main v-else class="startup">
    <span class="brand-mark">S</span>
    <h1>
      {{
        auth.initializationError ? "暂时无法连接服务" : "正在准备你的工作空间"
      }}
    </h1>
    <p role="status">
      {{ auth.initializationError || "正在确认会话，请稍候…" }}
    </p>
    <el-button
      v-if="auth.initializationError"
      type="primary"
      :loading="auth.loading"
      @click="retry"
      >重新连接</el-button
    >
  </main>
</template>
