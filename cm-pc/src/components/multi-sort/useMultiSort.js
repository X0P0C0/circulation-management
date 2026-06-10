export function useMultiSort({ maxLevels = 3, defaultSorts = [], onChange } = {}) {
  const sorts = (defaultSorts || []).slice(0, maxLevels)

  function toggleSort(prop) {
    if (!prop) return
    const idx = sorts.findIndex((s) => s.prop === prop)
    if (idx === -1) {
      sorts.push({ prop, order: 'ascending' })
    } else if (sorts[idx].order === 'ascending') {
      sorts[idx].order = 'descending'
    } else {
      sorts.splice(idx, 1)
    }
    if (sorts.length > maxLevels) {
      sorts.splice(0, sorts.length - maxLevels)
    }
    onChange && onChange(sorts)
  }

  function getSortState(prop) {
    const idx = sorts.findIndex((s) => s.prop === prop)
    if (idx === -1) return { order: null, level: 0 }
    return { order: sorts[idx].order, level: idx + 1 }
  }

  function buildSortParams() {
    if (!sorts.length) return {}
    return {
      sortFields: sorts.map((s) => s.prop).join(','),
      sortOrders: sorts.map((s) => s.order).join(',')
    }
  }

  function localCompare(a, b, prop, order) {
    const va = a?.[prop]
    const vb = b?.[prop]
    if (va == null && vb == null) return 0
    if (va == null) return order === 'ascending' ? -1 : 1
    if (vb == null) return order === 'ascending' ? 1 : -1
    let result = 0
    if (typeof va === 'number' && typeof vb === 'number') {
      result = va - vb
    } else {
      result = String(va).localeCompare(String(vb), 'zh-Hans-CN')
    }
    return order === 'ascending' ? result : -result
  }

  function applyLocalSort(list, { mode = 'array' } = {}) {
    if (!Array.isArray(list)) return list
    const copy = [...list]
    if (!sorts.length) return copy
    copy.sort((a, b) => {
      for (const s of sorts) {
        const r = localCompare(a, b, s.prop, s.order)
        if (r !== 0) return r
      }
      return 0
    })
    return mode === 'reactive' ? copy : copy
  }

  return {
    sorts,
    toggleSort,
    getSortState,
    buildSortParams,
    applyLocalSort
  }
}
