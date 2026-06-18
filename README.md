# 薅羊毛信息社区 — 后台服务

一个为微信小程序提供后端 API 服务的项目，用于社区化管理「薅羊毛」优惠信息。用户可以发布、浏览、收藏各类优惠活动，管理员负责内容审核，系统通过积分机制激励用户贡献内容。

---

## 目录

- [项目是什么](#项目是什么)
- [技术栈](#技术栈)
- [项目文件结构](#项目文件结构)
- [从零开始：环境准备](#从零开始环境准备)
  - [第一步：安装 Java](#第一步安装-java)
  - [第二步：安装 Maven](#第二步安装-maven)
  - [第三步：安装 MySQL](#第三步安装-mysql)
- [启动项目](#启动项目)
  - [第四步：创建数据库](#第四步创建数据库)
  - [第五步：修改项目配置](#第五步修改项目配置)
  - [第六步：编译项目](#第六步编译项目)
  - [第七步：运行项目](#第七步运行项目)
  - [第八步：验证项目是否启动成功](#第八步验证项目是否启动成功)
- [设置管理员账号](#设置管理员账号)
- [API 接口文档](#api-接口文档)
  - [通用说明](#通用说明)
  - [一、用户登录](#一用户登录)
  - [二、羊毛信息（用户端）](#二羊毛信息用户端)
  - [三、管理员接口](#三管理员接口)
  - [四、积分](#四积分)
  - [五、积分兑换](#五积分兑换)
- [业务流程图](#业务流程图)
- [常见问题排查](#常见问题排查)
- [生产环境部署建议](#生产环境部署建议)

---

## 项目是什么

### 功能概述

| 角色 | 能做什么 |
|------|----------|
| **游客**（未登录） | 浏览已上线的羊毛信息列表（看不到详情） |
| **普通用户**（微信登录后） | 查看详情、发布新信息、编辑/删除自己发布的信息 |
| **管理员** | 审核信息、上线/下线/删除任意信息、管理所有内容 |

### 核心业务流程

1. 用户通过微信小程序登录 → 系统自动注册并返回登录凭证
2. 登录用户可以发布「薅羊毛」信息发布后进入「待审核」状态
3. 管理员审核通过 → 信息上线，发布者自动获得 1 积分奖励
4. 管理员驳回 → 信息标记为驳回，附带驳回理由
5. 用户可以用积分兑换商品

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 8+ | 编程语言 |
| Spring Boot | 2.7.18 | Web 框架 |
| MyBatis-Plus | 3.5.5 | 数据库操作框架 |
| MySQL | 5.7+ / 8.0+ | 数据库 |
| JWT (jjwt) | 0.11.5 | 用户登录凭证 |
| Hutool | 5.8.25 | 工具库（HTTP 请求等） |
| Lombok | — | 简化代码 |
| Maven | 3.6+ | 项目构建工具 |

---

## 项目文件结构

```
wool-backend/
│
├── sql/
│   └── init.sql                          ← 数据库建表脚本（第一步就要用到）
│
├── pom.xml                               ← Maven 依赖配置（相当于"购物清单"）
│
├── README.md                             ← 就是你现在看的这个文件
│
└── src/main/
    ├── resources/
    │   └── application.yml               ← 项目配置文件（数据库密码、微信密钥等写在这里）
    │
    └── java/com/wool/
        ├── WoolApplication.java          ← 项目启动入口
        │
        ├── common/                       ← 公共工具类
        │   ├── R.java                    ← 统一返回格式 {"code":0,"msg":"success","data":...}
        │   ├── BizException.java         ← 业务异常类
        │   ├── GlobalExceptionHandler.java ← 全局异常处理（捕获所有错误并返回友好提示）
        │   ├── Constants.java            ← 常量定义
        │   ├── WoolStatus.java           ← 信息状态枚举：待审核/已上线/驳回/已下线
        │   └── PointsChangeType.java     ← 积分变动类型枚举
        │
        ├── config/                       ← 配置类
        │   ├── WebMvcConfig.java         ← 拦截器注册 & 跨域配置
        │   ├── MybatisPlusConfig.java    ← MyBatis-Plus 分页插件
        │   └── MyMetaObjectHandler.java  ← 自动填充创建时间/更新时间
        │
        ├── entity/                       ← 数据库实体类（和数据库表一一对应）
        │   ├── User.java                 ← 用户表
        │   ├── WoolInfo.java             ← 羊毛信息表
        │   ├── PointsLog.java            ← 积分变动记录表
        │   ├── ExchangeGoods.java        ← 兑换商品表
        │   └── ExchangeRecord.java       ← 兑换记录表
        │
        ├── mapper/                       ← 数据库操作接口（MyBatis-Plus 自动生成 SQL）
        │   ├── UserMapper.java
        │   ├── WoolInfoMapper.java
        │   ├── PointsLogMapper.java
        │   ├── ExchangeGoodsMapper.java
        │   └── ExchangeRecordMapper.java
        │
        ├── dto/                          ← 请求参数类（前端传给后端的数据格式）
        │   ├── WxLoginDTO.java           ← 微信登录请求
        │   ├── WoolInfoDTO.java          ← 发布/修改信息请求
        │   ├── AuditDTO.java             ← 审核请求
        │   └── ExchangeDTO.java          ← 兑换请求
        │
        ├── vo/                           ← 返回结果类（后端返回给前端的数据格式）
        │   ├── LoginVO.java              ← 登录返回
        │   ├── WoolInfoVO.java           ← 信息详情返回
        │   └── PointsLogVO.java          ← 积分记录返回
        │
        ├── util/
        │   └── JwtUtil.java              ← JWT 工具类（生成/解析登录凭证）
        │
        ├── interceptor/                  ← 拦截器（相当于"门卫"）
        │   ├── AuthInterceptor.java      ← 登录验证：检查请求是否携带有效 token
        │   └── AdminInterceptor.java     ← 管理员验证：检查当前用户是否是管理员
        │
        ├── service/                      ← 业务逻辑层（核心代码在这里）
        │   ├── AuthService.java          ← 登录服务接口
        │   ├── WoolInfoService.java      ← 信息服务接口
        │   ├── PointsService.java        ← 积分服务接口
        │   ├── ExchangeService.java      ← 兑换服务接口
        │   └── impl/                     ← 接口的具体实现
        │       ├── AuthServiceImpl.java
        │       ├── WoolInfoServiceImpl.java
        │       ├── PointsServiceImpl.java
        │       └── ExchangeServiceImpl.java
        │
        └── controller/                   ← 控制器层（接收 HTTP 请求，返回结果）
            ├── AuthController.java       ← 登录相关接口
            ├── WoolInfoController.java   ← 羊毛信息相关接口
            ├── AdminController.java      ← 管理员接口
            ├── PointsController.java     ← 积分相关接口
            └── ExchangeController.java   ← 兑换相关接口
```

---

## 从零开始：环境准备

> ⚠️ 如果你的电脑上已经装好了 Java 8+、Maven 3.6+、MySQL 5.7+，可以跳到 [第四步：创建数据库](#第四步创建数据库)。

### 第一步：安装 Java

本项目需要 **Java 8 或更高版本**。

#### Windows 用户

1. 打开浏览器，访问 https://adoptium.net/
2. 点击 **"Latest LTS Release"** 下载按钮，下载 `.msi` 安装包
3. 双击运行安装包，一路点 "Next"，保持默认设置即可
4. 安装完成后，打开 **命令提示符**（按 `Win + R`，输入 `cmd`，回车）
5. 输入以下命令验证安装：
   ```bash
   java -version
   ```
   看到类似 `openjdk version "1.8.0_xxx"` 或更高版本的输出就是成功了

#### macOS 用户

1. 打开 **终端**（在 Spotlight 搜索 "终端" 或 "Terminal"）
2. 输入：
   ```bash
   brew install openjdk@8
   ```
   如果提示 `brew: command not found`，先安装 Homebrew：
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
3. 验证安装：
   ```bash
   java -version
   ```

#### Linux (Ubuntu/Debian) 用户

```bash
sudo apt update
sudo apt install openjdk-8-jdk -y
java -version
```

---

### 第二步：安装 Maven

Maven 是 Java 项目的「包管理工具」，负责下载项目依赖的第三方库。

#### Windows 用户

1. 访问 https://maven.apache.org/download.cgi
2. 下载 **Binary zip archive**（如 `apache-maven-3.9.6-bin.zip`）
3. 解压到一个没有中文和空格的路径，例如 `C:\maven`
4. **配置环境变量**：
   - 右键「此电脑」→「属性」→「高级系统设置」→「环境变量」
   - 在「系统变量」中找到 `Path`，点击「编辑」
   - 点击「新建」，输入 `C:\maven\bin`（替换为你的实际路径）
   - 点击「确定」保存所有窗口
5. **重新打开** 命令提示符，验证：
   ```bash
   mvn -version
   ```

#### macOS / Linux 用户

```bash
# macOS
brew install maven

# Linux (Ubuntu/Debian)
sudo apt install maven -y
```

验证：
```bash
mvn -version
```

---

### 第三步：安装 MySQL

本项目使用 MySQL 数据库存储所有数据。

#### Windows 用户

1. 访问 https://dev.mysql.com/downloads/installer/
2. 下载 **MySQL Installer**（选较大的那个，约 300MB+）
3. 运行安装程序：
   - 选择 "Developer Default" 或 "Server only"
   - 一路点 "Next"/"Execute"
   - 到 **设置密码** 界面时，输入一个 root 密码（**务必记住这个密码！**）
   - 端口保持默认 `3306`
   - 继续点 "Next"/"Finish"
4. 验证安装：打开命令提示符，输入：
   ```bash
   mysql -u root -p
   ```
   输入你设置的密码，看到 `mysql>` 提示符就是成功了。输入 `exit;` 退出。

#### macOS 用户

```bash
brew install mysql
brew services start mysql

# 设置 root 密码（如果是全新安装）
mysql_secure_installation
```

#### Linux (Ubuntu/Debian) 用户

```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql

# 设置 root 密码
sudo mysql_secure_installation
```

#### 验证 MySQL 是否正常运行

```bash
mysql -u root -p
```

输入密码后看到 `mysql>` 提示符即可。输入 `exit;` 退出。

---

## 启动项目

### 第四步：创建数据库

> 这一步是用 SQL 脚本自动创建数据库和所有数据表，并插入一些初始数据。

1. 打开命令提示符 / 终端

2. 进入项目的 `sql` 目录：
   ```bash
   cd 你的项目路径/wool-backend/sql
   ```
   例如 Windows 上可能是：
   ```bash
   cd C:\Users\你的用户名\Desktop\wool-backend\sql
   ```

3. 执行建表脚本：
   ```bash
   mysql -u root -p < init.sql
   ```
   输入 MySQL 密码后，脚本会自动执行。

4. **验证是否成功**：登录 MySQL 检查
   ```bash
   mysql -u root -p
   ```
   进入 MySQL 后依次输入：
   ```sql
   USE wool_db;
   SHOW TABLES;
   ```
   应该看到 5 张表：
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

   再查看商品表是否有初始数据：
   ```sql
   SELECT * FROM t_exchange_goods;
   ```
   应该有 3 条商品记录。输入 `exit;` 退出。

---

### 第五步：修改项目配置

用任意文本编辑器（记事本、VS Code、Notepad++ 等）打开：

```
wool-backend/src/main/resources/application.yml
```

需要修改以下几项：

#### 5.1 数据库连接（必须修改）

找到这一段：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wool_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password    ← 改成你的 MySQL 密码
```

**把 `password: your_password` 改成你在第三步设置的 MySQL root 密码。**

如果你的 MySQL 不是装在本机、或者端口不是 3306，还需要改 `url` 中的 `localhost:3306` 部分。

#### 5.2 微信小程序配置（可稍后修改）

```yaml
wechat:
  appid: your_appid       ← 改成你的微信小程序 AppID
  secret: your_secret     ← 改成你的微信小程序 AppSecret
```

> **如果你还没有微信小程序**：可以先用测试值，登录接口会报错，但其他功能可以正常测试。后面在 [附录：获取微信 AppID](#附录获取微信-appid) 中会说明如何获取。

#### 5.3 JWT 密钥（建议修改）

```yaml
jwt:
  secret: your-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256
```

建议改成一个随机字符串，长度至少 32 个字符。可以用这个在线工具生成：https://www.random.org/strings/ （选 64 个字符的随机字符串）。

#### 5.4 完整配置示例

修改后的配置大概长这样：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wool_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: MySql2024!        # ← 你的实际密码
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

wechat:
  appid: wx1234567890abcdef     # ← 你的实际 AppID
  secret: abcdef1234567890abcdef # ← 你的实际 AppSecret

jwt:
  secret: aB3kF7mN9pQ2sU5vX8zA1cE4gH6jL0nRtWyDfGiKmOp    # ← 你自己生成的随机字符串
  expiration: 604800000

logging:
  level:
    com.wool: debug
```

**保存文件。**

---

### 第六步：编译项目

> 编译就是把源代码变成可以运行的程序。Maven 会自动下载所有需要的第三方库。

1. 打开命令提示符 / 终端

2. 进入项目根目录（**不是 sql 目录，是 wool-backend 目录**）：
   ```bash
   cd 你的项目路径/wool-backend
   ```

3. 执行编译命令：
   ```bash
   mvn clean package -DskipTests
   ```

4. **等待编译完成**。第一次编译需要下载大量依赖（约 100-200MB），取决于你的网速，可能需要 5-15 分钟。看到 `BUILD SUCCESS` 就是成功了：

   ```
   [INFO] -------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] -------------------------------------------------------
   [INFO] Total time:  xx.xxx s
   ```

> ⚠️ 如果编译失败，最常见的原因是：
> - 网络问题导致依赖下载失败 → 重试一次，或者配置 Maven 镜像源（见 [常见问题](#常见问题排查)）
> - Java 版本不对 → 确认 `java -version` 显示的是 1.8 或更高
> - 配置文件语法错误 → 检查 application.yml 的缩进是否正确

---

### 第七步：运行项目

编译成功后，在同一个目录下执行：

```bash
java -jar target/wool-backend-1.0.0.jar
```

你会看到大量日志输出，最后一行类似这样就是启动成功：

```
Started WoolApplication in x.xx seconds (process running for x.xxx)
```

> ⚠️ **注意**：启动后这个命令行窗口会被占用，不要关闭它。如果想后台运行，可以用：
> - Windows: `start /B java -jar target/wool-backend-1.0.0.jar`
> - Linux/macOS: `nohup java -jar target/wool-backend-1.0.0.jar &`

---

### 第八步：验证项目是否启动成功

打开一个新的命令提示符 / 终端窗口，执行：

```bash
curl http://localhost:8080/api/wool/list
```

如果返回类似这样的 JSON，说明项目运行正常：

```json
{"code":0,"msg":"success","data":{"records":[],"total":0,"size":10,"current":1,"pages":0}}
```

返回 `"code":0` 就是成功了。`records` 为空是正常的，因为还没有人发布过信息。

**🎉 恭喜，项目已成功启动！**

---

## 设置管理员账号

项目启动后，第一个通过微信登录的用户默认是「普通用户」。需要手动在数据库中将某个用户提升为管理员：

1. 登录 MySQL：
   ```bash
   mysql -u root -p
   ```

2. 执行以下 SQL（先让目标用户通过小程序登录一次，系统会自动创建用户记录）：
   ```sql
   USE wool_db;

   -- 查看所有用户
   SELECT id, openid, nickname, role FROM t_user;

   -- 将某个用户设为管理员（把 '目标用户的openid' 替换成实际值）
   UPDATE t_user SET role = 1 WHERE openid = '目标用户的openid';
   ```

3. 验证：
   ```sql
   SELECT id, openid, nickname, role FROM t_user WHERE role = 1;
   ```
   应该能看到该用户的 `role` 已变为 `1`。

---

## API 接口文档

### 通用说明

#### 服务器地址

```
http://localhost:8080
```

#### 统一返回格式

所有接口都返回以下格式的 JSON：

```json
{
  "code": 0,          // 0 表示成功，非 0 表示失败
  "msg": "success",   // 提示信息
  "data": { ... }     // 返回数据（成功时）
}
```

错误码说明：
| code | 含义 |
|------|------|
| 0 | 成功 |
| 1 | 业务错误（如积分不足、权限不够） |
| 2 | 参数校验失败 |
| 401 | 未登录或 token 过期 |
| 403 | 权限不足（非管理员访问管理员接口） |
| 500 | 服务器内部错误 |

#### 登录认证方式

需要登录的接口，必须在请求头（Header）中携带 token：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxxx...
```

> token 在调用登录接口后获得，有效期 7 天。

#### 分页参数

支持分页的接口使用以下参数：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| pageNum | 1 | 第几页（从 1 开始） |
| pageSize | 10 | 每页几条（最大 100） |

分页返回结构：
```json
{
  "records": [...],   // 数据列表
  "total": 100,       // 总记录数
  "size": 10,         // 每页大小
  "current": 1,       // 当前页码
  "pages": 10         // 总页数
}
```

---

### 一、用户登录

#### 1.1 微信登录

小程序端调用 `wx.login()` 获取临时 code，然后将 code 发给本接口。

```
POST /api/auth/login
Content-Type: application/json
无需 token
```

**请求参数：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | ✅ | 微信 `wx.login()` 获取的临时登录凭证 |
| nickname | String | ❌ | 用户昵称（首次登录建议传） |
| avatarUrl | String | ❌ | 用户头像 URL（首次登录建议传） |

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
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6MCwiaWF0IjoxNz...",
    "userId": 1,
    "nickname": "羊毛达人",
    "avatarUrl": "https://wx.qlogo.cn/mmopen/xxx/132",
    "role": 0,
    "points": 0
  }
}
```

**返回字段说明：**

| 字段 | 说明 |
|------|------|
| token | 登录凭证，后续请求需携带在 Header 中 |
| userId | 用户 ID |
| nickname | 昵称 |
| avatarUrl | 头像 URL |
| role | 角色：0=普通用户，1=管理员 |
| points | 当前积分余额 |

---

### 二、羊毛信息（用户端）

#### 2.1 获取已上线信息列表

公开接口，无需登录。只能看到审核通过且已上线的信息。

```
GET /api/wool/list?pageNum=1&pageSize=10&keyword=关键词
无需 token
```

**请求参数：**

| 参数 | 必填 | 说明 |
|------|------|------|
| pageNum | ❌ | 页码，默认 1 |
| pageSize | ❌ | 每页条数，默认 10 |
| keyword | ❌ | 搜索关键词（按标题模糊匹配） |

**返回示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 2,
        "authorName": "羊毛达人",
        "title": "京东PLUS会员免费领7天",
        "content": "打开京东APP，搜索...",
        "category": "会员",
        "sourceUrl": "https://jd.com/xxx",
        "status": 1,
        "statusDesc": "已上线",
        "rejectReason": "",
        "viewCount": 42,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T12:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

#### 2.2 获取信息详情

需要登录。普通用户只能查看「已上线」的和「自己发布的」信息。

```
GET /api/wool/detail/{id}
需要 token
```

**路径参数：**

| 参数 | 说明 |
|------|------|
| id | 信息 ID |

**返回示例：** 同列表中的单条记录结构。

**权限说明：**
- 已上线的信息：所有登录用户可查看
- 待审核/已驳回的信息：仅发布者本人和管理员可查看
- 已下线的信息：仅管理员可查看

#### 2.3 发布羊毛信息

```
POST /api/wool/publish
Content-Type: application/json
需要 token
```

**请求参数：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 标题（最长 128 字） |
| content | String | ✅ | 详细内容 |
| category | String | ❌ | 分类，如"会员"、"优惠券"、"话费" |
| sourceUrl | String | ❌ | 来源链接 |

**请求示例：**

```json
{
  "title": "支付宝扫码领红包",
  "content": "打开支付宝，扫描下方二维码，可领取最高99元消费红包...",
  "category": "红包",
  "sourceUrl": "https://www.alipay.com"
}
```

**返回示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": 15
}
```

`data` 为新创建的信息 ID。发布后状态为「待审核」。

#### 2.4 修改自己的信息

```
PUT /api/wool/update/{id}
Content-Type: application/json
需要 token
```

**路径参数：** `id` — 信息 ID

**请求参数：** 同发布接口。

**权限：** 只能修改自己发布的信息。

**注意：** 修改后信息状态会重置为「待审核」，需要管理员重新审核。

#### 2.5 删除自己的信息

```
DELETE /api/wool/delete/{id}
需要 token
```

**路径参数：** `id` — 信息 ID

**权限：** 只能删除自己发布的信息。

#### 2.6 查询我发布的信息

```
GET /api/wool/mine?pageNum=1&pageSize=10
需要 token
```

返回当前登录用户发布的所有信息（含各种状态）。

---

### 三、管理员接口

> 所有管理员接口都需要管理员权限。非管理员访问会返回 `{"code":403,"msg":"需要管理员权限"}`。

#### 3.1 查询所有信息（含各状态）

```
GET /api/admin/wool/list?pageNum=1&pageSize=10&status=0&keyword=关键词
需要 token + 管理员权限
```

**请求参数：**

| 参数 | 必填 | 说明 |
|------|------|------|
| pageNum | ❌ | 页码 |
| pageSize | ❌ | 每页条数 |
| status | ❌ | 筛选状态：0=待审核，1=已上线，2=已驳回，3=已下线。不传则返回全部 |
| keyword | ❌ | 搜索关键词 |

#### 3.2 审核信息

```
POST /api/admin/wool/audit/{id}
Content-Type: application/json
需要 token + 管理员权限
```

**路径参数：** `id` — 信息 ID

**请求参数：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | Integer | ✅ | 1=审核通过，2=审核驳回 |
| rejectReason | String | 驳回时必填 | 驳回理由 |

**审核通过示例：**

```json
{
  "action": 1
}
```

**审核驳回示例：**

```json
{
  "action": 2,
  "rejectReason": "内容不完整，请补充领取步骤"
}
```

**效果：**
- 通过：信息状态变为「已上线」，发布者自动获得 **1 积分**
- 驳回：信息状态变为「审核驳回」，记录驳回理由

#### 3.3 上线信息

将已下线或已驳回的信息重新上线。

```
PUT /api/admin/wool/online/{id}
需要 token + 管理员权限
```

#### 3.4 下线信息

将已上线的信息下线（用户将无法看到）。

```
PUT /api/admin/wool/offline/{id}
需要 token + 管理员权限
```

#### 3.5 删除任意信息

```
DELETE /api/admin/wool/delete/{id}
需要 token + 管理员权限
```

---

### 四、积分

#### 4.1 查询积分余额

```
GET /api/points/balance
需要 token
```

**返回示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": 15
}
```

#### 4.2 查询积分变动记录

```
GET /api/points/log?pageNum=1&pageSize=10
需要 token
```

**返回示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "changeType": 1,
        "changeTypeDesc": "发布奖励",
        "changeValue": 1,
        "beforePoints": 0,
        "afterPoints": 1,
        "remark": "信息审核通过奖励",
        "createdAt": "2024-01-15T14:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

**积分变动类型：**

| changeType | 说明 |
|------------|------|
| 1 | 发布奖励（信息审核通过 +1） |
| 2 | 兑换扣减 |
| 3 | 管理员调整 |

---

### 五、积分兑换

#### 5.1 查询可兑换商品列表

公开接口，无需登录。

```
GET /api/exchange/goods?pageNum=1&pageSize=10
无需 token
```

**返回示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "5元话费券",
        "description": "满10元可用话费券",
        "imageUrl": "",
        "pointsCost": 10,
        "stock": 100,
        "status": 1
      },
      {
        "id": 2,
        "name": "10元京东E卡",
        "description": "京东购物卡",
        "imageUrl": "",
        "pointsCost": 20,
        "stock": 50,
        "status": 1
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 5.2 兑换商品

```
POST /api/exchange/do
Content-Type: application/json
需要 token
```

**请求参数：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| goodsId | Long | ✅ | 商品 ID |
| quantity | Integer | ✅ | 兑换数量（最少 1） |

**请求示例：**

```json
{
  "goodsId": 1,
  "quantity": 2
}
```

**成功返回：**

```json
{
  "code": 0,
  "msg": "success",
  "data": null
}
```

**可能的错误：**
- `"积分不足，当前积分: 5"` — 积分不够
- `"库存不足，当前库存: 3"` — 库存不够
- `"商品已下架"` — 商品不可兑换

#### 5.3 查询我的兑换记录

```
GET /api/exchange/records?pageNum=1&pageSize=10
需要 token
```

---

## 业务流程图

```
┌─────────────────────────────────────────────────────────────┐
│                      用户发布羊毛信息                         │
│                           │                                  │
│                           ▼                                  │
│                    ┌─────────────┐                           │
│                    │  待审核 (0)  │                           │
│                    └──────┬──────┘                           │
│                           │                                  │
│                    管理员审核                                 │
│                    ┌──────┴──────┐                           │
│                    ▼             ▼                            │
│            ┌──────────┐   ┌──────────┐                       │
│            │ 通过 (1)  │   │ 驳回 (2)  │                       │
│            │ 已上线     │   │ 附理由    │                       │
│            │ 发布者+1积分│   └──────────┘                       │
│            └─────┬────┘                                      │
│                  │                                            │
│           管理员可下线                                         │
│                  ▼                                            │
│           ┌──────────┐                                       │
│           │ 已下线 (3) │                                       │
│           └─────┬────┘                                       │
│                 │                                             │
│          管理员可重新上线                                      │
│                 ▼                                             │
│           ┌──────────┐                                       │
│           │ 已上线 (1) │                                       │
│           └──────────┘                                       │
│                                                              │
│  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │
│                                                              │
│  用户修改自己的信息 → 状态重置为「待审核」→ 需管理员重新审核    │
│                                                              │
│  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │
│                                                              │
│  积分来源：信息审核通过 +1                                     │
│  积分用途：兑换商品（话费券、京东卡、视频会员等）              │
│  并发安全：积分扣减使用条件更新，防止超扣                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 常见问题排查

### Q1: 编译时报 `Could not resolve dependencies`

**原因：** Maven 下载依赖失败，通常是网络问题。

**解决方法：**

方法一：重试（有时是临时网络波动）
```bash
mvn clean package -DskipTests
```

方法二：配置阿里云镜像源（国内用户推荐）

编辑 Maven 配置文件：
- Windows: `C:\Users\你的用户名\.m2\settings.xml`
- macOS/Linux: `~/.m2/settings.xml`

如果文件不存在，新建一个，内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

然后重新编译。

---

### Q2: 启动报 `Communications link failure` 或 `Connection refused`

**原因：** 连接不上 MySQL 数据库。

**排查步骤：**

1. 确认 MySQL 是否在运行：
   ```bash
   # Windows
   sc query MySQL80

   # Linux
   sudo systemctl status mysql

   # macOS
   brew services list
   ```

2. 确认密码是否正确：
   ```bash
   mysql -u root -p
   ```

3. 确认数据库是否已创建：
   ```bash
   mysql -u root -p -e "SHOW DATABASES;"
   ```
   应该能看到 `wool_db`。

4. 检查 `application.yml` 中的 `url`、`username`、`password` 是否正确。

---

### Q3: 启动报 `Access denied for user 'root'@'localhost'`

**原因：** MySQL 密码错误。

**解决：** 修改 `application.yml` 中的 `password` 为你实际的 MySQL root 密码。

---

### Q4: 启动报 `Table 'wool_db.t_user' doesn't exist`

**原因：** 数据库表没有创建。

**解决：** 重新执行第四步的建表脚本。

---

### Q5: 端口 8080 被占用

**报错信息：** `Web server failed to start. Port 8080 was already in use.`

**解决方法：**

方法一：找到并关闭占用 8080 端口的程序
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# Linux/macOS
lsof -i :8080
kill -9 <进程ID>
```

方法二：修改项目端口

编辑 `application.yml`，将 `port: 8080` 改为其他端口，如 `port: 8081`。

---

### Q6: 微信登录报错

**可能原因：**
- `appid` 或 `secret` 配置错误
- 没有配置正确的服务器域名（正式环境）

**解决：**
- 检查 `application.yml` 中的微信配置
- 开发阶段可以使用微信开发者工具的「不校验合法域名」选项

---

### Q7: `mvn` 命令提示 `command not found`

**原因：** Maven 没有安装，或者没有配置到环境变量。

**解决：**
- 确认已安装 Maven（见第二步）
- 确认 `MAVEN_HOME` 和 `PATH` 环境变量已配置
- **重新打开** 命令提示符（修改环境变量后需要重新打开窗口才生效）

---

## 生产环境部署建议

1. **数据库**：使用独立的 MySQL 实例，不要用 root 账号，创建专用数据库用户
2. **JWT 密钥**：使用 64 位以上的随机字符串，定期更换
3. **HTTPS**：生产环境必须使用 HTTPS（微信小程序强制要求）
4. **日志**：将 `logging.level.com.wool` 从 `debug` 改为 `info`，减少日志量
5. **微信域名**：在微信公众平台 → 开发管理 → 服务器域名 中配置你的正式域名
6. **端口**：建议使用 Nginx 反向代理，不直接暴露 8080 端口
7. **备份**：定期备份 MySQL 数据库

---

## 附录：获取微信 AppID

如果你还没有微信小程序，按以下步骤注册并获取 AppID：

1. 访问 https://mp.weixin.qq.com/
2. 点击右上角「立即注册」
3. 选择「小程序」
4. 按照页面提示完成注册（需要一个未注册过公众平台的邮箱）
5. 注册完成后登录，在左侧菜单找到「开发」→「开发管理」→「开发设置」
6. 页面上方可以看到 **AppID（小程序ID）**
7. 向下滚动找到「开发者密钥」部分，点击生成 **AppSecret**

将获取到的 AppID 和 AppSecret 填入 `application.yml` 中：

```yaml
wechat:
  appid: 你的AppID
  secret: 你的AppSecret
```

---

## 附录：用 curl 测试接口示例

> 以下命令可以直接在命令行中执行，用于快速测试接口。

```bash
# 1. 获取信息列表（公开接口）
curl http://localhost:8080/api/wool/list

# 2. 登录（模拟，需要真实的微信 code）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"code":"test_code","nickname":"测试用户"}'

# 3. 发布信息（需要把 TOKEN 替换为登录接口返回的 token）
curl -X POST http://localhost:8080/api/wool/publish \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"title":"测试羊毛","content":"这是一个测试","category":"测试"}'

# 4. 查询积分余额
curl http://localhost:8080/api/points/balance \
  -H "Authorization: Bearer TOKEN"

# 5. 查询可兑换商品
curl http://localhost:8080/api/exchange/goods
```

---

## 附录：数据库表结构速览

### t_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| openid | VARCHAR(64) | 微信 openid，唯一 |
| nickname | VARCHAR(64) | 昵称 |
| avatar_url | VARCHAR(512) | 头像 URL |
| role | TINYINT | 0=普通用户，1=管理员 |
| points | INT | 积分余额 |
| status | TINYINT | 0=禁用，1=正常 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### t_wool_info（羊毛信息表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 发布者 ID |
| title | VARCHAR(128) | 标题 |
| content | TEXT | 详细内容 |
| category | VARCHAR(32) | 分类 |
| source_url | VARCHAR(512) | 来源链接 |
| status | TINYINT | 0=待审核，1=已上线，2=已驳回，3=已下线 |
| reject_reason | VARCHAR(256) | 驳回理由 |
| view_count | INT | 浏览次数 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### t_points_log（积分变动记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID |
| change_type | TINYINT | 1=发布奖励，2=兑换扣减，3=管理员调整 |
| change_value | INT | 变动值（正=增加，负=减少） |
| before_points | INT | 变动前积分 |
| after_points | INT | 变动后积分 |
| remark | VARCHAR(128) | 备注 |
| biz_id | BIGINT | 关联业务 ID |
| created_at | DATETIME | 创建时间 |

### t_exchange_goods（兑换商品表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(128) | 商品名称 |
| description | VARCHAR(512) | 商品描述 |
| image_url | VARCHAR(512) | 商品图片 |
| points_cost | INT | 兑换所需积分 |
| stock | INT | 库存数量 |
| status | TINYINT | 0=下架，1=上架 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### t_exchange_record（兑换记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID |
| goods_id | BIGINT | 商品 ID |
| goods_name | VARCHAR(128) | 商品名称（冗余存储） |
| points_cost | INT | 消耗积分 |
| status | TINYINT | 0=已取消，1=已兑换 |
| created_at | DATETIME | 创建时间 |

---

## 附录：积分并发安全说明

积分扣减采用 **条件更新** 策略保证并发安全：

```sql
-- 扣减积分时，同时检查余额是否充足
UPDATE t_user SET points = points - #{deduct}
WHERE id = #{id} AND points >= #{deduct}
```

如果 `affected rows = 0`，说明余额不足或并发冲突，抛出异常回滚事务。

库存扣减同理：

```sql
UPDATE t_exchange_goods SET stock = stock - #{quantity}
WHERE id = #{id} AND stock >= #{quantity}
```

整个兑换操作在一个数据库事务中执行，任何一步失败都会整体回滚，保证数据一致性。
