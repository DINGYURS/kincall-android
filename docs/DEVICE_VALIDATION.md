# KinCall 真机验证记录

## F01 工程基线

验证时间：2026-08-31 15:20（Asia/Shanghai）

### 设备与构建

| 项目 | 结果 |
|---|---|
| 设备 | HUAWEI HLK-AL00 |
| Android | 10（API 29） |
| 屏幕 | 1080 × 2340，480 dpi |
| 系统字体缩放 | 1.45 |
| 应用 | `com.kincall.android` 0.1.0（versionCode 1） |
| APK SHA-256 | `70DC6A7209301C9F1E0E914BC57BCE0AD457E06BF8C35F771B3DF85A87ACAD5D` |

设备序列号、原始截图和 UI 树不纳入版本控制。

### 验收结果

1. ADB USB 授权成功，设备状态为 `device`。
2. Debug APK 通过系统安装确认后安装成功。
3. 首次冷启动成功，`MainActivity` 成为前台 resumed Activity。
4. 实机截图和 UI 树均确认显示“KinCall 基础工程已就绪”。
5. 连续 5 次强制停止后的冷启动全部成功，耗时分别为 262、255、263、269、255 ms。
6. 验证期间未发现 KinCall 的 `FATAL EXCEPTION` 或 ANR。

结论：F01 工程基线通过目标真机验收。

### 后续约束

目标设备使用 1.45 倍字体缩放，当前占位文字会换行。F02 主界面必须在该字体缩放下验证照片按钮、状态提示和错误信息不会被截断或挤出可点击区域。
