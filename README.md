# 🐑 薅了个毛（Haloy Wool）

> **本来不想薅的，但它实在太便宜了。**

薅羊毛信息社区 —— 一个完整的微信小程序 + Java 后台项目。用户可以发布、浏览、收藏各类薅羊毛优惠活动，管理员负责内容审核，系统通过积分机制激励用户贡献内容。

---

## 📖 目录

- [项目是什么](#项目是什么)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [第一步：环境准备（只需做一次）](#第一步环境准备只需做一次)
- [第二步：启动后台服务](#第二步启动后台服务)
- [第三步：启动小程序前端](#第三步启动小程序前端)
- [第四步：验证是否成功](#第四步验证是否成功)
- [设置管理员账号](#设置管理员账号)
- [API 接口文档](#api-接口文档)
- [常见问题](#常见问题)
- [后台功能说明](#后台功能说明)

---

## 项目是什么

### 功能概览

| 角色 | 能做什么 |
|------|----------|
| **游客**（未登录） | 浏览已上线的羊毛信息列表（看不到详情） |
| **普通用户**（微信登录后） | 查看详情、发布新信息、编辑/删除自己发布的信息、批量导入、积分兑换 |
| **管理员** | 审核信息、上线/下线/删除任意信息、管理所有内容 |

### 核心业务流程

```
用户微信登录 → 自动注册 → 发布薅羊毛信息 → 状态"待审核"
                                              ↓
                        管理员审核通过 → "已上线" + 发布者获得 1 积分
                        管理员驳回   → "已驳回" + 附带理由
                                              ↓
                        用户可用积分兑换商品（话费券/京东卡/视频会员等）
```

### 小程序页面

| 页面 | 功能 |
|------|------|
| 🏠 首页（羊毛广场） | 卡片列表、搜索、下拉刷新、上拉加载 |
| 📄 详情页 | 富文本内容、权限操作按钮 |
| ✏️ 发布/编辑页 | 表单填写、Excel 批量导入 |
| 👤 个人中心 | 积分、我的发布（按状态筛选）、管理员入口 |
| ⚙️ 审核管理 | 管理员专用：审核通过/驳回、上下线 |
| 🎁 积分兑换 | 建设中占位页 |
| 📊 积分明细 | 积分变动记录 |

---

## 技术栈

### 后台（Java）

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 8+ | 编程语言 |
| Spring Boot | 2.7.18 | Web 框架 |
| MyBatis-Plus | 3.5.5 | 数据库 ORM |
| MySQL | 5.7+ / 8.0+ | 数据库 |
| JWT (jjwt) | 0.11.5 | 登录凭证 |
| EasyExcel | 3.3.4 | Excel 批量导入 |
| Hutool | 5.8.25 | 工具库 |
| Maven | 3.6+ | 项目构建 |

### 前端（微信小程序）

| 技术 | 说明 |
|------|------|
| 微信小程序原生框架 | WXML + WXSS + JS |
| 自定义组件 | 空状态组件、广告位占位组件 |
| 全局状态管理 | app.globalData |

---

## 项目结构

```
薅了个毛/
│
├── HLGM-JAVA-SERVER/                  ← 后台项目
│   ├── pom.xml                        ← Maven 依赖配置
│   ├── sql/
│   │   └── init.sql                   ← 数据库建表脚本（第一步就要用）
│   └── src/main/
│       ├── resources/
│       │   └── application.yml        ← 配置文件（数据库密码、微信密钥）
│       └── java/com/wool/
│           ├── WoolApplication.java   ← 启动入口
│           ├── common/                ← 公共类（异常处理、枚举、常量）
│           ├── config/                ← 配置类（跨域、分页、时间填充）
│           ├── entity/                ← 数据库实体（User、WoolInfo 等）
│           ├── mapper/                ← 数据库操作接口
│           ├── dto/                   ← 请求参数类
│           ├── vo/                    ← 返回结果类
│           ├── util/                  ← 工具类（JWT）
│           ├── interceptor/           ← 拦截器（登录验证、管理员验证）
│           ├── service/               ← 业务逻辑层
│           └── controller/            ← 控制器层（API 接口）
│
└── hlgm-miniprogram/                  ← 小程序前端项目
    ├── app.js                         ← 全局逻辑（登录、状态管理）
    ├── app.json                       ← 全局配置（页面路由、TabBar）
    ├── app.wxss                       ← 全局样式（主题色 #FF6B35）
    ├── project.config.json            ← 开发者工具配置
    ├── images/                        ← Logo 和 TabBar 图标
    ├── utils/
    │   ├── request.js                 ← HTTP 请求封装（自动带 Token）
    │   ├── auth.js                    ← 登录鉴权工具
    │   └── util.js                    ← 工具函数
    ├── components/
    │   ├── empty/                     ← 空状态组件
    │   └── ad-banner/                 ← 广告位占位组件
    └── pages/
        ├── welcome/                   ← 开屏页（Logo + Slogan）
        ├── index/                     ← 首页 - 羊毛广场
        ├── detail/                    ← 详情页
        ├── publish/                   ← 发布/编辑页（含批量导入）
        ├── mine/                      ← 个人中心
        ├── admin/                     ← 审核管理（仅管理员）
        ├── exchange/                  ← 积分兑换（建设中）
        ├── points/                    ← 积分明细
        └── exchange-record/           ← 兑换记录（占位）
```

---

## 第一步：环境准备（只需做一次）

> ⚠️ 如果你的电脑上已经装好了 Java 8+、Maven 3.6+、MySQL 5.7+，可以跳到 [第二步：启动后台服务](#第二步启动后台服务)。

### 1.1 安装 Java

本项目需要 **Java 8 或更高版本**。

#### Windows 用户

1. 打开浏览器，访问 https://adoptium.net/
2. 点击 **"Latest LTS Release"** 下载 `.msi` 安装包
3. 双击运行，一路点 "Next"，保持默认设置
4. 安装完成后，打开 **命令提示符**（`Win + R`，输入 `cmd`，回车）
5. 验证安装：
   ```bash
   java -version
   ```
   看到 `openjdk version "1.8.0_xxx"` 或更高版本就是成功了

#### macOS 用户

```bash
brew install openjdk@8
java -version
```

#### Linux (Ubuntu/Debian) 用户

```bash
sudo apt update
sudo apt install openjdk-8-jdk -y
java -version
```

### 1.2 安装 Maven

Maven 是 Java 项目的「包管理工具」，负责下载项目依赖的第三方库。

#### Windows 用户

1. 访问 https://maven.apache.org/download.cgi
2. 下载 **Binary zip archive**（如 `apache-maven-3.9.6-bin.zip`）
3. 解压到一个没有中文和空格的路径，例如 `C:\maven`
4. 配置环境变量：
   - 右键「此电脑」→「属性」→「高级系统设置」→「环境变量」
   - 在「系统变量」中找到 `Path`，点击「编辑」
   - 点击「新建」，输入 `C:\maven\bin`（替换为你的实际路径）
   - 点击「确定」保存
5. **重新打开** 命令提示符，验证：
   ```bash
   mvn -version
   ```

#### macOS / Linux 用户

```bash
# macOS
brew install maven

# Linux
sudo apt install maven -y
```

### 1.3 安装 MySQL

#### Windows 用户

1. 访问 https://dev.mysql.com/downloads/installer/
2. 下载 **MySQL Installer**（选较大的那个，约 300MB+）
3. 运行安装程序：
   - 选择 "Developer Default" 或 "Server only"
   - 一路点 "Next"/"Execute"
   - 到 **设置密码** 界面时，输入一个 root 密码（**务必记住！**）
   - 端口保持默认 `3306`
4. 验证安装：
   ```bash
   mysql -u root -p
   ```
   输入密码后看到 `mysql>` 提示符就是成功了。输入 `exit;` 退出。

#### macOS 用户

```bash
brew install mysql
brew services start mysql
mysql_secure_installation
```

#### Linux 用户

```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql
sudo mysql_secure_installation
```

### 1.4 安装微信开发者工具

1. 访问 https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html
2. 下载对应系统的稳定版安装包
3. 安装后用微信扫码登录

### 1.5 （可选）安装 IDE

推荐使用以下任一工具编辑代码：

- **IntelliJ IDEA**（写 Java 后台）：https://www.jetbrains.com/idea/download/
- **VS Code**（写小程序前端）：https://code.visualstudio.com/

---

## 第二步：启动后台服务

### 2.1 创建数据库

打开命令提示符 / 终端，执行建表脚本：

```bash
cd 你的项目路径/HLGM-JAVA-SERVER/sql
mysql -u root -p < init.sql
```

输入密码后会自动创建数据库 `wool_db` 和 5 张表。

验证：

```bash
mysql -u root -p
```

```sql
USE wool_db;
SHOW TABLES;
```

应该看到：

```
+-------------------+
| Tables_in_wool_db |
+-------------------+
| t_exchange_goods  |
| t_exchange_record |
| t_points_log      |
| t_user            |
| t_wool_info       |
+-------------------+
```

输入 `exit;` 退出。

### 2.2 修改配置文件

用文本编辑器打开：

```
HLGM-JAVA-SERVER/src/main/resources/application.yml
```

**必须修改的项：**

```yaml
spring:
  datasource:
    password: 你的MySQL密码    ← 改成你在 1.3 步设置的密码
```

**建议修改的项：**

```yaml
wechat:
  appid: 你的小程序AppID       ← 在微信公众平台获取
  secret: 你的小程序AppSecret   ← 在微信公众平台获取
```

> 💡 如果还没有小程序账号，可以先用默认值，登录功能暂时不可用，其他功能正常。

### 2.3 编译项目

```bash
cd HLGM-JAVA-SERVER
mvn clean package -DskipTests
```

第一次编译需要下载依赖（约 100-200MB），等待 5-15 分钟。看到 `BUILD SUCCESS` 就是成功了。

### 2.4 启动服务

```bash
java -jar target/wool-backend-1.0.0.jar
```

看到 `Started WoolApplication in x.xx seconds` 就是启动成功。

> 💡 启动后这个窗口会被占用，不要关闭它。想后台运行可以用：
> - Windows: `start /B java -jar target/wool-backend-1.0.0.jar`
> - macOS/Linux: `nohup java -jar target/wool-backend-1.0.0.jar &`

---

## 第三步：启动小程序前端

### 3.1 修改配置

用文本编辑器打开小程序项目中的配置文件：

**修改后台地址** — 打开 `hlgm-miniprogram/app.js`：

```javascript
globalData: {
  userInfo: null,
  token: '',
  baseUrl: 'http://localhost:8080'   ← 如果后台部署在服务器上，改成服务器地址
}
```

**修改 AppID** — 打开 `hlgm-miniprogram/project.config.json`：

```json
"appid": "your_appid"    ← 改成你自己的小程序 AppID
```

### 3.2 导入项目到开发者工具

1. 打开 **微信开发者工具**
2. 点击 **「+」号** → **导入项目**
3. 目录选择 `hlgm-miniprogram` 文件夹
4. AppID 填写你自己的（或使用测试号）
5. 点击 **「确定」**

### 3.3 编译预览

导入成功后，开发者工具会自动编译。你可以：

- 在模拟器中预览（左侧手机屏幕）
- 点击 **「预览」** 按钮生成二维码，用真机扫码体验
- 点击 **「真机调试」** 进行真机调试

---

## 第四步：验证是否成功

### 验证后台

打开浏览器或新的命令提示符，执行：

```bash
curl http://localhost:8080/api/wool/list
```

返回 `"code":0` 就说明后台运行正常：

```json
{"code":0,"msg":"success","data":{"records":[],"total":0,"size":10,"current":1,"pages":0}}
```

### 验证前端

在微信开发者工具的模拟器中：

1. 首页应该显示「薅了个毛」品牌栏 + 搜索框 + 空状态提示
2. 点击底部「发布」Tab，应该看到发布表单 + 批量导入入口
3. 点击底部「我的」Tab，应该看到登录引导

---

## 设置管理员账号

第一个通过微信登录的用户默认是「普通用户」。需要手动在数据库中设置管理员：

```bash
mysql -u root -p
```

```sql
USE wool_db;

-- 查看所有用户
SELECT id, openid, nickname, role FROM t_user;

-- 将某个用户设为管理员（替换 '目标openid'）
UPDATE t_user SET role = 1 WHERE openid = '目标openid';

-- 验证
SELECT id, openid, nickname, role FROM t_user WHERE role = 1;
```

设置后，该用户在小程序的「个人中心」会看到「审核管理」入口。

---

## API 接口文档

### 通用说明

**服务器地址：** `http://localhost:8080`

**统一返回格式：**

```json
{
  "code": 0,          // 0=成功，非0=失败
  "msg": "success",   // 提示信息
  "data": { ... }     // 返回数据
}
```

**错误码：**

| code | 含义 |
|------|------|
| 0 | 成功 |
| 1 | 业务错误（积分不足、权限不够等） |
| 2 | 参数校验失败 |
| 401 | 未登录或 token 过期 |
| 403 | 权限不足（非管理员访问管理员接口） |
| 500 | 服务器内部错误 |

**登录认证：** 需要登录的接口，请求头携带：

```
Authorization: Bearer eyJhbG...x...
```

**分页参数：**

| 参数 | 默认值 | 说明 |
|------|--------|------|
| pageNum | 1 | 第几页（从 1 开始） |
| pageSize | 10 | 每页几条（最大 100） |

---

### 一、用户登录

#### 1.1 微信登录

```
POST /api/auth/login
无需 token
```

**请求参数：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | ✅ | 微信 `wx.login()` 获取的临时凭证 |
| nickname | String | ❌ | 用户昵称 |
| avatarUrl | String | ❌ | 用户头像 URL |

**请求示例：**

```json
{
  "code": "0a3lGd000xxxxxxxx",
  "nickname": "羊毛达人",
  "avatarUrl": "https://wx.qlogo.cn/mmopen/xxx/132"
}
```

**返回示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "nickname": "羊毛达人",
    "avatarUrl": "https://wx.qlogo.cn/mmopen/xxx/132",
    "role": 0,
    "points": 0
  }
}
```

---

### 二、羊毛信息（用户端）

#### 2.1 获取信息列表（公开）

```
GET /api/wool/list?pageNum=1&pageSize=10&keyword=关键词
无需 token
```

#### 2.2 获取信息详情（需登录）

```
GET /api/wool/detail/{id}
需要 token
```

**权限说明：**
- 已上线的信息：所有登录用户可查看
- 待审核/已驳回的信息：仅发布者本人和管理员可查看

#### 2.3 发布信息

```
POST /api/wool/publish
需要 token
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 标题，最多 128 字 |
| content | String | ✅ | 详细内容 |
| category | String | ❌ | 分类（如：会员、话费、外卖） |
| sourceUrl | String | ❌ | 来源链接 |
| claimSteps | String | ❌ | 领取步骤 |

#### 2.4 修改信息

```
PUT /api/wool/update/{id}
需要 token（只能修改自己的）
```

#### 2.5 删除信息

```
DELETE /api/wool/delete/{id}
需要 token（只能删除自己的）
```

#### 2.6 我的发布

```
GET /api/wool/mine?pageNum=1&pageSize=10
需要 token
```

#### 2.7 批量导入

```
POST /api/wool/import
Content-Type: multipart/form-data
需要 token
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | ✅ | Excel 文件（.xlsx） |

**返回示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "successCount": 5,
    "failCount": 1,
    "failDetails": ["第3行: 标题不能为空"]
  }
}
```

---

### 三、管理员接口

#### 3.1 管理员列表

```
GET /api/admin/wool/list?pageNum=1&pageSize=10&status=0&keyword=关键词
需要 token（管理员）
```

| 参数 | 说明 |
|------|------|
| status | 0=待审核，1=已上线，2=已驳回，3=已下线（不传则查全部） |
| keyword | 标题模糊搜索 |

#### 3.2 审核

```
POST /api/admin/wool/audit/{id}
需要 token（管理员）
```

| 字段 | 类型 | 说明 |
|------|------|------|
| action | Integer | 1=通过，2=驳回 |
| rejectReason | String | 驳回理由（action=2 时可选） |

#### 3.3 上线 / 下线

```
PUT /api/admin/wool/online/{id}    ← 上线
PUT /api/admin/wool/offline/{id}   ← 下线
需要 token（管理员）
```

#### 3.4 管理员删除

```
DELETE /api/admin/wool/delete/{id}
需要 token（管理员）
```

---

### 四、积分

#### 4.1 积分余额

```
GET /api/points/balance
需要 token
```

#### 4.2 积分记录

```
GET /api/points/log?pageNum=1&pageSize=20
需要 token
```

---

### 五、积分兑换

#### 5.1 商品列表

```
GET /api/exchange/goods
需要 token
```

#### 5.2 兑换商品

```
POST /api/exchange/do
需要 token
```

| 字段 | 类型 | 说明 |
|------|------|------|
| goodsId | Long | 商品 ID |

#### 5.3 兑换记录

```
GET /api/exchange/records?pageNum=1&pageSize=10
需要 token
```

---

## 常见问题

### Q: 编译报错 `程序包lombok不存在`

检查 `pom.xml` 中是否有 Lombok 依赖。如果有，确保 IDE 安装了 Lombok 插件。如果没有，说明代码中没用 Lombok，检查是否用了 `@Data` 注解。

### Q: 启动报错 `Communications link failure`

MySQL 连接失败。检查：
1. MySQL 服务是否启动：`mysql -u root -p` 能不能连上
2. `application.yml` 中的密码是否正确
3. 端口是否是 3306

### Q: 小程序请求报错 `url not in domain list`

在开发者工具中，点击右上角 **「详情」→「本地设置」**，勾选 **「不校验合法域名」**。

> ⚠️ 正式上线时需要在微信公众平台配置合法域名。

### Q: 小程序页面空白

检查 `app.js` 中的 `baseUrl` 是否指向了正确的后台地址。确保后台服务已启动。

### Q: 批量导入 Excel 没反应

确保上传的文件是 `.xlsx` 格式，且第一行是表头（标题、内容、分类、来源链接、领取步骤）。

### Q: Maven 下载依赖太慢

配置阿里云镜像源。在 Maven 安装目录的 `conf/settings.xml` 中添加：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Mirror</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

---

## 后台功能说明

### 数据库表

| 表名 | 说明 |
|------|------|
| t_user | 用户表（openid、昵称、头像、角色、积分） |
| t_wool_info | 羊毛信息表（标题、内容、分类、状态、浏览数） |
| t_points_log | 积分变动记录（类型、变动值、前后积分） |
| t_exchange_goods | 兑换商品表（名称、所需积分、库存） |
| t_exchange_record | 兑换记录表（用户、商品、消耗积分） |

### 信息状态流转

```
待审核(0) ──管理员通过──→ 已上线(1)
    │
    └──管理员驳回──→ 已驳回(2)
                        │
                        └──管理员上线──→ 已上线(1)

已上线(1) ──管理员下线──→ 已下线(3)
已下线(3) ──管理员上线──→ 已上线(1)
已驳回(2) ──管理员上线──→ 已上线(1)
```

### 积分规则

| 场景 | 积分变动 |
|------|----------|
| 发布信息审核通过 | +1 积分 |
| 积分兑换商品 | -对应积分 |
| 管理员手动调整 | 由管理员决定 |

---

## 📄 开源协议

本项目仅供学习交流使用。
