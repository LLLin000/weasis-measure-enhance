# Weasis Measure Enhance Plugin

[English](#english) | [中文](#中文)

---

## English

A plugin for [Weasis](https://github.com/nroduit/Weasis) medical imaging viewer that enhances measurement tools with additional features like circle center detection, angle calculations, and perpendicular measurements.

> **Note**: This plugin was developed with the assistance of AI (GitHub Copilot / Claude) to simplify medical image measurements. It is provided as-is for convenience and learning purposes.

### Acknowledgments

Special thanks to:

- **[Weasis](https://github.com/nroduit/Weasis)** - An excellent open-source DICOM viewer by [@nroduit](https://github.com/nroduit) and contributors. This plugin would not be possible without their outstanding work.
- The open-source community for making medical imaging software accessible to everyone.

### Features

- **Circle Center Tool**: Find the center of a circle from 3 points on the circumference
- **Continue Line Tool**: Draw a line and automatically extend it by the same length
- **Angle Calculation** (∠ button): Calculate angles between 2 selected lines
- **Perpendicular Bisector** (⊥ button): Draw perpendicular bisector of a selected line
- **Perpendicular Distance** (⊥d button): Measure perpendicular distance from a point to a selected line

### Requirements

- **Weasis**: 4.6.5 or higher
- **Java**: 24

> ⚠️ This plugin is compiled against Weasis 4.6.x APIs. Tested with Weasis 4.6.5 release + JDK 24.

### Installation

#### Method 1: Download Pre-built JAR (Recommended)

1. Go to [Releases](../../releases) page
2. Download `weasis-measure-enhance-1.0.0-SNAPSHOT.jar`
3. Follow the "Loading the Plugin" instructions below

#### Method 2: Build from Source

> ⚠️ **Prerequisites:** Building from source requires [Weasis source code](https://github.com/nroduit/Weasis) to be cloned and built first, as this plugin depends on `weasis-parent`.

```bash
# 1. First, clone and build Weasis (if not already done)
git clone https://github.com/nroduit/Weasis.git
cd Weasis
mvn install -DskipTests

# 2. Then clone and build this plugin
cd ..
git clone https://github.com/YOUR_USERNAME/weasis-measure-enhance.git
cd weasis-measure-enhance
mvn clean package -DskipTests
```

The JAR file will be created in `target/weasis-measure-enhance-1.0.0-SNAPSHOT.jar`

### Loading the Plugin

#### Option A: Using Configuration File

1. Create a JSON configuration file (e.g., `myplugin.json`):

```json
{
  "weasisPreferences": [
    {
      "code": "felix.auto.start.13",
      "value": "file:///PATH/TO/weasis-measure-enhance-1.0.0-SNAPSHOT.jar",
      "description": "Weasis Measure Enhance Plugin"
    }
  ]
}
```

> **Note:** `felix.auto.start.13` is just an example slot number. If slot 13 is already in use, change it to an unused number (e.g., 14, 15, etc.).

2. Launch Weasis with the config:

**Windows:**

```batch
"C:\Program Files\Weasis\Weasis.exe" $weasis:config pro="felix.extended.config.properties file:///C:/path/to/myplugin.json"
```

> ⚠️ **Replace these paths:**
>
> - `C:\Program Files\Weasis\Weasis.exe` → Your Weasis installation path
> - `C:/path/to/myplugin.json` → Your actual myplugin.json path (use `/` not `\`)
>
> **Example:** If your config is at `D:\MyPlugins\myplugin.json`, use:
>
> ```batch
> "C:\Program Files\Weasis\Weasis.exe" $weasis:config pro="felix.extended.config.properties file:///D:/MyPlugins/myplugin.json"
> ```

**Linux/Mac:**

```bash
weasis $weasis:config pro="felix.extended.config.properties file:///path/to/myplugin.json"
```

> ⚠️ **Replace** `/path/to/myplugin.json` with your actual config file path.

### Usage

After loading the plugin, new measurement tools will appear in the measurement toolbar:

**Drawing Tools (in toolbar dropdown):**

1. **Circle Center**: Click 3 points on a circle's edge to find and mark its center
2. **Continue Line**: Draw a line and automatically extend it by the same length

**Toolbar Buttons:**
3. **∠ (Angle Calculation)**: Select 2 lines (Ctrl+Click), then click the button to calculate the angle between them
4. **⊥ (Perpendicular Bisector)**: Select a line, then click the button to draw its perpendicular bisector
5. **⊥d (Perpendicular Distance)**: Select a line, then after clicking the button, click another point to measure the perpendicular distance

### Development

#### Project Structure

```
weasis-measure-enhance/
├── src/main/java/com/mycompany/weasis/measure/enhance/
│   ├── MeasureEnhanceFactory.java       # OSGi bundle activator & toolbar setup
│   ├── MeasureEnhanceTool.java          # Tool name constants
│   ├── CircleCenterToolGraphic.java     # Circle center finder (3 points)
│   ├── ContinueLineToolGraphic.java     # Line with auto extension
│   ├── AngleCalculationAction.java      # Calculate angle between 2 lines
│   ├── PerpendicularBisectorAction.java # Draw perpendicular bisector
│   ├── PerpendicularDistanceAction.java # Measure perpendicular distance
│   └── SnappingUtil.java                # Point snapping utilities (reserved)
├── pom.xml
└── README.md
```

#### Building

```bash
mvn clean package
```

### License

This project is licensed under the EPL-2.0 OR Apache-2.0 License - same as Weasis.

### Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

---

## 中文

这是一个 [Weasis](https://github.com/nroduit/Weasis) 医学影像查看器的插件，增强了测量工具的功能，包括圆心定位、角度计算和垂直测量等功能。

> **说明**: 本插件在 AI（GitHub Copilot / Claude）辅助下开发，旨在简化医学影像测量操作。仅供学习和便捷使用。

### 致谢

特别感谢：

- **[Weasis](https://github.com/nroduit/Weasis)** - 由 [@nroduit](https://github.com/nroduit) 及其贡献者开发的优秀开源 DICOM 查看器。没有他们的杰出工作，就不会有这个插件。
- 开源社区让医学影像软件对每个人都触手可及。

### 功能特性

- **圆心工具**: 通过圆周上的3点找到圆心
- **延长线工具**: 画一条线并自动延长相同长度
- **角度计算** (∠ 按钮): 计算两条选中线段之间的夹角
- **垂直平分线** (⊥ 按钮): 绘制选中线段的垂直平分线
- **垂直距离** (⊥d 按钮): 测量一点到选中线段的垂直距离

### 系统要求

- **Weasis**: 4.6.5 或更高版本
- **Java**: 24

> ⚠️ 本插件基于 Weasis 4.6.x API 编译。已在 Weasis 4.6.5 发行版 + JDK 24 上测试通过。

### 安装方法

#### 方法一：下载预编译 JAR（推荐）

1. 访问 [Releases](../../releases) 页面
2. 下载 `weasis-measure-enhance-1.0.0-SNAPSHOT.jar`
3. 按照下方“加载插件”的说明操作

#### 方法二：从源码编译

> ⚠️ **前提条件：** 从源码编译需要先克隆并编译 [Weasis 源码](https://github.com/nroduit/Weasis)，因为本插件依赖 `weasis-parent`。

```bash
# 1. 首先克隆并编译 Weasis（如果还没有的话）
git clone https://github.com/nroduit/Weasis.git
cd Weasis
mvn install -DskipTests

# 2. 然后克隆并编译本插件
cd ..
git clone https://github.com/YOUR_USERNAME/weasis-measure-enhance.git
cd weasis-measure-enhance
mvn clean package -DskipTests
```

JAR 文件将生成在 `target/weasis-measure-enhance-1.0.0-SNAPSHOT.jar`

### 加载插件

#### 方式 A：使用配置文件

1. 创建 JSON 配置文件（例如 `myplugin.json`）：

```json
{
  "weasisPreferences": [
    {
      "code": "felix.auto.start.13",
      "value": "file:///你的路径/weasis-measure-enhance-1.0.0-SNAPSHOT.jar",
      "description": "Weasis Measure Enhance Plugin"
    }
  ]
}
```

> **说明：** `felix.auto.start.13` 只是示例编号。如果 13 已被占用，请改成未使用的编号（如 14、15 等）。

2. 使用配置启动 Weasis：

**Windows:**

```batch
"C:\Program Files\Weasis\Weasis.exe" $weasis:config pro="felix.extended.config.properties file:///C:/path/to/myplugin.json"
```

> ⚠️ **需要修改的路径：**
>
> - `C:\Program Files\Weasis\Weasis.exe` → 改成你的 Weasis 安装路径
> - `C:/path/to/myplugin.json` → 改成你的 myplugin.json 实际路径（注意用 `/` 不是 `\`）
>
> **举例：** 如果你的配置文件在 `D:\MyPlugins\myplugin.json`，则命令为：
>
> ```batch
> "C:\Program Files\Weasis\Weasis.exe" $weasis:config pro="felix.extended.config.properties file:///D:/MyPlugins/myplugin.json"
> ```

**Linux/Mac:**

```bash
weasis $weasis:config pro="felix.extended.config.properties file:///path/to/myplugin.json"
```

> ⚠️ **需要修改：** 把 `/path/to/myplugin.json` 改成你的配置文件实际路径。

### 使用方法

加载插件后，测量工具栏中会出现新的测量工具：

**绘图工具（在工具栏下拉菜单中）：**

1. **圆心**: 点击圆周上3点找到圆心
2. **延长线**: 画一条线并自动延长相同长度

**工具栏按钮：**

3. **∠ (角度计算)**: 选中2条线段（Ctrl+点击多选），然后点击按钮计算夹角
4. **⊥ (垂直平分线)**: 选中一条线段，然后点击按钮绘制垂直平分线
5. **⊥d (垂直距离)**: 选中一条线段，然后点击按钮后点击一点来测量垂直距离

### 许可证

本项目采用 EPL-2.0 OR Apache-2.0 许可证 - 与 Weasis 相同。

### 贡献

欢迎提交 Pull Request。如有重大更改，请先开 Issue 讨论。
