<template>
  <div class="custom-select">
    <div class="select-trigger" @click="togglePanel">
      <span v-if="selectedLabel" class="select-value">{{ selectedLabel }}</span>
      <span v-else class="select-placeholder">{{ placeholder }}</span>
      <span class="select-suffix">
        <span v-if="modelValue" class="select-clear" @click.stop="clearSelection">
          <svg width="14" height="14" viewBox="0 0 1024 1024"><path fill="currentColor" d="m466.752 512-90.496-90.496a32 32 0 0 1 45.248-45.248L512 466.752l90.496-90.496a32 32 0 1 1 45.248 45.248L557.248 512l90.496 90.496a32 32 0 1 1-45.248 45.248L512 557.248l-90.496 90.496a32 32 0 0 1-45.248-45.248z"></path><path fill="currentColor" d="M512 896a384 384 0 1 0 0-768 384 384 0 0 0 0 768m0 64a448 448 0 1 1 0-896 448 448 0 0 1 0 896"></path></svg>
        </span>
        <span v-else class="select-arrow">
          <svg width="14" height="14" viewBox="0 0 1024 1024" :style="{ transform: showPanel ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.25s' }"><path fill="currentColor" d="M831.872 340.864 512 652.672 192.128 340.864a30.59 30.59 0 0 0-42.752 0 29.12 29.12 0 0 0 0 41.6L489.664 714.24a32 32 0 0 0 44.672 0l340.288-331.712a29.12 29.12 0 0 0 0-41.728 30.59 30.59 0 0 0-42.752 0z"></path></svg>
        </span>
      </span>
    </div>
    <transition name="select-panel">
      <div v-if="showPanel" class="select-panel">
        <div class="select-search">
          <input v-model="searchText" :placeholder="searchPlaceholder" class="select-search-input" />
        </div>
        <div class="select-items">
          <div v-for="item in filteredOptions" :key="item[valueKey]" class="select-item" :class="{ active: modelValue === item[valueKey] }" @click="selectItem(item)">
            <span>{{ item[labelKey] }}</span>
            <span v-if="extraKey && item[extraKey]" class="select-extra">{{ item[extraKey] }}</span>
          </div>
          <div v-if="filteredOptions.length === 0" class="select-empty">暂无数据</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: [String, Number, null],
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '请选择' },
  labelKey: { type: String, default: 'label' },
  valueKey: { type: String, default: 'id' },
  extraKey: { type: String, default: '' },
  searchPlaceholder: { type: String, default: '搜索...' }
})

const emit = defineEmits(['update:modelValue'])
const showPanel = ref(false)
const searchText = ref('')

const selectedLabel = computed(() => {
  if (!props.modelValue) return ''
  const item = props.options.find(o => o[props.valueKey] === props.modelValue)
  return item ? item[props.labelKey] : ''
})

const filteredOptions = computed(() => {
  if (!searchText.value) return props.options
  const kw = searchText.value.toLowerCase()
  return props.options.filter(o => {
    const label = (o[props.labelKey] || '').toLowerCase()
    const extra = props.extraKey ? (o[props.extraKey] || '').toLowerCase() : ''
    return label.includes(kw) || extra.includes(kw)
  })
})

function togglePanel() { showPanel.value = !showPanel.value }
function selectItem(item) { emit('update:modelValue', item[props.valueKey]); showPanel.value = false; searchText.value = '' }
function clearSelection() { emit('update:modelValue', null); searchText.value = '' }

function handleClickOutside(e) {
  if (!e.target.closest('.custom-select')) {
    showPanel.value = false
    searchText.value = ''
  }
}

onMounted(() => { document.addEventListener('click', handleClickOutside) })
onUnmounted(() => { document.removeEventListener('click', handleClickOutside) })
</script>

<style scoped>
.custom-select { position: relative; width: 100%; overflow: visible; }
.select-trigger { position: relative; display: flex; align-items: center; justify-content: space-between; width: 116px; height: 22px; padding: 1px 7px; margin: 0; font-size: 12px; border: 1px solid #dcdfe6; border-radius: 8px; cursor: pointer; background: #fff; box-sizing: border-box; }
.select-trigger:hover { border-color: #409eff; }
.select-value { color: #303133; font-size: 12px; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.select-placeholder { color: #c0c4cc; font-size: 12px; flex: 1; }
.select-suffix { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); display: flex; align-items: center; }
.select-arrow { display: flex; align-items: center; color: #c0c4cc; }
.select-clear { visibility: hidden; display: flex; align-items: center; color: #c0c4cc; cursor: pointer; position: absolute; right: 0; }
.select-clear:hover { color: #909399; }
.select-trigger:hover .select-clear { visibility: visible; }
.select-trigger:hover .select-arrow { visibility: hidden; }
.select-panel { position: absolute; top: 100%; left: 0; z-index: 9999; background: #fff; border: 1px solid #e4e7ed; border-radius: 4px; box-shadow: none; width: 100%; max-height: 350px; overflow-y: auto; display: flex; flex-direction: column; margin-top: 4px; }
.select-search { padding: 6px 8px; border-bottom: 1px solid #e4e7ed; }
.select-search-input { width: 100%; height: auto; padding: 6px 10px; margin: 0; font-size: 13px; border-radius: 4px; background: #fff; border: 1px solid #dcdfe6; outline: none; box-sizing: border-box; }
.select-search-input:focus { border-color: #409eff; }
.select-items { max-height: 280px; overflow-y: auto; overflow-x: hidden; padding: 4px 0; }
.select-item { padding: 8px 12px; font-size: 14px; color: #606266; cursor: pointer; display: flex; align-items: center; justify-content: space-between; }
.select-item:hover { background: #ecf5ff; color: #409eff; }
.select-item.active { color: #409eff; font-weight: 600; background: #ecf5ff; }
.select-extra { font-size: 12px; color: #909399; margin-left: 8px; }
.select-empty { padding: 12px; text-align: center; color: #c0c4cc; font-size: 13px; }
.select-panel-enter-active { transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.select-panel-leave-active { transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); }
.select-panel-enter-from { opacity: 0; transform: translateY(-8px) scaleY(0.95); }
.select-panel-leave-to { opacity: 0; transform: translateY(-4px) scaleY(0.98); }
</style>