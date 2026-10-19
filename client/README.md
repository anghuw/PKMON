# PKMon 客户端（Flutter / Android / Web）

校园 LBS 收集游戏 PKMon 的客户端。同一套代码可构建安卓 App 和网页版。

> 仓库根目录的 [`../README.md`](../README.md) 有完整的构建、启动和验收步骤；
> 本文只记录客户端自身的结构与注意事项。

---

## 一、当前完成度

### 已跑通的完整闭环

```text
启动 App（自动建会话、连后端）
   ↓
地图页：校园南门，进度 0/3，「模拟走到图书馆」
   ↓ 点按钮
精灵出现（立绘 +「点击遇见」金标签），位置变「图书馆入口」
   ↓ 点精灵（或点底部「遇见攻城狮」）
答题页：题干 + 三个选项（题目由后端下发）
   ↓ 选 A
服务端判定 → 成功页「成为伙伴了！」
   ↓ 返回地图
地图变已捕获态，图鉴 1/3，背包 ×1
   ↓
个人页「重新开始演示」→ 回到初始态
```

答错走另一条分支：砖红图标 +「下次一定会更熟悉」，回地图可重新出题。

### 功能清单

| 功能 | 状态 |
|---|---|
| 底部导航四页切换 | ✅ |
| 地图三态（未探索 / 精灵出现 / 已捕获） | ✅ |
| 模拟定位（走到图书馆） | ✅ 纯字符串模拟 |
| 答题抓捕（服务端判题） | ✅ |
| 图鉴（已解锁 / 未解锁） | ✅ |
| 背包（空态 / 已捕获 ×N） | ✅ |
| 精灵详情弹窗 | ✅ |
| 任务模式弹窗 | ✅ 仅界面原型 |
| 错误横幅 + 重试 | ✅ |
| 重新开始演示 | ✅ |

### 尚未实现

- 地图是绘制的示意图，未接高德 SDK，没有真实定位
- 「走到图书馆」只是切换地点文案，没有坐标运算
- 任务模式不采集位置、不计算驻留时长
- 没有账号系统，个人页是固定文案
- 精灵只有 1 种（攻城狮），题库只有 1 道演示题
- 数据存在后端内存里，重启即清空，没有数据库

---

## 二、运行

**必须先启动后端**，否则客户端会显示「连不上后端服务」。

```bash
# 终端 1：仓库根目录
cd server
mvn clean package
java -jar target/pkmon-server-0.1.0.jar

# 终端 2
cd client
flutter pub get
flutter run
```

安卓模拟器默认访问 `http://10.0.2.2:4180`；真机用：

```bash
flutter run --dart-define=API_BASE=http://192.168.x.x:4180
```

网页版构建与托管见根目录 README 的「快速开始」。

### 网页版的两个实现细节

- `lib/core/api.dart` 用 `kIsWeb` 判断环境：网页取 `Uri.base.origin`（同源，无需 CORS），
  安卓取 `10.0.2.2:4180`。
- `lib/widgets/phone_frame.dart` 在宽屏网页上套一个 412dp 的手机壳，安卓上完全不起作用。
  它挂在 `MaterialApp.builder` 上，所以所有页面（含答题页）都在壳里。

---

## 三、测试

```bash
flutter analyze     # 应为 No issues found!
flutter test        # 应为 All tests passed!（8 个用例）
```

测试直接渲染单个页面并断言三种状态下的文案，**不发网络请求**，所以快且不偶发失败。

手工验收清单：

- [ ] 冷启动：顶部短暂出现进度条，然后页脚显示 `Flutter 预览 · hello world`
- [ ] 初始地图：按钮「模拟走到图书馆」，进度 `0 / 3`，地图上无精灵
- [ ] 点按钮 → 精灵出现，带「点击遇见」金标签；位置变「图书馆入口」；按钮变「遇见攻城狮」
- [ ] 点精灵本身 → 进答题页，中文正常不乱码
- [ ] 选 **A** → 「成为伙伴了！」→ 返回地图
- [ ] 地图变已捕获态；图鉴 `1 / 3` 第 1 格亮出立绘；背包显示 `× 1`
- [ ] 点图鉴第 2 格 → SnackBar「这是后续精灵的设计占位」
- [ ] 点背包卡片 → 精灵详情弹窗
- [ ] 个人页 →「重新开始演示」→ 回到初始态
- [ ] 点地图右上计时器 → 任务模式弹窗；开始后地图左下出现「模拟自习进行中」
- [ ] 点图鉴第 1 格（未捕获时）→ 详情弹窗显示「还未收集」

断网容错：关掉后端 → 点任意按钮 → 顶部出现粉红横幅和「重试」；重启后端点「重试」恢复。

---

## 四、代码结构

```text
lib/
├── main.dart                    # 入口
├── core/
│   ├── tokens.dart              # 设计变量：色值、字号、间距、圆角
│   ├── theme.dart               # 全局 ThemeData
│   ├── api.dart                 # 后端接口封装（会话头、统一响应解析）
│   └── location_provider.dart   # 定位抽象 + 模拟实现
├── models/
│   └── sprite.dart              # 图鉴格子定义 + 资源路径
├── features/
│   ├── home_shell.dart          # 全局状态机 + 底部导航
│   ├── map_page.dart            # 地图主界面（三态）
│   ├── dex_page.dart            # 图鉴
│   ├── bag_page.dart            # 我的精灵（背包）
│   ├── profile_page.dart        # 我的
│   └── capture_page.dart        # 答题抓捕页
└── widgets/                     # 可复用组件
    ├── app_buttons.dart         # 主按钮 / 描边次按钮
    ├── section_header.dart      # 页面头
    ├── pill_chip.dart           # 白底胶囊标签
    ├── app_tag.dart             # 「点击遇见」金色小标签
    ├── error_banner.dart        # 全局错误横幅
    ├── sprite_card.dart         # 图鉴三列卡片
    ├── bag_row.dart             # 背包列表行
    ├── sprite_detail_sheet.dart # 精灵详情弹窗
    └── task_sheet.dart          # 任务模式弹窗
```

资源：`assets/sprites/siege_lion.png` 是攻城狮立绘。新增精灵时把图片放进
`assets/sprites/`，`pubspec.yaml` 已按目录声明，会自动包含。

### 三个关键约定

1. **视觉数值只在 `core/tokens.dart` 里定义**，页面不写死色值和尺寸。
2. **状态只在 `home_shell.dart` 里**，四个页面都是无状态组件（接收状态 + 回调）。
3. **所有网络操作走 `_action()` 包装**：统一开关加载态、把异常转成错误横幅。

---

## 五、安卓特有的坑（改动时别删）

| 位置 | 配置 | 为什么 |
|---|---|---|
| `android/app/src/main/AndroidManifest.xml` | `INTERNET` 权限 | 无此权限所有网络请求直接失败 |
| `android/app/src/debug/AndroidManifest.xml` | `usesCleartextTraffic="true"` | Android 9+ 默认禁止明文 HTTP，而演示后端是 `http://10.0.2.2:4180`；不加会被静默拦截 |

`core/api.dart` 解析响应必须用 `utf8.decode(response.bodyBytes)`，不能用
`response.body`，否则中文题干和选项会乱码。

`android/local.properties` 是本机 SDK 路径，由 Flutter 自动生成，已在
`.gitignore` 中排除，**不要提交**。
