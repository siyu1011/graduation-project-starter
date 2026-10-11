<script setup>
import BrandLogo from "../components/BrandLogo.vue";
defineProps({ status: { type: Number, default: 404 } });
</script>
<template>
  <div class="error-shell">
    <header><BrandLogo /></header>
    <main class="error-page">
      <p class="eyebrow">LET'S FIND YOUR WAY BACK</p>
      <span class="error-number">{{ status }}</span>
      <h1>
        {{ status === 403 ? "这个空间需要管理员权限" : "没有找到这个页面" }}
      </h1>
      <p>
        {{
          status === 403
            ? "你可以回到门户，继续管理自己的记录。"
            : "页面可能已移动，或链接地址有误。"
        }}
      </p>
      <RouterLink to="/" class="button-link"
        >返回首页 <span aria-hidden="true">↗</span></RouterLink
      >
    </main>
  </div>
</template>
