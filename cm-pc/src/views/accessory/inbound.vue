<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配件入库</h2>
    </div>
    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="inbound-form">
        <el-form-item label="条码" prop="barcode">
          <BarcodeScanner v-model="form.barcode" @scanned="(code) => form.barcode = code" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
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
    <el-dialog v-model="successVisible" title="入库成功" width="420px" :close-on-click-modal="false">
      <div class="success-content">
        <el-icon :size="48" color="#22c55e"><CircleCheckFilled /></el-icon>
        <h3>入库成功</h3>
        <div class="success-info">
          <div class="info-row">
            <span class="label">工件编号</span>
            <span class="value code">{{ lastItemCode }}</span>
            <el-button link type="primary" @click="copyCode">复制</el-button>
          </div>
          <div class="info-row">
            <span class="label">条码</span>
            <span class="value">{{ lastBarcode }}</span>
          </div>
          <div class="info-row">
            <span class="label">分类</span>
            <span class="value">{{ lastCategoryName }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="successVisible = false">关闭</el-button>
        <el-button type="primary" @click="continueInbound">继续入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { accessoryInbound } from '@/api/accessory'
import { getCategories } from '@/api/category'
import BarcodeScanner from '@/components/BarcodeScanner.vue'

const formRef = ref()
const loading = ref(false)
const categoryList = ref([])
const successVisible = ref(false)
const lastItemCode = ref('')
const lastBarcode = ref('')
const lastCategoryName = ref('')

const form = reactive({ barcode: '', categoryId: null, remark: '' })
const rules = {
  barcode: [{ required: true, message: '请输入条码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const { data } = await accessoryInbound(form)
    lastItemCode.value = data.itemCode
    lastBarcode.value = data.barcode
    lastCategoryName.value = data.categoryName || '-'
    successVisible.value = true
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

const copyCode = () => {
  navigator.clipboard.writeText(lastItemCode.value)
  ElMessage.success('已复制：' + lastItemCode.value)
}

const continueInbound = () => {
  successVisible.value = false
  resetForm()
}

const resetForm = () => {
  formRef.value?.resetFields()
  form.remark = ''
}

onMounted(async () => {
  try {
    const { data } = await getCategories()
    categoryList.value = data
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.inbound-form { max-width: 700px; }
.inbound-form :deep(.el-form-item) { margin-bottom: 22px; }
.inbound-form :deep(.el-form-item__label) { font-weight: 500; color: #475569; }

.success-content {
  text-align: center;
  padding: 12px 0;
}
.success-content h3 {
  margin: 12px 0 20px;
  font-size: 18px;
  color: #1e293b;
}
.success-info {
  background: #f8fafc;
  border-radius: 10px;
  padding: 16px 20px;
  text-align: left;
}
.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  gap: 12px;
}
.info-row + .info-row {
  border-top: 1px solid #e2e8f0;
}
.info-row .label {
  width: 70px;
  color: #94a3b8;
  font-size: 13px;
  flex-shrink: 0;
}
.info-row .value {
  flex: 1;
  font-size: 14px;
  color: #1e293b;
}
.info-row .code {
  font-weight: 700;
  font-size: 16px;
  color: #4361ee;
  font-family: monospace;
}
</style>