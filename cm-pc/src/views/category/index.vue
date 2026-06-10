<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
    </div>

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索分类名称" clearable style="width: 220px" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="openDialog(null)">添加大类</el-button>
    </div>

    <el-card v-loading="loading">
      <template v-if="treeData.length === 0 && !loading">
        <el-empty description="还没有分类，点击上方「添加大类」开始添加" />
      </template>

      <draggable v-model="treeData" item-key="id" handle=".drag-handle" ghost-class="ghost" @end="onDragEnd">
        <template #item="{ element: group, index: gIdx }">
          <div class="category-item">
            <div class="cat-row" :class="{ expanded: expandedIds.has(group.id) }" @click.stop="toggleExpand(group.id)">
              <div class="cat-row-left">
                <div class="drag-handle" @click.stop>
                  <div class="drag-dots"><span></span><span></span><span></span><span></span><span></span><span></span></div>
                </div>
                <span class="arrow">{{ expandedIds.has(group.id) ? "▼" : "▶" }}</span>
                <span class="cat-name">{{ group.name }}</span>
                <el-tag v-if="group.partNumber" size="small">{{ group.partNumber }}</el-tag>
                <span class="cat-count">{{ group.children ? group.children.length : 0 }} 个小类</span>
              </div>
              <div class="cat-row-actions" @click.stop>
                <el-button link type="primary" @click="openChildDialog(group)">添加小类</el-button>
                <el-button link type="primary" @click="openDialog(group)">编辑</el-button>
                <el-button link type="danger" @click="handleDelete(group)">删除</el-button>
              </div>
            </div>

            <transition name="expand">
              <div v-if="expandedIds.has(group.id)" class="sub-list-wrapper">
                <div v-if="group.children && group.children.length > 0" class="sub-list">
                  <draggable v-model="group.children" item-key="id" handle=".drag-handle" ghost-class="ghost" @end="onSubDragEnd(group)">
                    <template #item="{ element: child, index: cIdx }">
                      <div class="sub-row">
                        <div class="sub-row-left">
                          <div class="drag-handle" @click.stop>
                            <div class="drag-dots"><span></span><span></span><span></span><span></span><span></span><span></span></div>
                          </div>
                          <span class="sub-name">{{ child.name }}</span>
                          <el-tag v-if="child.partNumber" size="small">{{ child.partNumber }}</el-tag>
                          <span v-if="child.remark" class="sub-remark">{{ child.remark }}</span>
                        </div>
                        <div class="sub-row-actions">
                          <el-button link type="primary" @click="openDialog(child)">编辑</el-button>
                          <el-button link type="danger" @click="handleDelete(child)">删除</el-button>
                        </div>
                      </div>
                    </template>
                  </draggable>
                </div>
                <div v-else class="sub-empty">
                  <span class="sub-empty-text">暂无小类</span>
                </div>
              </div>
            </transition>
          </div>
        </template>
      </draggable>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <template v-if="!editId && !presetParentId">
          <div class="dialog-hint">添加一个大的类别，例如：空调、热水器、油烟机<br>当前排序值：{{ nextSort }}</div>
          <el-form-item label="大类名称" prop="name"><el-input v-model="form.name" placeholder="例如：空调" /></el-form-item>
          <el-form-item label="专用号"><el-input v-model="form.partNumber" placeholder="选填" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" /></el-form-item>
        </template>
        <template v-else-if="presetParentId && !editId">
          <div class="dialog-hint">在「{{ presetParentName }}」下添加一个小类</div>
          <el-form-item label="小类名称" prop="name"><el-input v-model="form.name" placeholder="例如：电机、内电脑板" /></el-form-item>
          <el-form-item label="专用号"><el-input v-model="form.partNumber" placeholder="选填" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" /></el-form-item>
        </template>
        <template v-else>
          <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="归类">
            <el-tag v-if="editParentName">{{ editParentName }} 下的小类</el-tag>
            <el-tag v-else type="info">顶级大类</el-tag>
          </el-form-item>
          <el-form-item label="专用号"><el-input v-model="form.partNumber" placeholder="选填" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import draggable from "vuedraggable"
import { getCategories, createCategory, updateCategory, deleteCategory, batchSortCategories } from "@/api/category"

const loading = ref(false)
const list = ref([])
const treeData = ref([])
const expandedIds = ref(new Set())
const dialogVisible = ref(false)
const editId = ref(null)
const presetParentId = ref(null)
const presetParentName = ref("")
const formRef = ref()
const submitting = ref(false)
const keyword = ref("")
const form = reactive({ name: "", remark: "", parentId: null, partNumber: "" })
const formRules = { name: [{ required: true, message: "请输入名称", trigger: "blur" }] }

const dialogTitle = computed(() => editId.value ? "编辑分类" : presetParentId.value ? "添加小类" : "添加大类")
const nextSort = computed(() => {
  if (treeData.value.length === 0) return 10
  return Math.max(...treeData.value.map(c => c.sort || 0)) + 10
})
const editParentName = computed(() => {
  if (!editId.value) return ""
  const row = list.value.find(r => r.id === editId.value)
  if (!row?.parentId) return ""
  const p = list.value.find(r => r.id === row.parentId)
  return p ? p.name : ""
})

function buildTree(flat) {
  const map = {}, roots = []
  for (const item of flat) map[item.id] = { ...item, children: [] }
  for (const item of flat) {
    if (item.parentId && map[item.parentId]) map[item.parentId].children.push(map[item.id])
    else if (!item.parentId) roots.push(map[item.id])
  }
  for (const id in map) if (map[id].children.length) map[id].children.sort((a, b) => a.sort - b.sort)
  roots.sort((a, b) => a.sort - b.sort)
  return roots
}

function toggleExpand(id) {
  const s = new Set(expandedIds.value)
  s.has(id) ? s.delete(id) : s.add(id)
  expandedIds.value = s
}

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getCategories({ keyword: keyword.value || undefined })
    list.value = data || []
    treeData.value = buildTree(list.value)
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
  finally { loading.value = false }
}

const openDialog = (row) => {
  editId.value = row?.id || null
  presetParentId.value = null
  presetParentName.value = ""
  Object.assign(form, row ? { name: row.name, remark: row.remark || "", parentId: row.parentId || null, partNumber: row.partNumber || "" } : { name: "", remark: "", parentId: null, partNumber: "" })
  dialogVisible.value = true
}

const openChildDialog = (parent) => {
  editId.value = null
  presetParentId.value = parent.id
  presetParentName.value = parent.name
  Object.assign(form, { name: "", remark: "", parentId: parent.id, partNumber: "" })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editId.value) { await updateCategory(editId.value, form); ElMessage.success("编辑成功") }
    else { await createCategory(form); ElMessage.success("添加成功") }
    dialogVisible.value = false
    presetParentId.value = null
    await loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
  finally { submitting.value = false }
}

const handleDelete = async (row) => {
  const hasChildren = row.children && row.children.length > 0
  const msg = hasChildren ? "该分类下有小类，删除会同时删除所有小类，确认？" : "确认删除该分类？"
  try {
    await ElMessageBox.confirm(msg, "提示", { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" })
    await deleteCategory(row.id)
    ElMessage.success("删除成功")
    await loadData()
  } catch (e) { if (e !== "cancel") ElMessage.error(e.response?.data?.message || e.message) }
}

const onDragEnd = async () => {
  try { await batchSortCategories(treeData.value.map(i => i.id)); ElMessage.success("排序已更新"); await loadData() }
  catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
}

const onSubDragEnd = async (group) => {
  try { await batchSortCategories(group.children.map(i => i.id)); ElMessage.success("小类排序已更新"); await loadData() }
  catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; color: #1e293b; margin: 0; }
.toolbar { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.category-item { border: 1px solid #e5e7eb; border-radius: 6px; margin-bottom: 8px; overflow: hidden; }
.cat-row { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: #f9fafb; cursor: pointer; user-select: none; transition: background 0.2s; }
.cat-row:hover { background: #f3f4f6; }
.cat-row.expanded { border-bottom: 1px solid #e5e7eb; background: #eef2ff; }
.cat-row-left { display: flex; align-items: center; gap: 8px; }
.arrow { font-size: 10px; color: #9ca3af; width: 14px; text-align: center; transition: transform 0.2s; }
.cat-name { font-weight: 600; font-size: 14px; color: #1f2937; }
.cat-count { font-size: 12px; color: #9ca3af; }
.cat-row-actions { display: flex; align-items: center; gap: 2px; }
.drag-handle { cursor: grab; display: flex; align-items: center; padding: 4px; border-radius: 4px; transition: background 0.15s; }
.drag-handle:hover { background: #e5e7eb; }
.drag-handle:active { cursor: grabbing; }
.drag-dots { display: grid; grid-template-columns: 8px 8px; gap: 3px; }
.drag-dots span { width: 4px; height: 4px; background: #9ca3af; border-radius: 50%; }
.ghost { opacity: 0.5; background: #e0e7ff; }
.sub-list-wrapper { overflow: hidden; }
.sub-list { padding: 0; }
.sub-row { display: flex; align-items: center; justify-content: space-between; padding: 8px 14px 8px 32px; border-bottom: 1px solid #f3f4f6; transition: background 0.15s; }
.sub-row:last-child { border-bottom: none; }
.sub-row:hover { background: #fafafa; }
.sub-row-left { display: flex; align-items: center; gap: 6px; }
.sub-name { font-size: 14px; color: #374151; }
.sub-remark { font-size: 12px; color: #9ca3af; }
.sub-row-actions { display: flex; align-items: center; gap: 2px; }
.sub-empty { padding: 10px 14px 10px 32px; }
.sub-empty-text { font-size: 12px; color: #d1d5db; font-style: italic; }
.dialog-hint { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 6px; padding: 10px 14px; margin-bottom: 16px; font-size: 13px; color: #1d4ed8; line-height: 1.5; }
.expand-enter-active, .expand-leave-active { transition: all 0.3s ease-in-out; max-height: 2000px; opacity: 1; }
.expand-enter-from, .expand-leave-to { max-height: 0; opacity: 0; }
</style>