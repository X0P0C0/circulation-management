# Git 工作流规范

> 配件流转管理系统  
> 版本：V1.0 | 更新日期：2026-05-27

---

## 1. 分支策略

### 1.1 分支模型

采用简化版 Git Flow，适合单人开发场景：

```
main (生产) ←── hotfix/xxx
  ↑
  merge
  ↑
 dev (开发) ←── feat/xxx
                fix/xxx
                docs/xxx
```

### 1.2 分支说明

| 分支 | 用途 | 来源 | 合并目标 | 生命周期 |
|------|------|------|---------|---------|
| `main` | 生产环境代码，始终保持可部署 | — | — | 永久 |
| `dev` | 日常开发集成分支 | `main` | `main` | 永久 |
| `feat/{name}` | 新功能开发 | `dev` | `dev` | 临时 |
| `fix/{name}` | Bug 修复 | `dev` | `dev` | 临时 |
| `hotfix/{name}` | 生产紧急修复 | `main` | `main` + `dev` | 临时 |
| `docs/{name}` | 文档更新 | `dev` | `dev` | 临时 |

### 1.3 分支命名

- 使用小写字母 + 连字符
- 简洁描述功能内容
- 示例：
  - `feat/accessory-inbound`
  - `feat/worker-crud`
  - `fix/barcode-duplicate-check`
  - `docs/api-spec`

---

## 2. 提交规范

### 2.1 Commit Message 格式

```
<type>(<scope>): <subject>

<body>（可选）

<footer>（可选）
```

### 2.2 Type 类型

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(backend): 实现配件入库接口` |
| `fix` | Bug 修复 | `fix(pc): 修复表格分页不刷新问题` |
| `docs` | 文档变更 | `docs: 更新 API 接口文档` |
| `style` | 代码格式（不影响逻辑） | `style(backend): 格式化代码` |
| `refactor` | 重构（非新功能、非修复） | `refactor(backend): 重构库存查询逻辑` |
| `test` | 测试相关 | `test(backend): 添加领用接口单元测试` |
| `chore` | 构建/工具变更 | `chore: 更新 .gitignore` |
| `perf` | 性能优化 | `perf(backend): 优化库存查询 SQL` |

### 2.3 Scope 范围

| Scope | 说明 |
|-------|------|
| `backend` | 后端代码变更 |
| `pc` | PC 前端变更 |
| `h5` | H5 前端变更 |
| `db` | 数据库脚本变更 |
| `docs` | 文档变更 |
| `config` | 配置文件变更 |

### 2.4 示例

```
feat(backend): 实现配件领用接口，支持批量操作

- 新增 FlowController.transferOut 接口
- 实现库存扣减和转移事务逻辑
- 添加条码不存在和库存不足的业务异常

Closes #12
```

```
fix(h5): 修复扫码组件在 iOS Safari 下无法调用摄像头

- 添加 navigator.mediaDevices 兼容性检测
- 降级方案：提示用户手动输入条码
```

---

## 3. 工作流程

### 3.1 日常开发流程

```bash
# 1. 从 dev 创建功能分支
git checkout dev
git pull origin dev
git checkout -b feat/worker-crud

# 2. 开发并提交
git add .
git commit -m "feat(backend): 实现师傅管理 CRUD 接口"

# 3. 开发完成，合并回 dev
git checkout dev
git merge feat/worker-crud

# 4. 删除功能分支
git branch -d feat/worker-crud
```

### 3.2 发布流程

```bash
# 1. dev 分支测试通过后，合并到 main
git checkout main
git merge dev
git tag -a v1.0.0 -m "Release v1.0.0"

# 2. 推送到远程
git push origin main --tags
```

### 3.3 紧急修复流程

```bash
# 1. 从 main 创建 hotfix 分支
git checkout main
git checkout -b hotfix/critical-fix

# 2. 修复并提交
git commit -m "fix(backend): 修复库存计算错误"

# 3. 合并回 main 和 dev
git checkout main && git merge hotfix/critical-fix
git checkout dev && git merge hotfix/critical-fix

# 4. 删除 hotfix 分支
git branch -d hotfix/critical-fix
```

---

## 4. .gitignore 配置

```gitignore
# === Java / Maven ===
target/
*.class
*.jar
*.war
*.log
.idea/
*.iml

# === Node.js ===
node_modules/
dist/
.env.local
.env.*.local

# === 数据库 ===
*.sql.bak

# === 敏感配置 ===
application-dev.yml
application-prod.yml

# === IDE ===
.vscode/
*.swp
*.swo

# === OS ===
.DS_Store
Thumbs.db

# === 本项目 ===
reference/
```

---

## 5. 代码提交检查清单

每次提交前确认：

- [ ] 代码能正常编译，无语法错误
- [ ] 不包含 `System.out.println` / `console.log` 调试代码
- [ ] 不包含硬编码的测试数据
- [ ] 不包含敏感信息（密码、密钥）
- [ ] 新增文件已加入 Git 跟踪
- [ ] commit message 符合格式规范
- [ ] 相关文档已同步更新
