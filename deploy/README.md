# CM 系统部署指南（Linux 服务器）

## 前置条件

- Java 17+
- MySQL 8.0+
- Nginx
- Node.js 20（仅首次构建需要）

## 第一步：本地构建

在开发机上执行：

```bash
build.bat
```

构建产物在 deploy/ 目录下：
- deploy/app/cm-backend-1.0.0.jar  — 后端 JAR
- deploy/pc/                        — PC 前端静态文件
- deploy/h5/                        — H5 前端静态文件
- deploy/nginx.conf                 — Nginx 配置
- deploy/cm.service                 — systemd 服务配置

## 第二步：上传到服务器

将 deploy/ 整个目录上传到服务器 /opt/cm/

```bash
scp -r deploy/* root@你的服务器IP:/opt/cm/
```

## 第三步：初始化数据库

在服务器上执行：

```bash
mysql -u root -p < /opt/cm/init.sql
```

## 第四步：配置 Nginx

```bash
cp /opt/cm/nginx.conf /etc/nginx/conf.d/cm.conf
nginx -t          # 测试配置
systemctl reload nginx   # 重载
```

## 第五步：配置后端服务

```bash
# 编辑生产数据库密码
vim /opt/cm/application-prod.yml

# 配置 systemd 服务
cp /opt/cm/cm.service /etc/systemd/system/
systemctl daemon-reload
systemctl start cm
systemctl enable cm    # 开机自启

# 查看日志
journalctl -u cm -f
```

## 第六步：检查

- 浏览器访问 http://服务器IP/
- 后端 API：http://服务器IP/api/dashboard/stats
- 默认登录：admin / admin123

## 常用命令

```bash
systemctl status cm        # 查看状态
systemctl restart cm       # 重启
journalctl -u cm -f        # 查看实时日志
```