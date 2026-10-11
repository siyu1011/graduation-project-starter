<script setup>
import BrandLogo from "../../components/BrandLogo.vue";
import SessionMenu from "../../components/SessionMenu.vue";
import { useAuth } from "../../stores/auth";
const auth = useAuth();
</script>
<template>
  <div class="portal-shell">
    <header class="portal-header">
      <BrandLogo />
      <nav aria-label="门户导航">
        <RouterLink to="/">首页</RouterLink
        ><RouterLink v-if="auth.user" to="/portal/records">我的记录</RouterLink
        ><RouterLink v-if="auth.user?.role === 'ADMIN'" to="/admin"
          >管理中心</RouterLink
        >
      </nav>
      <SessionMenu />
    </header>
    <main class="portal-main"><RouterView /></main>
    <footer class="footer">
      <span>STARTER / 从记录开始</span><span>毕业设计 · 第一版</span>
    </footer>
  </div>
</template>
