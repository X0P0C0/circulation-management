<template>
  <div class="category-cascader">
    <div class="cascader-trigger" @click="togglePanel">
      <span v-if="selectedLabel" class="cascader-value">{{ selectedLabel }}</span>
      <span v-else class="cascader-placeholder">{{ placeholder }}</span>
      <span class="cascader-suffix">
        <span v-if="modelValue" class="cascader-clear" @click.stop="clearSelection">
          <svg width="14" height="14" viewBox="0 0 1024 1024"><path fill="currentColor" d="m466.752 512-90.496-90.496a32 32 0 0 1 45.248-45.248L512 466.752l90.496-90.496a32 32 0 1 1 45.248 45.248L557.248 512l90.496 90.496a32 32 0 1 1-45.248 45.248L512 557.248l-90.496 90.496a32 32 0 0 1-45.248-45.248z"></path><path fill="currentColor" d="M512 896a384 384 0 1 0 0-768 384 384 0 0 0 0 768m0 64a448 448 0 1 1 0-896 448 448 0 0 1 0 896"></path></svg>
        </span>
        <span v-else class="cascader-arrow">
          <svg width="14" height="14" viewBox="0 0 1024 1024" :style="{ transform: showPanel ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.25s' }"><path fill="currentColor" d="M831.872 340.864 512 652.672 192.128 340.864a30.59 30.59 0 0 0-42.752 0 29.12 29.12 0 0 0 0 41.6L489.664 714.24a32 32 0 0 0 44.672 0l340.288-331.712a29.12 29.12 0 0 0 0-41.728 30.59 30.59 0 0 0-42.752 0z"></path></svg>
        </span>
      </span>
    </div>
    <transition name="cascader-panel">
      <div v-if="showPanel" class="cascader-panel">
        <div class="cascader-col">
          <div class="cascader-col-title">大类</div>
          <div class="cascader-items">
            <div v-for="top in topCategories" :key="top.id" class="cascader-item" :class="{ active: selectedTopId === top.id }" @click="selectTop(top)">
              <span>{{ top.name }}</span>
              <span class="cascader-arrow-right">&gt;</span>
            </div>
          </div>
        </div>
        <div class="cascader-col">
          <div class="cascader-col-title">小类</div>
          <div class="cascader-search">
            <input v-model="searchText" placeholder="搜索小类..." class="cascader-search-input" />
          </div>
          <div class="cascader-items">
            <template v-if="selectedTopId">
              <div v-for="sub in filteredSubCategories" :key="sub.id" class="cascader-item" :class="{ active: modelValue === sub.id }" @click="selectSub(sub)">
                <span>{{ sub.name }}</span>
                <span v-if="sub.partNumber" class="cascader-part">{{ sub.partNumber }}</span>
              </div>
            </template>
            <div v-else class="cascader-empty">请先选择大类</div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { getCategories } from '@/api/category'

const props = defineProps({ modelValue: [Number, null], placeholder: { type: String, default: '请选择分类' } })
const emit = defineEmits(['update:modelValue'])

const showPanel = ref(false)
const searchText = ref('')
const allCategories = ref([])
const selectedTopId = ref(null)

const topCategories = computed(() => allCategories.value.filter(c => c.parentId === 0))
const subCategories = computed(() => allCategories.value.filter(c => c.parentId === selectedTopId.value))
const filteredSubCategories = computed(() => {
  if (!searchText.value) return subCategories.value
  const kw = searchText.value.toLowerCase()
  return subCategories.value.filter(c => c.name.toLowerCase().includes(kw) || (c.partNumber && c.partNumber.toLowerCase().includes(kw)))
})

const selectedLabel = computed(() => {
  if (!props.modelValue) return ''
  const cat = allCategories.value.find(c => c.id === props.modelValue)
  if (!cat) return ''
  const parent = cat.parentId ? allCategories.value.find(c => c.id === cat.parentId) : null
  return parent ? parent.name + ' / ' + cat.name : cat.name
})

function togglePanel() { showPanel.value = !showPanel.value }
function selectTop(top) { selectedTopId.value = top.id; searchText.value = '' }
function selectSub(sub) { emit('update:modelValue', sub.id); showPanel.value = false; searchText.value = '' }
function clearSelection() { emit('update:modelValue', null); selectedTopId.value = null; searchText.value = '' }

watch(() => props.modelValue, (val) => {
  if (val) {
    const cat = allCategories.value.find(c => c.id === val)
    if (cat?.parentId) selectedTopId.value = cat.parentId
  }
})

function handleClickOutside(e) {
  if (!e.target.closest('.category-cascader')) {
    showPanel.value = false
    searchText.value = ''
  }
}

onMounted(() => {
  loadData()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

async function loadData() {
  try {
    const { data } = await getCategories()
    allCategories.value = data || []
  } catch (e) { /* ignore */ }
}

defineExpose({ refresh: loadData })
</script>

<style scoped>
.category-cascader { position: relative; width: 100%; overflow: visible; }
.cascader-trigger { position: relative; display: flex; align-items: center; justify-content: space-between; width: 116px; height: 22px; padding: 1px 7px; margin: 0; font-size: 12px; border: 1px solid #dcdfe6; border-radius: 8px; cursor: pointer; background: #fff; box-sizing: border-box; }
.cascader-trigger:hover { border-color: #409eff; }
.cascader-value { color: #303133; font-size: 12px; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cascader-placeholder { color: #c0c4cc; font-size: 12px; flex: 1; }
.cascader-suffix { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); display: flex; align-items: center; }
.cascader-arrow { display: flex; align-items: center; color: #c0c4cc; }
.cascader-clear { visibility: hidden; display: flex; align-items: center; color: #c0c4cc; cursor: pointer; position: absolute; right: 0; }
.cascader-clear:hover { color: #909399; }
.cascader-trigger:hover .cascader-clear { visibility: visible; }
.cascader-trigger:hover .cascader-arrow { visibility: hidden; }
.cascader-panel { position: absolute; top: 100%; left: 0; z-index: 9999; display: flex; background: #fff; border: 1px solid #e4e7ed; border-radius: 4px; box-shadow: none; margin-top: 4px; width: auto; max-height: 350px; overflow: hidden; align-items: stretch; }
.cascader-col { width: 200px; border-right: 1px solid #e4e7ed; display: flex; flex-direction: column; }
.cascader-col:last-child { border-right: none; width: 260px; min-width: 260px; }
.cascader-col-title { padding: 8px 12px; font-size: 12px; background: #f5f7fa; color: #909399; font-weight: 600; }
.cascader-search { padding: 6px 8px; border-bottom: 1px solid #e4e7ed; }
.cascader-search-input { width: 100%; height: auto; padding: 6px 10px; margin: 0; font-size: 13px; border-radius: 4px; background: #fff; border: 1px solid #dcdfe6; outline: none; box-sizing: border-box; }
.cascader-search-input:focus { border-color: #409eff; }
.cascader-items { max-height: 400px; overflow-y: auto; overflow-x: hidden; padding: 4px 0; flex: 1; }
.cascader-item { width: 220.8px; height: 25px; padding: 10px 12px; margin: 0; font-size: 14px; border-radius: 0; background: #f5f7fa; display: flex; align-items: center; justify-content: space-between; cursor: pointer; color: #606266; }
.cascader-item:hover { background: #ecf5ff; color: #409eff; }
.cascader-item.active { color: #409eff; font-weight: 600; background: #ecf5ff; }
.cascader-arrow-right { color: #c0c4cc; font-size: 12px; }
.cascader-part { font-size: 12px; color: #909399; margin-left: 8px; }
.cascader-empty { padding: 12px; text-align: center; color: #c0c4cc; font-size: 13px; }
.cascader-panel-enter-active { transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.cascader-panel-leave-active { transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); }
.cascader-panel-enter-from { opacity: 0; transform: translateY(-8px) scaleY(0.95); }
.cascader-panel-leave-to { opacity: 0; transform: translateY(-4px) scaleY(0.98); }
</style>