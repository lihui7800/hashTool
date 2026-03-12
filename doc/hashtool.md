# SHA256Tool 使用说明文档

## 一、项目概述

SHA256Tool 是一款基于 JavaFX 开发的轻量级桌面应用程序，专为计算本地文件的 SHA-256 哈希值而设计。通过简洁的图形界面，用户只需选择文件并点击按钮，即可快速获取文件的 64 位小写十六进制哈希字符串，并将其一键复制到系统剪贴板。

| 属性 | 内容 |
|------|------|
| 项目名称 | SHA256Tool |
| 当前版本 | 1.0.0 |
| 开发语言 | Java 17 |
| UI 框架 | JavaFX 17.0.13 |
| 构建工具 | Maven |
| 哈希算法 | SHA-256 |
| 输出格式 | 64 位小写十六进制字符串 |
| 支持平台 | Windows 10+、Linux、macOS |

---

## 二、功能说明

### 2.1 选择文件

点击【选择文件】按钮，弹出系统原生文件选择对话框（`FileChooser`），支持选取任意类型、任意大小的本地文件。选择完成后，文件的绝对路径将显示在路径输入框中，【计算 SHA256】按钮同时变为可用状态。

> 若在已有计算结果时重新选择文件，界面将自动清除上次的计算结果，等待新一轮计算。

### 2.2 计算 SHA256

点击【计算 SHA256】按钮，程序将在后台线程中对所选文件进行 SHA-256 哈希计算。计算期间界面会显示半透明加载遮罩层和进度指示器，同时禁用选择文件和计算按钮，防止重复操作。

计算完成后：
- 遮罩层自动消失
- 界面动态展开显示"计算结果："标签和结果文本框
- 文本框中显示 64 个十六进制字符的 SHA-256 哈希值
- 两个按钮恢复可用状态

若计算过程中发生异常（如文件读取失败），结果文本框将显示相应错误信息。

### 2.3 复制结果

计算完成后，点击【复制结果】按钮，结果文本框中的哈希字符串将被写入系统剪贴板，可直接粘贴到其他应用程序中使用。

---

## 三、界面说明

应用窗口宽度固定为 **480px**，高度随内容自动适应，不可调整大小。

**初始状态（未选择文件）：**

```
┌────────────────────────────────────────────────┐
│  SHA256Tool v1.0.0                   [─][□][×]  │
├────────────────────────────────────────────────┤
│                                                │
│  ┌──────────────────────────────┐ [选择文件]   │
│  │  请选择文件...                │             │
│  └──────────────────────────────┘             │
│                                [计算 SHA256]   │
│                                                │
└────────────────────────────────────────────────┘
```

**计算完成后（动态展开）：**

```
┌────────────────────────────────────────────────┐
│  SHA256Tool v1.0.0                   [─][□][×]  │
├────────────────────────────────────────────────┤
│                                                │
│  ┌──────────────────────────────┐ [选择文件]   │
│  │  D:\files\example.pdf        │             │
│  └──────────────────────────────┘             │
│  计算结果：            [计算 SHA256]            │
│  ┌──────────────────────────────────────────┐  │
│  │ e3b0c44298fc1c149afbf4c8996fb92427ae41e4 │  │
│  │ 649b934ca495991b7852b855                 │  │
│  └──────────────────────────────────────────┘  │
│                                  [复制结果]    │
│                                                │
└────────────────────────────────────────────────┘
```

### 界面组件

| 组件 | 类型 | 说明 |
|------|------|------|
| 文件路径框 | `TextField`（只读） | 显示所选文件的绝对路径，不可手动编辑，随 HBox 自动填充剩余宽度 |
| 选择文件按钮 | `Button` | 打开系统 FileChooser 对话框，计算进行中禁用 |
| 计算 SHA256 按钮 | `Button` | 未选择文件时禁用，计算进行中禁用并显示加载遮罩 |
| 加载遮罩 | `StackPane` | 计算期间显示半透明黑色遮罩与 `ProgressIndicator` 旋转动画 |
| 计算结果标签 | `Label` | 计算完成前隐藏（`setManaged(false)`，不占布局空间） |
| 结果文本框 | `TextArea`（只读） | 显示 SHA-256 哈希字符串，自动折行，计算完成前隐藏 |
| 复制结果按钮 | `Button` | 将结果文本框内容写入系统剪贴板，计算完成前隐藏 |

---

## 四、使用步骤

```
1. 启动 SHA256Tool
2. 点击【选择文件】，在弹出的系统对话框中选取目标文件
3. 文件路径显示在路径框中，点击【计算 SHA256】
4. 等待进度指示器消失，结果框中显示 64 位哈希字符串
5. 点击【复制结果】，哈希值写入剪贴板，可直接粘贴使用
```

---

## 五、技术实现

### 5.1 SHA-256 哈希计算

哈希计算采用流式读取方式，文件内容不会被整体加载到内存，支持任意大小的文件。

```java
// Sha256Util.java
public static String sha256Hex(File file) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (InputStream in = Files.newInputStream(file.toPath());
         DigestInputStream dis = new DigestInputStream(in, digest)) {
        byte[] buffer = new byte[8192];
        while (dis.read(buffer) != -1) { /* 逐块读取，更新摘要 */ }
    }
    byte[] hash = digest.digest();
    StringBuilder sb = new StringBuilder(64);
    for (byte b : hash) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
```

**关键特性：**
- 使用 JDK 内置 `MessageDigest`，算法为 `SHA-256`
- 通过 `DigestInputStream` 包装原始输入流，读取数据时自动计算摘要
- 每次读取 8192 字节（8 KB）分块处理，内存占用恒定
- 输出为 64 个字符的小写十六进制字符串

### 5.2 异步计算与 UI 线程

为避免大文件计算时阻塞 JavaFX UI 线程，计算任务通过 `javafx.concurrent.Task` 在独立线程中执行：

```java
Task<String> task = new Task<>() {
    @Override
    protected String call() throws Exception {
        return Sha256Util.sha256Hex(selectedFile[0]);
    }
};

task.setOnSucceeded(ev -> { /* 在 UI 线程更新界面 */ });
task.setOnFailed(ev -> { /* 在 UI 线程显示错误信息 */ });

new Thread(task).start();
```

`Task` 的 `setOnSucceeded` / `setOnFailed` 回调自动在 JavaFX Application Thread 上执行，确保 UI 更新线程安全。

### 5.3 动态布局

结果区域（标签、文本框、复制按钮行）使用 `setVisible(false)` + `setManaged(false)` 双重隐藏：
- `setVisible(false)`：组件不可见
- `setManaged(false)`：组件不参与布局计算，不占用任何空间

计算完成后调用 `setVisible(true)` + `setManaged(true)` 恢复，并通过 `stage.sizeToScene()` 重新适配窗口高度。

---

## 六、项目结构

```
hashTool/
├── pom.xml                          # Maven 构建配置
├── src/main/java/
│   ├── module-info.java             # Java 模块声明（requires javafx.controls）
│   └── com/enss/sha256tool/
│       ├── Sha256ToolApp.java       # JavaFX 主界面与交互逻辑
│       └── Sha256Util.java          # SHA-256 流式哈希计算工具类
└── doc/
    ├── sha256tool.md                # 软件设计文档
    └── hashtool.md                  # 本文件（使用说明文档）
```

---

## 七、构建与运行

### 7.1 前置要求

- JDK 17+（含 JavaFX 模块支持）
- Maven 3.6+

### 7.2 编译

```bash
mvn compile
```

### 7.3 直接运行

```bash
mvn javafx:run
```

### 7.4 生成自定义运行时镜像（jlink）

```bash
mvn javafx:jlink
```

执行后在 `target/sha256tool-runtime/` 目录生成包含最小化 JRE 的独立运行环境：

```
target/sha256tool-runtime/
├── bin/
│   ├── sha256tool        # Linux/macOS 启动脚本
│   └── sha256tool.bat    # Windows 启动脚本
├── conf/
└── lib/
```

> jlink 构建已启用 `stripDebug`、`noManPages`、`noHeaderFiles` 和 `compress=2` 优化，最大限度压缩运行时体积。

### 7.5 Windows 安装包（jpackage）

```bash
jpackage --type exe \
  --input target/sha256tool-runtime \
  --main-jar sha256tool.jar \
  --name SHA256Tool \
  --app-version 1.0.0 \
  --dest staging
```

> 生成的 `.exe` 为安装程序，默认安装路径为 `C:\Program Files\SHA256Tool\`。

---

## 八、依赖声明

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.13</version>
</dependency>
```

仅依赖 `javafx-controls` 模块（通过 `javafx.graphics` 传递依赖获得 `Application`、`Stage`、`Scene` 等基础类），无任何第三方库。
