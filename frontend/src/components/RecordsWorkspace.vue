<script setup>
import { reactive, ref, computed, onMounted, onBeforeUnmount } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { recordApi } from "../api/records";
import RecordEditor from "./RecordEditor.vue";
const props = defineProps({ admin: Boolean });
const api = recordApi(props.admin);
const filters = reactive({ keyword: "", status: "" });
const applied = reactive({ keyword: "", status: "" });
const page = ref(1),
  size = ref(10),
  total = ref(0),
  records = ref([]);
const loading = ref(false),
  error = ref(""),
  editing = ref(false),
  editRecord = ref(null);
const saving = ref(false),
  saveError = ref(""),
  detailOpen = ref(false),
  detail = ref(null);
const detailLoading = ref(false),
  detailError = ref(""),
  deleting = ref(null);
const pageCount = computed(() =>
  Math.max(1, Math.ceil(total.value / size.value)),
);
let querySequence = 0;
onBeforeUnmount(() => {
  querySequence++;
});
async function load() {
  const sequence = ++querySequence;
  loading.value = true;
  error.value = "";
  try {
    const result = await api.list({
      page: page.value,
      size: size.value,
      keyword: applied.keyword || undefined,
      status: applied.status || undefined,
    });
    if (sequence !== querySequence) return;
    total.value = result.total;
    const maxPage = Math.max(1, Math.ceil(result.total / size.value));
    if (page.value > maxPage) {
      page.value = maxPage;
      return await load();
    }
    records.value = result.records;
  } catch (e) {
    if (sequence === querySequence) {
      records.value = [];
      error.value = e.message;
    }
  } finally {
    if (sequence === querySequence) loading.value = false;
  }
}
function search() {
  Object.assign(applied, filters);
  page.value = 1;
  load();
}
function reset() {
  Object.assign(filters, { keyword: "", status: "" });
  search();
}
function changeSize() {
  page.value = 1;
  load();
}
function create() {
  editRecord.value = null;
  saveError.value = "";
  editing.value = true;
}
async function edit(row) {
  try {
    editRecord.value = await api.detail(row.id);
    saveError.value = "";
    editing.value = true;
  } catch (e) {
    ElMessage.error(e.message);
  }
}
async function save(data) {
  saving.value = true;
  saveError.value = "";
  try {
    if (editRecord.value) await api.update(editRecord.value.id, data);
    else await api.create(data);
    editing.value = false;
    ElMessage.success("记录已保存");
    await load();
  } catch (e) {
    saveError.value = e.message;
  } finally {
    saving.value = false;
  }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(
      "删除后无法恢复，确定删除“" + row.title + "”吗？",
      "删除记录",
      {
        confirmButtonText: "确认删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );
  } catch {
    return;
  }
  deleting.value = row.id;
  try {
    await api.remove(row.id);
    ElMessage.success("记录已删除");
    await load();
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    deleting.value = null;
  }
}
async function showDetail(row) {
  detailOpen.value = true;
  detailLoading.value = true;
  detailError.value = "";
  detail.value = null;
  try {
    detail.value = await api.detail(row.id);
  } catch (e) {
    detailError.value = e.message;
  } finally {
    detailLoading.value = false;
  }
}
function time(value) {
  return value?.replace("T", " ") || "—";
}
onMounted(load);
</script>
<template>
  <section class="page-heading records-heading">
    <div>
      <p class="eyebrow">
        {{ admin ? "RECORD MANAGEMENT" : "PERSONAL WORKSPACE" }}
      </p>
      <h1>
        {{ admin ? "记录管理" : "我的记录" }}<span class="heading-dot">.</span>
      </h1>
      <p>
        {{
          admin
            ? "查看所有记录与归属，让每一条内容清楚有序。"
            : "记下新的想法，随时查找、整理与更新。"
        }}
      </p>
    </div>
    <el-button type="primary" size="large" @click="create"
      ><span aria-hidden="true" class="plus">＋</span> 新增记录</el-button
    >
  </section>
  <section class="records-panel">
    <form class="filters" @submit.prevent="search">
      <div class="filter-field">
        <label for="record-keyword">关键词</label
        ><el-input
          id="record-keyword"
          v-model="filters.keyword"
          maxlength="100"
          clearable
          placeholder="搜索记录标题"
        />
      </div>
      <div class="filter-field status-filter">
        <label for="record-status">记录状态</label
        ><el-select
          id="record-status"
          v-model="filters.status"
          placeholder="全部状态"
          clearable
          ><el-option label="启用" value="ENABLED" /><el-option
            label="停用"
            value="DISABLED"
        /></el-select>
      </div>
      <div class="filter-actions">
        <el-button type="primary" native-type="submit" :loading="loading"
          >查询</el-button
        ><el-button @click="reset">重置</el-button>
      </div>
    </form>
    <div class="table-caption">
      <span
        ><strong>{{ admin ? "全部记录" : "记录列表" }}</strong
        ><span class="count-badge">{{ total }}</span></span
      ><span class="muted-text">按创建时间排序</span>
    </div>
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
      class="list-error"
      ><template #default
        ><el-button size="small" @click="load">重新加载</el-button></template
      ></el-alert
    >
    <div class="table-scroll" v-loading="loading">
      <el-table
        :data="records"
        style="width: 100%"
        :row-key="(row) => row.id"
        :empty-text="
          error ? '记录加载失败' : '还没有记录，点击「新增记录」开始'
        "
      >
        <el-table-column label="记录标题" min-width="260"
          ><template #default="{ row }"
            ><button class="title-button" @click="showDetail(row)">
              {{ row.title }}
            </button>
            <p class="row-description">
              {{ row.description || "暂无描述" }}
            </p></template
          ></el-table-column
        >
        <el-table-column label="状态" width="100"
          ><template #default="{ row }"
            ><span
              class="status-pill"
              :class="{ disabled: row.status === 'DISABLED' }"
              ><span class="dot"></span
              >{{ row.status === "ENABLED" ? "启用" : "停用" }}</span
            ></template
          ></el-table-column
        >
        <el-table-column
          v-if="admin"
          prop="ownerUsername"
          label="归属账号"
          min-width="120"
        />
        <el-table-column label="创建时间" min-width="180"
          ><template #default="{ row }"
            ><span class="table-time">{{ time(row.createdAt) }}</span></template
          ></el-table-column
        >
        <el-table-column label="操作" width="192"
          ><template #default="{ row }"
            ><div class="row-actions">
              <el-button link type="primary" @click="showDetail(row)"
                >详情</el-button
              ><el-button link type="primary" @click="edit(row)">编辑</el-button
              ><el-button
                link
                type="danger"
                :loading="deleting === row.id"
                :disabled="deleting !== null"
                @click="remove(row)"
                >删除</el-button
              >
            </div></template
          ></el-table-column
        >
      </el-table>
    </div>
    <div class="pagination-row">
      <span class="muted-text"
        >共 {{ total }} 条记录 · 第 {{ page }} / {{ pageCount }} 页</span
      ><el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="sizes, prev, pager, next"
        :disabled="loading"
        @size-change="changeSize"
        @current-change="load"
      />
    </div>
  </section>
  <p class="workspace-note">
    {{
      admin
        ? "新建记录归属当前管理员，门户空间仅展示自己的记录。"
        : "这里仅展示属于你的记录，你可以随时新增与整理。"
    }}
  </p>
  <RecordEditor
    v-model="editing"
    :record="editRecord"
    :busy="saving"
    :error="saveError"
    @save="save"
  />
  <el-dialog v-model="detailOpen" title="记录详情" width="600px"
    ><div v-loading="detailLoading" class="detail-content">
      <el-alert
        v-if="detailError"
        :title="detailError"
        type="error"
        :closable="false"
      /><template v-if="detail"
        ><span
          class="status-pill"
          :class="{ disabled: detail.status === 'DISABLED' }"
          >{{ detail.status === "ENABLED" ? "启用" : "停用" }}</span
        >
        <h2>{{ detail.title }}</h2>
        <p class="detail-description">{{ detail.description || "暂无描述" }}</p>
        <dl class="detail-meta">
          <dt>记录编号</dt>
          <dd>{{ detail.id }}</dd>
          <dt>归属账号 / 编号</dt>
          <dd>{{ detail.ownerUsername }} / {{ detail.ownerId }}</dd>
          <dt>创建时间</dt>
          <dd>{{ time(detail.createdAt) }}</dd>
          <dt>更新时间</dt>
          <dd>{{ time(detail.updatedAt) }}</dd>
        </dl></template
      >
    </div>
    <template #footer
      ><el-button @click="detailOpen = false">关闭</el-button></template
    ></el-dialog
  >
</template>
