<script setup>
import { project } from "../config";
</script>
<template>
  <RouterLink to="/" class="brand"
    ><span class="brand-mark">S</span
    ><span
      >{{ project.name
      }}<small>{{ project.shortName }} / WORKSPACE</small></span
    ></RouterLink
  >
</template>
