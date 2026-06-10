<template>
  <span class="sort-header" :class="{ 'is-sorted': !!state.order }" @click.stop="$emit('sort')">
    <span class="sort-header__text"><slot /></span>
    <span class="sort-header__icons">
      <el-icon class="sort-header__icon sort-header__icon--up" :class="{ 'is-active': state.order === 'ascending' }">
        <CaretTop />
      </el-icon>
      <el-icon class="sort-header__icon sort-header__icon--down" :class="{ 'is-active': state.order === 'descending' }">
        <CaretBottom />
      </el-icon>
    </span>
    <span v-if="state.level > 0" class="sort-header__level">{{ state.level }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'

const props = defineProps({
  state: { type: Object, default: () => ({ order: null, level: 0 }) }
})

const state = computed(() => ({
  order: props.state?.order || null,
  level: props.state?.level || 0
}))
</script>

<style scoped>
.sort-header {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  user-select: none;
  line-height: 1;
}
.sort-header__text {
  white-space: nowrap;
}
.sort-header__icons {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
  color: #c0c4cc;
}
.sort-header__icon {
  font-size: 10px;
  line-height: 10px;
}
.sort-header__icon--up.is-active {
  color: #409eff;
}
.sort-header__icon--down.is-active {
  color: #67c23a;
}
.sort-header__level {
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  font-size: 10px;
  color: #fff;
  background: #909399;
  border-radius: 8px;
}
.sort-header.is-sorted .sort-header__level {
  background: #409eff;
}
</style>
