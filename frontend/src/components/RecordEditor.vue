<script setup>
import { ref, reactive, watch } from "vue";
const props = defineProps({
  modelValue: Boolean,
  record: { type: Object, default: null },
  busy: Boolean,
  error: { type: String, default: "" },
});
const emit = defineEmits(["update:modelValue", "save"]);
const formRef = ref();
const form = reactive({ title: "", description: "", status: "ENABLED" });
const rules = {
  title: [
    {
      validator: (_, value, done) => {
        const title = value.trim();
        done(
          !title || title.length > 100
            ? new Error("标题不能为空且最多100字")
            : undefined,
        );
      },
      trigger: "blur",
    },
  ],
  description: [{ max: 2000, message: "描述最多2000字", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
};
watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      Object.assign(form, {
        title: props.record?.title ?? "",
        description: props.record?.description ?? "",
        status: props.record?.status ?? "ENABLED",
      });
      formRef.value?.clearValidate();
    }
  },
);
async function save() {
  try {
    await formRef.value.validate();
    emit("save", {
      title: form.title.trim(),
      description: form.description,
      status: form.status,
    });
  } catch {
    /* 字段提示 */
  }
}
</script>
<template>
  <el-dialog
    :model-value="modelValue"
    :title="record ? '编辑记录' : '新增记录'"
    width="560px"
    :close-on-click-modal="false"
    :close-on-press-escape="!busy"
    :show-close="!busy"
    @update:model-value="
      (value) => {
        if (!busy) emit('update:modelValue', value);
      }
    "
  >
    <p class="dialog-intro">把想法记下来，用清楚的标题让它更容易被找到。</p>
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
      class="form-error"
    />
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      :disabled="busy"
      @submit.prevent="save"
    >
      <el-form-item label="标题" prop="title"
        ><el-input
          v-model="form.title"
          maxlength="100"
          show-word-limit
          placeholder="为记录起一个清楚的标题"
      /></el-form-item>
      <el-form-item label="描述" prop="description"
        ><el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          maxlength="2000"
          show-word-limit
          placeholder="补充记录的详细内容（选填）"
      /></el-form-item>
      <el-form-item label="状态" prop="status"
        ><el-radio-group v-model="form.status"
          ><el-radio value="ENABLED">启用</el-radio
          ><el-radio value="DISABLED">停用</el-radio></el-radio-group
        ></el-form-item
      >
    </el-form>
    <template #footer
      ><el-button :disabled="busy" @click="emit('update:modelValue', false)"
        >取消</el-button
      ><el-button type="primary" :loading="busy" @click="save"
        >保存记录</el-button
      ></template
    >
  </el-dialog>
</template>
