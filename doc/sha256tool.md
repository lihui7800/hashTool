# SHA256Tool 软件设计文档

## 一、软件说明

SHA256Tool 是一个基于 JavaFX 开发的桌面工具，用于计算指定文件的 SHA256 哈希值，并将结果复制到系统剪贴板。本工具主要服务于数资系统中对文件完整性验证、数据资产确权、区块链存证等业务场景，提供一致、可验证的文件指纹计算能力。

| 属性 | 说明 |
|------|------|
| 工具名称 | SHA256Tool |
| 当前版本 | 1.0.0 |
| UI 框架 | JavaFX 17 |
| 支持平台 | Windows 10+、Linux、macOS |
| 哈希算法 | SHA-256（Secure Hash Algorithm 256-bit） |
| 输出格式 | 64 位小写十六进制字符串 |

---

## 二、功能说明

### 2.1 核心功能

- **选择文件**：通过系统文件选择对话框（FileChooser）选取任意本地文件。
- **计算 SHA256**：点击按钮对所选文件进行 SHA256 哈希运算，结果展示在界面文本框中。
- **复制结果**：点击复制按钮，将计算结果写入系统剪贴板，方便粘贴使用。

---

## 三、技术原理

### 3.1 SHA-256 算法简介

SHA-256 是由美国国家安全局（NSA）设计、NIST 发布的密码散列函数，属于 SHA-2 系列。

- 输入：任意长度的字节序列
- 输出：256 位（32 字节）定长摘要，通常以 64 个十六进制字符表示
- 抗碰撞性强：在当前计算能力下，不同文件产生相同哈希值（碰撞）的概率可忽略不计
- 雪崩效应：文件内容任意微小变化，哈希值将发生剧烈变化

### 3.2 文件哈希计算流程

```
文件 → 读取字节流 → SHA-256 摘要计算 → 输出 64 位十六进制字符串
```

1. 以二进制流方式读取目标文件全部字节。
2. 将字节数组输入 SHA-256 摘要算法。
3. 将摘要结果（32 字节）转换为小写十六进制字符串（64 字符）。

---

## 四、界面设计

### 4.1 主界面布局

使用 JavaFX `VBox` 作为根容器，整体窗口固定宽度 480px。第一行用 `HBox` 将文件路径框与选择文件按钮水平排列，计算按钮靠右对齐；计算结果框与复制结果按钮初始隐藏，计算完成后动态显示。

**初始状态（未选择文件）：**

```
┌──────────────────────────────────────────────┐
│  SHA256Tool v1.0.0                  [─][□][×] │
├──────────────────────────────────────────────┤
│                                              │
│  ┌────────────────────────────┐ [选择文件]   │
│  │  请选择文件...              │             │
│  └────────────────────────────┘             │
│                                              │
│                          [ 计算 SHA256 ]     │
│                                              │
└──────────────────────────────────────────────┘
```

**计算完成后（动态展开）：**

```
┌──────────────────────────────────────────────┐
│  SHA256Tool v1.0.0                  [─][□][×] │
├──────────────────────────────────────────────┤
│                                              │
│  ┌────────────────────────────┐ [选择文件]   │
│  │  /path/to/yourfile.pdf     │             │
│  └────────────────────────────┘             │
│                                              │
│                          [ 计算 SHA256 ]     │
│                                              │
│  计算结果：                                   │
│  ┌──────────────────────────────────────┐    │
│  │ e3b0c44298fc1c149afbf4c8996fb924...  │    │
│  │ 27ae41e4649b934ca495991b7852b855     │    │
│  └──────────────────────────────────────┘    │
│                          [ 复制结果 ]        │
│                                              │
└──────────────────────────────────────────────┘
```

### 4.2 组件说明

| 组件 | 类型 | 说明 |
|------|------|------|
| 文件路径框 | `TextField`（只读） | 与选择文件按钮同行（`HBox`），`HGrow.ALWAYS` 自动填充剩余宽度，不可手动编辑 |
| 选择文件按钮 | `Button` | 与路径框同行靠右，打开 `FileChooser` 系统对话框 |
| 计算 SHA256 按钮 | `Button` | 独占一行并靠右对齐，未选择文件时禁用 |
| 计算结果框 | `TextArea`（只读） | 初始隐藏（`setVisible(false)` + `setManaged(false)`），计算完成后动态显示 |
| 复制结果按钮 | `Button` | 初始隐藏，计算完成后动态显示并靠右对齐，点击将结果写入系统剪贴板 |

---

## 五、使用流程

```
1. 启动 SHA256Tool
2. 点击 [选择文件]，在系统对话框中选取目标文件
3. 文件路径显示在路径框后，点击 [计算 SHA256]
4. 计算结果展示在结果框中
5. 点击 [复制结果]，哈希值写入系统剪贴板
```

### 5.1 在数资系统中的使用场景

| 场景 | 说明 |
|------|------|
| 资产文件上传 | 文件上传时自动计算 SHA256，存入数据库，后续用于完整性校验 |
| 数据资产确权 | 对资产所有子文件计算 SHA256，构建 Merkle 树，将根哈希写入区块链 |
| TSA 时间戳确权 | 将数据资产完整 JSON 信息的 SHA256 作为附件指纹，写入 TSA 证书 |
| AES 加密验证 | 文件加密前后分别记录 SHA256，解密后比对，确保文件未被篡改 |

---

## 六、核心代码实现

### 6.1 JavaFX 主界面

```java
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Sha256ToolApp extends Application {

    @Override
    public void start(Stage stage) {
        // ── 第一行：文件路径框 + 选择文件按钮 ──
        TextField pathField = new TextField();
        pathField.setPromptText("请选择文件...");
        pathField.setEditable(false);
        HBox.setHgrow(pathField, Priority.ALWAYS); // 自动填充剩余宽度

        Button chooseBtn = new Button("选择文件");

        HBox fileRow = new HBox(8, pathField, chooseBtn);
        fileRow.setAlignment(Pos.CENTER_LEFT);

        // ── 第二行：计算按钮（靠右）──
        Button calcBtn = new Button("计算 SHA256");
        calcBtn.setDisable(true);

        HBox calcRow = new HBox(calcBtn);
        calcRow.setAlignment(Pos.CENTER_RIGHT);

        // ── 第三行：结果标签 + 结果框（初始隐藏）──
        Label resultLabel = new Label("计算结果：");
        resultLabel.setVisible(false);
        resultLabel.setManaged(false);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(3);
        resultArea.setWrapText(true);
        resultArea.setVisible(false);
        resultArea.setManaged(false);

        // ── 第四行：复制按钮（靠右，初始隐藏）──
        Button copyBtn = new Button("复制结果");
        copyBtn.setVisible(false);
        copyBtn.setManaged(false);

        HBox copyRow = new HBox(copyBtn);
        copyRow.setAlignment(Pos.CENTER_RIGHT);
        copyRow.setVisible(false);
        copyRow.setManaged(false);

        // 文件引用
        final File[] selectedFile = {null};

        // 选择文件事件
        chooseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("选择文件");
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                selectedFile[0] = file;
                pathField.setText(file.getAbsolutePath());
                calcBtn.setDisable(false);
                // 重新选择文件时隐藏上次结果
                setResultVisible(resultLabel, resultArea, copyRow, false);
                stage.sizeToScene();
            }
        });

        // 计算 SHA256 事件
        calcBtn.setOnAction(e -> {
            try {
                String hash = Sha256Util.sha256Hex(selectedFile[0]);
                resultArea.setText(hash);
                setResultVisible(resultLabel, resultArea, copyRow, true);
            } catch (Exception ex) {
                resultArea.setText("计算失败：" + ex.getMessage());
                setResultVisible(resultLabel, resultArea, copyRow, true);
            }
            stage.sizeToScene();
        });

        // 复制结果事件
        copyBtn.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(resultArea.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });

        // 根布局
        VBox root = new VBox(12, fileRow, calcRow, resultLabel, resultArea, copyRow);
        root.setPadding(new Insets(20));

        stage.setTitle("SHA256Tool v1.0.0");
        stage.setScene(new Scene(root, 480, Region.USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.show();
    }

    /** 统一控制结果区域的显示/隐藏 */
    private void setResultVisible(Label label, TextArea area, HBox copyRow, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        area.setVisible(visible);
        area.setManaged(visible);
        copyRow.setVisible(visible);
        copyRow.setManaged(visible);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### 6.2 SHA256 工具类

```java
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class Sha256Util {

    /**
     * 以流式方式计算文件 SHA256，适用于任意大小文件
     *
     * @param file 目标文件
     * @return 64 位十六进制小写字符串
     */
    public static String sha256Hex(java.io.File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file.toPath());
             DigestInputStream dis = new DigestInputStream(in, digest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) { /* 流式读取，自动更新摘要 */ }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
```

---

## 七、输出规范

- **格式**：64 位小写十六进制字符串
- **示例**：`e3b0c44298fc1c149afbf4c8996fb924 27ae41e4649b934ca495991b7852b855`（空文件哈希值）
- **长度**：固定 64 个字符，不足补 `0`

---

## 八、技术依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 运行环境，内置 `MessageDigest`、`DigestInputStream` |
| JavaFX | 17+ | 界面框架，提供 `FileChooser`、`Clipboard` 等组件 |

> JavaFX 自 JDK 11 起已从 JDK 中分离，需单独引入 `javafx-controls`、`javafx-fxml` 依赖，或使用含 JavaFX 的发行版（如 Liberica JDK）。

---

## 九、注意事项

1. **流式读取**：工具类使用 `DigestInputStream` 流式读取文件，不将文件内容一次性加载进内存，支持任意大小文件。
2. **平台一致性**：以二进制模式读取文件字节，跨平台计算结果一致，不受操作系统换行符差异影响。
3. **哈希值格式**：输出统一为 64 位小写十六进制字符串，与数资系统后端保持一致。
4. **SHA256 安全性**：SHA-256 当前无已知实用碰撞攻击，可作为可信文件指纹使用，但不具备加密保护效果。