/**
 * Print a slip with the given content
 * @param {string} title - slip title
 * @param {string} htmlContent - inner HTML content
 */
export function printSlip(title, htmlContent) {
  const win = window.open('', '_blank')
  win.document.write(`<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>${title}</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: "Microsoft YaHei", sans-serif; padding: 30px; color: #333; font-size: 13px; }
  .slip-header { text-align: center; margin-bottom: 20px; }
  .slip-header h1 { font-size: 22px; margin-bottom: 6px; }
  .slip-header .sub { color: #666; font-size: 12px; }
  .info-section { display: flex; justify-content: space-between; margin-bottom: 16px; font-size: 13px; }
  .info-section .left, .info-section .right { display: flex; flex-direction: column; gap: 4px; }
  table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
  th, td { border: 1px solid #333; padding: 6px 10px; text-align: left; font-size: 13px; }
  th { background: #f0f0f0; font-weight: 600; }
  .footer { margin-top: 30px; display: flex; justify-content: space-between; font-size: 13px; }
  .footer .sign { width: 200px; border-bottom: 1px solid #333; height: 30px; display: inline-block; }
  @media print { body { padding: 20px; } }
</style></head><body>${htmlContent}
<script>window.onload=function(){window.print();}<\/script>
</body></html>`)
  win.document.close()
}