<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { useAuth } from "../stores/auth";
import { router } from "../router";
const auth = useAuth();
const busy = ref(false);
async function logout() {
  busy.value = true;
  try {
    await auth.logout();
    await router.push("/");
    ElMessage.success("已退出登录");
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    busy.value = false;
  }
}
</script>
<template>
  <div class="session-menu" v-if="auth.user">
    <span class="avatar">{{ auth.user.displayName.slice(0, 1) }}</span>
    <span class="user-name"
      >{{ auth.user.displayName
      }}<small>{{
        auth.user.role === "ADMIN" ? "管理员" : "普通用户"
      }}</small></span
    >
    <el-button plain :loading="busy" @click="logout">退出</el-button>
  </div>
  <RouterLink v-else class="button-link" to="/login"
    >登录工作空间 <span aria-hidden="true">↗</span></RouterLink
  >
</template>
