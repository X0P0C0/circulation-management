<template>
  <div class="page-container inbound-page">
    <div class="page-header">
      <h2 class="page-title">配件入库</h2>
      <el-button @click="importDialogVisible = true">批量导入</el-button>
    </div>
    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="inbound-form">
        <el-form-item label="条码" prop="barcode">
          <BarcodeScanner v-model="form.barcode" @scanned="(code) => form.barcode = code" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="100" style="width: 160px" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <CategoryCascader v-model="form.categoryId" placeholder="请选择分类" />
        </el-form-item>
        <el-form-item label="货架">
          <CustomSelect v-model="form.shelfId" :options="shelfList" placeholder="选填，选择货架位置" searchPlaceholder="搜索货架..." labelKey="name" extraKey="location" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认入库</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 入库成功弹窗 -->
    <el-dialog v-model="successVisible" title="入库成功" width="520px" :close-on-click-modal="true">
      <div class="success-content">
        <el-icon :size="48" color="#22c55e"><CircleCheckFilled /></el-icon>
        <h3>成功入库 {{ lastItems.length }} 个工件</h3>
        <div class="success-info">
          <div class="info-row">
            <span class="label">条码</span>
            <span class="value">{{ lastBarcode }}</span>
          </div>
          <div class="info-row">
            <span class="label">分类</span>
            <span class="value">{{ lastCategoryName }}</span>
          </div>
        </div>
        <div class="item-code-list">
          <div class="list-header">
            <span>工件编号</span>
            <el-button link type="primary" size="small" @click="copyAllCodes">复制全部</el-button>
          </div>
          <div class="code-grid">
            <div v-for="item in lastItems" :key="item.itemCode" class="code-item"
              @click="copyCode(item.itemCode)">
              <span class="code-text">{{ item.itemCode }}</span>
              <el-icon size="14" color="#94a3b8"><CopyDocument /></el-icon>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="successVisible = false">关闭</el-button>
        <el-button type="primary" @click="continueInbound">继续入库</el-button>
      </template>
    </el-dialog>
    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="批量导入工件" width="480px" :close-on-click-modal="true">
      <el-form label-width="80px">
        <el-form-item label="CSV文件">
          <el-upload :auto-upload="false" :limit="1" :on-change="onImportFileChange" :on-remove="() => importFile = null"
            accept=".csv" drag>
            <el-icon :size="40"><UploadFilled /></el-icon>
            <div>拖拽或点击选择 CSV 文件</div>
            <template #tip>
              <div style="color:#94a3b8;font-size:12px">格式：条码,数量,分类名称,备注（分类名称自动匹配，可选） — 首行为表头</div>
            </template>
          </el-upload>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, CopyDocument, UploadFilled } from '@element-plus/icons-vue'
import { accessoryInbound, importAccessories } from '@/api/accessory'
import { getCategories } from '@/api/category'
import { getShelves } from '@/api/shelf'
import BarcodeScanner from '@/components/BarcodeScanner.vue'
import CategoryCascader from '@/components/CategoryCascader.vue'
import CustomSelect from '@/components/CustomSelect.vue'

const formRef = ref()
const loading = ref(false)
const categoryList = ref([])
const shelfList = ref([])
const successVisible = ref(false)
const importDialogVisible = ref(false)
const importFile = ref(null)
const importLoading = ref(false)
const lastItems = ref([])
const lastBarcode = ref('')
const lastCategoryName = ref('')

const form = reactive({ barcode: '', quantity: 1, categoryId: null, shelfId: null, remark: '' })
const rules = {
  barcode: [{ required: true, message: '请输入条码', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

// 选择分类后自动填充条码（仅当条码为空时）
watch(() => form.categoryId, (catId) => {
  if (!catId || form.barcode) return
  const cat = categoryList.value.find(c => c.id === catId)
  if (cat && cat.partNumber) {
    form.barcode = cat.partNumber
  }
})

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const { data } = await accessoryInbound(form)
    lastItems.value = data
    lastBarcode.value = form.barcode
    lastCategoryName.value = categoryList.value.find(c => c.id === form.categoryId)?.name || '-'
    successVisible.value = true
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally {
    loading.value = false
  }
}

const copyCode = (code) => {
  navigator.clipboard.writeText(code)
  ElMessage.success('已复制：' + code)
}

const copyAllCodes = () => {
  const text = lastItems.value.map(i => i.itemCode).join('\n')
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制全部工件编号')
}

const continueInbound = () => {
  successVisible.value = false
  resetForm()
}

const resetForm = () => {
  formRef.value?.resetFields()
  form.remark = ''
  form.quantity = 1
}

const handleImport = async () => {
  if (!importFile.value) { ElMessage.warning('请选择文件') ; return }
  importLoading.value = true
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    const { data } = await importAccessories(fd)
    ElMessage.success('成功导入 ' + data.length + ' 个工件')
    importDialogVisible.value = false
    importFile.value = null
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { importLoading.value = false }
}

const onImportFileChange = (file) => {
  importFile.value = file.raw
}

const loadShelfData = async () => {
  try {
    const { data } = await getShelves()
    shelfList.value = data || []
  } catch (e) { /* ignore */ }
}

onMounted(async () => {
  try {
    const { data } = await getCategories()
    categoryList.value = data
  } catch (e) { /* ignore */ }
  loadShelfData()
})
</script>

<style scoped>
.page-container { padding: 24px; overflow: visible; }
.inbound-form :deep(.el-card) { overflow: visible; }
.page-container :deep(.el-card__body) { width: 100%; height: 520px; padding: 20px; margin: 0; font-size: 16px; border-radius: 0; overflow: visible; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.inbound-form { max-width: 700px; }
.inbound-form :deep(.el-form-item) { margin-bottom: 22px; }
.inbound-form :deep(.el-form-item__label) { font-weight: 500; color: #475569; }
.inbound-form :deep(.el-input__inner) { font-size: 14px; color: #303133; }
.inbound-form :deep(.el-textarea__inner) { font-size: 14px; color: #303133; }
.inbound-form :deep(.el-input-number) { font-size: 14px; }
.inbound-form :deep(.cascader-value) { font-size: 14px; color: #303133; }
.inbound-form :deep(.cascader-placeholder) { font-size: 14px; color: #c0c4cc; }
.inbound-form :deep(.select-value) { font-size: 14px; color: #303133; }
.inbound-form :deep(.select-placeholder) { font-size: 14px; color: #c0c4cc; }

.success-content { text-align: center; padding: 12px 0; }
.success-content h3 { margin: 12px 0 20px; font-size: 18px; color: #1e293b; }
.success-info {
  background: #f8fafc; border-radius: 10px; padding: 16px 20px; text-align: left; margin-bottom: 16px;
}
.info-row { display: flex; align-items: center; padding: 8px 0; gap: 12px; }
.info-row + .info-row { border-top: 1px solid #e2e8f0; }
.info-row .label { width: 50px; color: #94a3b8; font-size: 13px; flex-shrink: 0; }
.info-row .value { flex: 1; font-size: 14px; color: #1e293b; }

.item-code-list {
  background: #f8fafc; border-radius: 10px; padding: 12px 16px; text-align: left;
}
.list-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;
  font-size: 13px; font-weight: 600; color: #475569;
}
.code-grid { display: flex; flex-direction: column; gap: 6px; }
.code-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px; background: #fff; border-radius: 8px;
  cursor: pointer; transition: all 0.15s; border: 1px solid #e2e8f0;
}
.code-item:hover { border-color: #4361ee; background: #eef1ff; }
.code-text {
  font-family: 'Cascadia Code', 'Fira Code', monospace;
  font-size: 15px; font-weight: 600; color: #4361ee;
}
</style>
<style>
/* Inbound page - wider selectors */
.inbound-page .cascader-trigger { width: 255px !important; }
.inbound-page .select-trigger { width: 255px !important; }
/* Inbound page - constrain dropdown panel widths to match trigger */
.inbound-page .category-cascader .cascader-panel { width: 460px !important; }
.inbound-page .custom-select .select-panel { width: 255px !important; }
</style>