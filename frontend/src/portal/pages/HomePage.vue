<script setup>
import { project } from "../../config";
import { useAuth } from "../../stores/auth";
const auth = useAuth();
const features = [
  {
    n: "01",
    title: "整理每一条想法",
    description: "通过标题、描述与状态，让记录清楚有序。",
  },
  {
    n: "02",
    title: "找到需要的内容",
    description: "关键词搜索、状态筛选和分页，让查找更直接。",
  },
  {
    n: "03",
    title: "专注自己的空间",
    description: "在个人空间管理记录，在管理中心协同处理。",
  },
];
</script>
<template>
  <section class="hero">
    <div class="hero-copy">
      <p class="eyebrow"><span class="dot"></span> YOUR PROJECT STARTS HERE</p>
      <h1>让想法有处可放，<br />让项目从这里<span>开始。</span></h1>
      <p class="hero-description">
        {{ project.description
        }}<br />一个简单、有序的空间，记录、查找与管理都在这里。
      </p>
      <div class="hero-actions">
        <RouterLink
          class="button-link large"
          :to="auth.user ? '/portal/records' : '/login'"
          >{{ auth.user ? "进入我的记录" : "登录并开始" }}
          <span aria-hidden="true">↗</span></RouterLink
        ><span class="hero-note">简单记录，持续积累</span>
      </div>
    </div>
    <div class="hero-preview" aria-label="记录功能示意">
      <div class="preview-toolbar">
        <span class="mini-logo">S</span><span>我的工作空间</span
        ><span class="preview-pill">记录示意</span>
      </div>
      <div class="preview-body">
        <p class="eyebrow">A LITTLE ORGANIZATION</p>
        <h2>把灵感变成记录</h2>
        <div class="preview-record">
          <span class="record-square">01</span>
          <div>
            <strong>一个新的开始</strong>
            <p>写下标题，补充你的想法。</p>
          </div>
          <span class="status-pill">启用</span>
        </div>
        <div class="preview-record">
          <span class="record-square muted">02</span>
          <div>
            <strong>留给下一次的灵感</strong>
            <p>随时整理，按状态轻松查找。</p>
          </div>
          <span class="status-pill disabled">停用</span>
        </div>
        <div class="preview-bottom">
          <span>属于你的每一条记录</span><span aria-hidden="true">↗</span>
        </div>
      </div>
    </div>
  </section>
  <section class="feature-section">
    <div class="section-heading">
      <p class="eyebrow">CLEAR & SIMPLE</p>
      <h2>少一些重复，多一些专注。</h2>
    </div>
    <div class="feature-grid">
      <article v-for="item in features" :key="item.n">
        <span class="feature-number">{{ item.n }}</span>
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
      </article>
    </div>
  </section>
</template>
