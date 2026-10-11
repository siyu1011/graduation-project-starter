<script setup>
import BrandLogo from "../../components/BrandLogo.vue";
import SessionMenu from "../../components/SessionMenu.vue";
</script>
<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <BrandLogo />
      <p class="sidebar-label">管理工作空间</p>
      <nav aria-label="管理导航">
        <RouterLink to="/admin" exact-active-class="selected"
          ><span aria-hidden="true">▦</span> 工作台</RouterLink
        ><RouterLink to="/admin/records" active-class="selected"
          ><span aria-hidden="true">≡</span> 记录管理</RouterLink
        >
      </nav>
      <div class="sidebar-bottom">
        <span class="dot"></span> 轻量 · 有序 · 可复用<RouterLink to="/"
          >返回门户 ↗</RouterLink
        >
      </div>
    </aside>
    <div class="admin-content">
      <header class="admin-header">
        <span class="breadcrumb"
          >管理中心 <span>/</span>
          {{ $route.path === "/admin" ? "工作台" : "记录管理" }}</span
        ><SessionMenu />
      </header>
      <main class="admin-main"><RouterView /></main>
      <footer class="footer">
        <span>STARTER / 管理工作空间</span><span>毕业设计 · 第一版</span>
      </footer>
    </div>
  </div>
</template>
