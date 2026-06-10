/**
 * Export data to CSV file and trigger download
 * @param {string} filename - filename without extension
 * @param {string[]} headers - column headers
 * @param {string[][]} rows - data rows (array of arrays)
 */
export function exportCsv(filename, headers, rows) {
  const BOM = '\uFEFF'
  const csvContent = BOM + [
    headers.join(','),
    ...rows.map(row => row.map(cell => {
      const val = cell == null ? '' : String(cell)
      return val.includes(',') || val.includes('"') || val.includes('\n')
        ? '"' + val.replace(/"/g, '""') + '"'
        : val
    }).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename + '_' + new Date().toISOString().slice(0, 10) + '.csv'
  link.click()
  URL.revokeObjectURL(url)
}
/**
 * Format datetime string, replace 'T' with space
 * @param {string} val - datetime string like "2026-06-03T14:30:00"
 * @returns {string} formatted string like "2026-06-03 14:30:00"
 */
export function formatDate(val) {
  if (!val) return ''
  return String(val).replace('T', ' ')
}
