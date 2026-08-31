# KinCall

KinCall 是一个面向不会操作智能手机的老人设计的一键视频通话 Android 应用。第一阶段仅支持在固定设备上，由用户主动点击家人照片后，通过受限、确定性的无障碍状态机发起固定联系人的微信视频通话。

## 当前状态

项目处于工程基线阶段。微信自动化能力必须在目标设备上通过 UI 树采集、真机操作和输出状态验证后，才能视为完成。

## 本地验证

创建不纳入版本控制的 `local.properties`：

```properties
sdk.dir=W\:\\DevTools\\Android\\Sdk
```

然后执行：

```powershell
.\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug
```

详细阶段、验收门禁和提交策略见 [开发计划](docs/DEVELOPMENT_PLAN.md)。

## 安全边界

- 不绕过锁屏、系统授权或微信账号验证。
- 不注入、修改或逆向微信，不读取联系人关系链。
- 仅响应用户明确点击，执行单联系人、低频、确定性的操作。
- 任一步状态不明确时安全停止，不盲目继续点击。

## License

Apache License 2.0。
