<script setup>
import { useAuth } from "../../stores/auth";
const auth = useAuth();
</script>
<template>
  <section class="page-heading">
    <p class="eyebrow">WORKSPACE OVERVIEW</p>
    <h1>欢迎回来，{{ auth.user.displayName }}。</h1>
    <p>从这里进入管理空间，查看与整理所有示例记录。</p>
  </section>
  <section class="dashboard-card">
    <div>
      <span class="section-index">01 / RECORDS</span>
      <h2>让每一条记录井然有序</h2>
      <p>查找记录、查看归属，并完成新增、编辑与删除。</p>
      <RouterLink to="/admin/records" class="button-link"
        >进入记录管理 <span aria-hidden="true">↗</span></RouterLink
      >
    </div>
    <div class="dashboard-emblem" aria-hidden="true">
      R<span>RECORD SPACE</span>
    </div>
  </section>
  <div class="dashboard-notes">
    <article>
      <p class="eyebrow">YOUR IDENTITY</p>
      <h3>{{ auth.user.username }}</h3>
      <p>管理员 · 可管理全部记录</p>
    </article>
    <article>
      <p class="eyebrow">PERSONAL SPACE</p>
      <h3>也可以整理自己的记录</h3>
      <RouterLink to="/portal/records">进入我的记录 ↗</RouterLink>
    </article>
  </div>
</template>
