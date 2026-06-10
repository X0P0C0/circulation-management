<template>
  <div v-if="active" class="dev-design-overlay">
    <div class="dev-highlight" :style="highlightStyle"></div>
    <div class="dev-panel" v-if="selectedEl" :style="panelStyle">
      <div class="dev-panel-header">
        <span class="dev-tag">&lt;{{ selectedTag }}&gt;</span>
        <span class="dev-class">{{ selectedClass }}</span>
        <button class="dev-btn-close" @click="deselect">&times;</button>
      </div>
      <div class="dev-panel-body">
        <div class="dev-row">
          <label>宽</label>
          <input type="text" v-model="props.width" @change="apply" placeholder="auto" />
          <label>高</label>
          <input type="text" v-model="props.height" @change="apply" placeholder="auto" />
        </div>
        <div class="dev-row">
          <label>内边距</label>
          <input type="text" v-model="props.padding" @change="apply" placeholder="0" />
          <label>外边距</label>
          <input type="text" v-model="props.margin" @change="apply" placeholder="0" />
        </div>
        <div class="dev-row">
          <label>字号</label>
          <input type="text" v-model="props.fontSize" @change="apply" placeholder="inherit" />
          <label>圆角</label>
          <input type="text" v-model="props.borderRadius" @change="apply" placeholder="0" />
        </div>
        <div class="dev-row">
          <label>背景</label>
          <input type="text" v-model="props.background" @change="apply" placeholder="transparent" />
        </div>
      </div>
      <div class="dev-panel-footer">
        <button class="dev-btn" @click="copyCSS">复制 CSS</button>
        <button class="dev-btn" @click="resetCSS">重置</button>
        <span class="dev-hint">点击元素选中 | Esc 退出</span>
      </div>
    </div>
    <div class="dev-toolbar">
      <span class="dev-title">设计模式</span>
      <button class="dev-btn" @click="active = false">退出</button>
    </div>
  </div>
  <button v-if="isDev && !active" class="dev-trigger" @click="active = true" title="打开设计模式">
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M12 20h9M16.5 3.5a2.12 2.12 0 013 3L7 19l-4 1 1-4z"/>
    </svg>
  </button>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'

const active = ref(false)
const isDev = import.meta.env.DEV

const selectedEl = ref(null)
const selectedTag = ref('')
const selectedClass = ref('')

const highlightStyle = reactive({ display: 'none', top: '0', left: '0', width: '0', height: '0' })
const panelStyle = reactive({ top: '0', left: '0' })

const props = reactive({
  width: '', height: '', padding: '', margin: '',
  fontSize: '', borderRadius: '', background: ''
})

let originalStyles = {}

function getRect(el) {
  const r = el.getBoundingClientRect()
  return { top: r.top + window.scrollY, left: r.left + window.scrollX, width: r.width, height: r.height }
}

function onHover(e) {
  if (!active.value) return
  const el = e.target
  if (el.closest('.dev-panel') || el.closest('.dev-toolbar') || el.closest('.dev-trigger')) return
  const r = getRect(el)
  Object.assign(highlightStyle, {
    display: 'block',
    top: r.top + 'px', left: r.left + 'px',
    width: r.width + 'px', height: r.height + 'px'
  })
}

function onClick(e) {
  if (!active.value) return
  const el = e.target
  if (el.closest('.dev-panel') || el.closest('.dev-toolbar') || el.closest('.dev-trigger')) return
  e.preventDefault()
  e.stopPropagation()

  selectedEl.value = el
  selectedTag.value = el.tagName.toLowerCase()
  selectedClass.value = el.className ? ('.' + String(el.className).split(' ').slice(0, 3).join('.')) : ''

  const cs = getComputedStyle(el)
  originalStyles = {
    width: el.style.width || '', height: el.style.height || '',
    padding: el.style.padding || '', margin: el.style.margin || '',
    fontSize: el.style.fontSize || '', borderRadius: el.style.borderRadius || '',
    background: el.style.background || ''
  }
  props.width = cs.width !== 'auto' ? cs.width : ''
  props.height = cs.height !== 'auto' ? cs.height : ''
  props.padding = cs.padding || ''
  props.margin = cs.margin || ''
  props.fontSize = cs.fontSize || ''
  props.borderRadius = cs.borderRadius || ''
  props.background = cs.background !== 'rgba(0, 0, 0, 0)' ? cs.background : ''

  const r = getRect(el)
  const panelTop = Math.min(r.top, window.innerHeight - 260)
  const panelLeft = Math.min(r.left + r.width + 8, window.innerWidth - 280)
  Object.assign(panelStyle, { top: panelTop + 'px', left: panelLeft + 'px' })
}

function apply() {
  const el = selectedEl.value
  if (!el) return
  if (props.width) el.style.width = props.width
  if (props.height) el.style.height = props.height
  if (props.padding) el.style.padding = props.padding
  if (props.margin) el.style.margin = props.margin
  if (props.fontSize) el.style.fontSize = props.fontSize
  if (props.borderRadius) el.style.borderRadius = props.borderRadius
  if (props.background) el.style.background = props.background
}

function resetCSS() {
  const el = selectedEl.value
  if (!el) return
  el.style.width = originalStyles.width
  el.style.height = originalStyles.height
  el.style.padding = originalStyles.padding
  el.style.margin = originalStyles.margin
  el.style.fontSize = originalStyles.fontSize
  el.style.borderRadius = originalStyles.borderRadius
  el.style.background = originalStyles.background
  Object.keys(props).forEach(k => { props[k] = '' })
}

function copyCSS() {
  if (!selectedEl.value) return
  const el = selectedEl.value
  const lines = []
  if (props.width) lines.push('  width: ' + props.width + ';')
  if (props.height) lines.push('  height: ' + props.height + ';')
  if (props.padding) lines.push('  padding: ' + props.padding + ';')
  if (props.margin) lines.push('  margin: ' + props.margin + ';')
  if (props.fontSize) lines.push('  font-size: ' + props.fontSize + ';')
  if (props.borderRadius) lines.push('  border-radius: ' + props.borderRadius + ';')
  if (props.background) lines.push('  background: ' + props.background + ';')

  // Element info
  const tag = el.tagName.toLowerCase()
  const cls = el.className ? String(el.className).split(' ').filter(Boolean).join('.') : ''
  const id = el.id ? '#' + el.id : ''
  const selector = tag + (id || '') + (cls ? '.' + cls : '')
  const text = (el.textContent || '').trim().substring(0, 30)
  const parent = el.parentElement ? el.parentElement.tagName.toLowerCase() + (el.parentElement.className ? '.' + String(el.parentElement.className).split(' ')[0] : '') : ''

  const css = selectedClass.value + ' {\n' + lines.join('\n') + '\n}'
  const info = '元素: <' + selector + '>\n父级: <' + parent + '>\n文本: ' + text + '\n---\n' + css
  navigator.clipboard.writeText(info).then(() => {
    alert('已复制到剪贴板！\n\n' + info)
  })
}

function deselect() {
  selectedEl.value = null
  Object.assign(highlightStyle, { display: 'none' })
}

function onKeydown(e) {
  // Ctrl+D to toggle design mode
  if (e.ctrlKey && e.key === 'd') {
    e.preventDefault()
    active.value = !active.value
    if (!active.value) deselect()
    return
  }
  if (e.key === 'Escape') {
    if (selectedEl.value) { deselect() }
    else { active.value = false }
  }
}

onMounted(() => {
  document.addEventListener('mousemove', onHover, true)
  document.addEventListener('click', onClick, true)
  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onHover, true)
  document.removeEventListener('click', onClick, true)
  document.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.dev-highlight {
  position: absolute;
  pointer-events: none;
  border: 2px solid #409eff;
  background: rgba(64, 158, 255, 0.08);
  z-index: 99998;
  transition: all 0.08s ease;
}
.dev-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 99997;
  pointer-events: none;
}
.dev-overlay > * { pointer-events: auto; }
.dev-toolbar {
  position: fixed;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: #1d1e1f;
  color: #fff;
  border-radius: 8px;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 99999;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
  font-size: 13px;
}
.dev-title { opacity: 0.7; }
.dev-trigger {
  position: fixed;
  bottom: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  box-shadow: 0 2px 12px rgba(64,158,255,0.4);
  transition: transform 0.2s;
}
.dev-trigger:hover { transform: scale(1.1); }
.dev-panel {
  position: absolute;
  width: 260px;
  background: #1d1e1f;
  color: #e5e5e5;
  border-radius: 8px;
  font-size: 12px;
  z-index: 99999;
  box-shadow: 0 4px 20px rgba(0,0,0,0.4);
  overflow: hidden;
}
.dev-panel-header {
  padding: 8px 12px;
  background: #2a2b2c;
  display: flex;
  align-items: center;
  gap: 6px;
}
.dev-tag { color: #409eff; font-weight: 600; }
.dev-class { color: #909399; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dev-btn-close {
  background: none; border: none; color: #909399; cursor: pointer;
  font-size: 18px; line-height: 1; padding: 0 4px;
}
.dev-btn-close:hover { color: #f56c6c; }
.dev-panel-body { padding: 10px 12px; }
.dev-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.dev-row label {
  width: 42px;
  flex-shrink: 0;
  color: #909399;
  text-align: right;
  font-size: 11px;
}
.dev-row input {
  flex: 1;
  background: #2a2b2c;
  border: 1px solid #3a3b3c;
  border-radius: 4px;
  color: #e5e5e5;
  padding: 4px 6px;
  font-size: 12px;
  outline: none;
  min-width: 0;
}
.dev-row input:focus { border-color: #409eff; }
.dev-panel-footer {
  padding: 8px 12px;
  background: #2a2b2c;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dev-btn {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 11px;
  cursor: pointer;
  white-space: nowrap;
}
.dev-btn:hover { background: #66b1ff; }
.dev-btn.on { background: #f56c6c; }
.dev-hint { color: #606266; font-size: 10px; margin-left: auto; }
</style>
