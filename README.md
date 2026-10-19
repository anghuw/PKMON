# PKMon（攻城狮版）

北京大学软件工程课程实践项目。PKMon 是一个校园 LBS 收集游戏：玩家在校园地图上移动，触发精灵，答对校园知识题后将其收入图鉴。

本仓库收录**课堂展示用的「攻城狮版」完整源码**：

| 目录 | 内容 | 技术栈 |
|---|---|---|
| `client/` | 安卓客户端 + 网页客户端 | Flutter 3.47.6 / Dart 3.13.5 |
| `server/` | 演示后端、判题、网页静态资源托管 | Java 17+ / Spring Boot 3.5.7 / Maven |
| `开发文档/`、`参考文档/` | 课程文档与参考资料（原有内容，未改动） | — |

> 仓库里的源码是课堂展示时实际运行的版本，**不是**旧压缩包 `PKMON源码.zip` 里的旧版。
> 演示题目、选项和正确答案都在后端 `server/src/main/java/edu/pkmon/demo/DemoService.java`。

---

## 一、环境要求

| 工具 | 要求 | 说明 |
|---|---|---|
| JDK | 17 或更高 | 需要 `javac`，只有 JRE 不能构建 |
| Maven | 3.9+ | 构建后端 |
| Flutter | 3.47.6（Dart 3.13.5） | 构建客户端；其他 3.x 版本一般也可 |
| Android SDK | 任意近期版本 | 只有构建安卓 APK 时需要 |
| 浏览器 | Chrome / Edge 等 | 网页版演示不需要安卓环境 |

检查版本：

```bash
java -version
mvn -version
flutter --version
```

---

## 二、快速开始（推荐：网页版）

网页版和后端由**同一个 Spring Boot 服务**托管，演示时对方只需要一个浏览器。

### 1. 构建前端网页

```bash
cd client
flutter pub get
flutter build web --release
```

产物在 `client/build/web/`。

### 2. 把网页产物放进后端

```bash
# 从仓库根目录执行（Git Bash / macOS / Linux）
find server/src/main/resources/static -mindepth 1 ! -name .gitkeep -delete
cp -r client/build/web/. server/src/main/resources/static/
```

```powershell
# PowerShell 等价写法
Get-ChildItem server/src/main/resources/static -Exclude .gitkeep |
  Remove-Item -Recurse -Force
Copy-Item client/build/web/* server/src/main/resources/static/ -Recurse -Force
```

> `server/src/main/resources/static/` 里的网页文件是**构建产物，已被 `.gitignore` 排除**，
> 不要提交进仓库。仓库里只保留一个 `.gitkeep` 占位。

### 3. 构建并启动后端

```bash
cd server
mvn clean package
java -jar target/pkmon-server-0.1.0.jar
```

看到下面这行即启动成功（约 5–15 秒）：

```
Tomcat started on port 4180
```

浏览器打开 <http://127.0.0.1:4180/> 即可开始游戏。
健康检查：<http://127.0.0.1:4180/test/hello> 返回 `hello world`。

> 换端口：`java -jar target/pkmon-server-0.1.0.jar --server.port=4181`
>
> 默认只监听 `127.0.0.1`。真机演示需要监听所有网卡：
> `java -jar target/pkmon-server-0.1.0.jar --server.address=0.0.0.0`

---

## 三、安卓客户端

### 1. 先启动后端

```bash
cd server
mvn clean package
java -jar target/pkmon-server-0.1.0.jar
```

**后端窗口在演示期间要一直开着。** 安卓模拟器通过 `10.0.2.2:4180` 访问宿主机，
所以默认配置不用改。

### 2. 启动模拟器并运行

```bash
# 查看可用模拟器
flutter emulators

# 启动（AVD 名称按你本机为准）
flutter emulators --launch PKMon_Pixel7
adb devices

cd client
flutter pub get
flutter run
```

热重载按 `r`，退出按 `q`。

### 3. 真机运行

真机不在模拟器的网络栈里，需要用电脑的局域网 IP：

```bash
# 后端监听所有网卡
java -jar target/pkmon-server-0.1.0.jar --server.address=0.0.0.0

# 客户端指定后端地址
cd client
flutter run --dart-define=API_BASE=http://192.168.x.x:4180
```

同时电脑防火墙要放行 4180 端口。

### 4. 构建 APK

```bash
cd client
flutter build apk --debug
```

产物：`client/build/app/outputs/flutter-apk/app-debug.apk`。

> release 构建目前沿用 Flutter 模板的 debug 签名，仅用于课程演示。

---

## 四、攻城狮捕获流程

### 界面操作

1. 启动后端与客户端。
2. 地图页初始状态：校园南门、进度 `0 / 3`。
3. 点「模拟走到图书馆」→ 攻城狮出现在地图上。
4. 点精灵（或「遇见攻城狮」）→ 进入答题页。
5. 题目：`执行 print("hello world") 之后，控制台会输出什么？`
6. 选择 **A：hello world** → 显示「成为伙伴了！」。
7. 返回地图 → 图鉴变为 `1 / 3`，背包显示 `× 1`。
8. 「我的」→「重新开始演示」可重置。

答错不会捕获，回到地图后可以重新遇见。

### 不开客户端，直接验证后端与判题

以下命令会完整走一遍「建会话 → 探索 → 出题 → 答对 → 状态变为已捕获」。

**PowerShell：**

```powershell
$base = "http://127.0.0.1:4180"
$s = Invoke-RestMethod -Method Post -Uri "$base/api/v1/demo/sessions" `
  -ContentType "application/json" -Body "{}"
$h = @{ "X-Demo-Session" = $s.data.sessionId }

Invoke-RestMethod -Method Post -Uri "$base/api/v1/demo/explore" `
  -Headers $h -ContentType "application/json" -Body "{}"

$q = Invoke-RestMethod -Method Post -Uri "$base/api/v1/capture/challenge" `
  -Headers $h -ContentType "application/json" -Body '{"spawnId":"library-001"}'
$q.data

$a = Invoke-RestMethod -Method Post -Uri "$base/api/v1/capture/answer" `
  -Headers $h -ContentType "application/json" `
  -Body (@{ challengeId = $q.data.challengeId; optionId = "A" } | ConvertTo-Json)
$a.data

(Invoke-RestMethod -Uri "$base/api/v1/demo/state" -Headers $h).data
```

最后的 `state` 应包含：

```text
captured : True
count    : 1
nearby   : False
```

**Git Bash / Linux / macOS：**

```bash
BASE=http://127.0.0.1:4180
SID=$(curl -s -X POST "$BASE/api/v1/demo/sessions" \
  -H "Content-Type: application/json" -d '{}' \
  | python -c "import sys,json;print(json.load(sys.stdin)['data']['sessionId'])")
H="X-Demo-Session: $SID"

curl -s -X POST "$BASE/api/v1/demo/explore" -H "$H" -H "Content-Type: application/json" -d '{}'
echo
Q=$(curl -s -X POST "$BASE/api/v1/capture/challenge" -H "$H" \
  -H "Content-Type: application/json" -d '{"spawnId":"library-001"}')
echo "$Q"
CID=$(echo "$Q" | python -c "import sys,json;print(json.load(sys.stdin)['data']['challengeId'])")

curl -s -X POST "$BASE/api/v1/capture/answer" -H "$H" \
  -H "Content-Type: application/json" \
  -d "{\"challengeId\":\"$CID\",\"optionId\":\"A\"}"
echo
curl -s "$BASE/api/v1/demo/state" -H "$H"
echo
```

---

## 五、测试与静态检查

```bash
# 后端：单元测试（判题、幂等、会话隔离等 8 个用例）
cd server
mvn test

# 前端：静态分析 + 界面冒烟测试（8 个用例，不需要后端）
cd client
flutter analyze
flutter test
```

---

## 六、目录结构

```text
PKMON/
├── README.md
├── client/                         # Flutter 客户端（Android + Web）
│   ├── lib/
│   │   ├── main.dart
│   │   ├── core/                   # 设计变量、主题、API 封装、定位抽象
│   │   ├── models/                 # 精灵与图鉴定义
│   │   ├── features/               # 地图、答题、图鉴、背包、我的
│   │   └── widgets/                # 可复用组件
│   ├── assets/sprites/             # 精灵立绘（攻城狮）
│   ├── android/                    # 安卓构建配置
│   ├── web/                        # 网页构建配置
│   ├── test/                       # 界面冒烟测试
│   └── pubspec.yaml / pubspec.lock
├── server/                         # Spring Boot 后端
│   ├── src/main/java/edu/pkmon/demo/
│   │   ├── PkmonApplication.java
│   │   ├── DemoController.java     # HTTP 接口
│   │   └── DemoService.java        # 题库、判题、会话状态
│   ├── src/test/java/              # 单元测试
│   ├── src/main/resources/
│   │   ├── application.properties  # 端口 4180，监听 127.0.0.1
│   │   └── static/                 # 网页构建产物存放处（构建时生成）
│   └── pom.xml
├── 开发文档/                        # 课程文档（原有）
└── 参考文档/                        # 课程模板与参考资料（原有）
```

---

## 七、常见问题

**「连不上后端服务」**
后端没启动，或端口不是 4180。先在浏览器打开 <http://127.0.0.1:4180/test/hello> 确认。

**安卓模拟器能打开网页版，但 App 连不上**
App 用的是 `10.0.2.2:4180`（模拟器访问宿主机的固定别名），确认后端在本机运行。
真机请用 `--dart-define=API_BASE=http://电脑局域网IP:4180`，并让后端监听 `0.0.0.0`。

**安卓 9+ 上 HTTP 请求被静默拦截**
`client/android/app/src/debug/AndroidManifest.xml` 已开启 `usesCleartextTraffic`。
该配置只在 debug 构建生效；正式发布应改用 HTTPS。

**中文题干乱码**
客户端已用 `utf8.decode(response.bodyBytes)` 解析响应，不要改回 `response.body`。

**`local.properties` 需要提交吗？**
不需要。它由 Flutter 根据本机 SDK 路径自动生成，已在 `.gitignore` 中排除。

**数据会保存吗？**
不会。演示后端的状态全部在内存里，重启服务即清空；没有数据库、没有账号系统。
