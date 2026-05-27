# API 接口规范

> 配件流转管理系统 | RESTful API 设计规范  
> 版本：V1.0 | 更新日期：2026-05-27

---

## 1. URL 设计规范

### 1.1 基本格式

```
http://{host}:{port}/api/{资源名}[/{操作}]
```

- 所有接口以 `/api` 为前缀。
- 资源名使用**复数名词**、**小写**、**连字符分隔**。
- 使用 HTTP 方法表示操作语义，不在 URL 中出现动词（特殊业务操作除外）。

### 1.2 HTTP 方法约定

| 方法 | 语义 | 示例 |
|------|------|------|
| GET | 查询 | `GET /api/accessories/{barcode}` |
| POST | 新增/执行操作 | `POST /api/accessories`（入库） |
| PUT | 更新 | `PUT /api/accessories/{id}` |
| DELETE | 删除 | `DELETE /api/workers/{id}` |

### 1.3 特殊业务操作

对于无法用标准 HTTP 方法表达的业务动作，使用 `POST /api/{资源}/{动作}` 格式：

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /api/auth/login` | POST | 登录 |
| `POST /api/auth/logout` | POST | 退出 |
| `POST /api/flows/transfer-out` | POST | 配件领用 |
| `POST /api/flows/transfer-in` | POST | 配件归还 |
| `POST /api/flows/sell` | POST | 配件售卖 |

---

## 2. 完整 API 清单

### 2.1 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | `/api/auth/login` | 管理员登录 | ❌ |
| POST | `/api/auth/logout` | 退出登录 | ✅ |
| GET | `/api/auth/info` | 获取当前用户信息 | ✅ |

### 2.2 配件管理 `/api/accessories`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | `/api/accessories` | 配件入库 | ✅ |
| GET | `/api/accessories/{barcode}` | 根据条码查询配件 | ✅ |
| PUT | `/api/accessories/{id}` | 更新配件信息 | ✅ |
| GET | `/api/accessories` | 配件列表（分页） | ✅ |

### 2.3 师傅管理 `/api/workers`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | `/api/workers` | 新增师傅 | ✅ |
| GET | `/api/workers` | 师傅列表（分页） | ✅ |
| GET | `/api/workers/all` | 师傅全量列表（下拉选择用） | ✅ |
| PUT | `/api/workers/{id}` | 编辑师傅 | ✅ |
| DELETE | `/api/workers/{id}` | 删除师傅 | ✅ |

### 2.4 分类管理 `/api/categories`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | `/api/categories` | 新增分类 | ✅ |
| GET | `/api/categories` | 分类列表 | ✅ |
| PUT | `/api/categories/{id}` | 编辑分类 | ✅ |
| DELETE | `/api/categories/{id}` | 删除分类 | ✅ |

### 2.5 库存查询 `/api/inventories`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | `/api/inventories` | 总库存查询（分页+筛选） | ✅ |
| GET | `/api/inventories/workers/{workerId}` | 师傅库存查询 | ✅ |
| GET | `/api/inventories/stats` | 库存统计概览 | ✅ |

### 2.6 流转操作 `/api/flows`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | `/api/flows/transfer-out` | 配件领用（支持批量） | ✅ |
| POST | `/api/flows/transfer-in` | 配件归还（支持批量） | ✅ |
| POST | `/api/flows/sell` | 配件售卖 | ✅ |
| GET | `/api/flows/trace/{barcode}` | 条码追溯 | ✅ |
| GET | `/api/flows/records` | 流转记录查询（分页+筛选） | ✅ |
| GET | `/api/flows/workers/{workerId}/records` | 师傅流转记录 | ✅ |

### 2.7 操作日志 `/api/logs`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | `/api/logs` | 操作日志查询（分页+筛选） | ✅ |

---

## 3. 请求规范

### 3.1 请求头

| Header | 值 | 说明 |
|--------|------|------|
| `Content-Type` | `application/json` | POST/PUT 请求体格式 |
| `Authorization` | `Bearer {token}` | JWT Token（登录后所有请求携带） |

### 3.2 查询参数规范

- 分页参数：`pageNum`（页码，从 1 开始）、`pageSize`（每页条数，默认 10）
- 排序参数：`orderBy`（排序字段）、`orderDir`（ASC/DESC）
- 筛选参数：直接用字段名，如 `categoryId=1`, `keyword=螺丝`

**示例**：
```
GET /api/inventories?pageNum=1&pageSize=20&categoryId=1&keyword=螺丝
```

### 3.3 请求体示例

**配件入库**
```json
{
  "barcode": "A20260527001",
  "name": "六角螺丝 M8x30",
  "spec": "M8x30mm",
  "categoryId": 1,
  "unit": "个",
  "remark": ""
}
```

**批量领用**
```json
{
  "workerId": 1,
  "barcodes": ["A20260527001", "A20260527002", "A20260527003"]
}
```

**配件售卖**
```json
{
  "barcode": "A20260527001",
  "customerName": "张三",
  "customerPhone": "13800138000",
  "remark": ""
}
```

---

## 4. 响应规范

### 4.1 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

### 4.2 分页响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 4.3 错误响应格式

```json
{
  "code": 400,
  "message": "条码不能为空",
  "data": null
}
```

### 4.4 错误码定义

| 错误码 | HTTP 状态码 | 含义 | 典型场景 |
|--------|:-----------:|------|---------|
| 200 | 200 | 成功 | — |
| 400 | 400 | 请求参数错误 | 必填项为空、格式不正确 |
| 401 | 401 | 未认证 | Token 缺失、过期、无效 |
| 403 | 403 | 无权限 | 非管理员访问 |
| 404 | 404 | 资源不存在 | 条码不存在、师傅不存在 |
| 409 | 409 | 业务冲突 | 条码已入库、库存不足、师傅名下有配件 |
| 500 | 500 | 服务器内部错误 | 未知异常 |

---

## 5. 接口文档维护

- 使用 **Apifox** 维护接口文档（推荐）。
- 每个接口必须包含：路径、方法、描述、请求参数说明、响应示例、错误码说明。
- 接口变更后同步更新文档。
- 前后端基于接口文档并行开发。
