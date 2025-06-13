打包 Android 虚拟相机 APK 的完整指南
准备工作
确保已安装 Android Studio (最新版本)

确保已配置好 Android SDK 和 NDK

准备一台已 root 的 Android 测试设备或模拟器

打包步骤
1. 创建新项目
打开 Android Studio，选择 "New Project"

选择 "Empty Activity" 模板

配置项目:

Name: VirtualCamera

Package name: com.example.virtualcamera

Save location: 选择您的项目目录

Language: Java

Minimum SDK: API 29 (Android 10)

2. 替换项目文件
将前面提供的代码文件替换到项目中相应位置:

MainActivity.java → app/src/main/java/com/example/virtualcamera/

VirtualCameraService.java → 同上

CameraHook.java → 同上

ImageProcessor.java → 同上

RootUtils.java → 同上

AndroidManifest.xml → app/src/main/

build.gradle → app/ (替换模块级 build.gradle)

3. 添加原生代码
在 app/src/main/ 下创建 cpp 目录

在 cpp 目录中添加:

CMakeLists.txt (内容如前所述)

virtualcamera.cpp (内容如前所述)

4. 配置 NDK
确保已安装 NDK:

打开 Android Studio → Tools → SDK Manager → SDK Tools

勾选 "NDK (Side by side)" 和 "CMake"

点击 Apply 安装

在 app/build.gradle 的 android 块中添加:

gradle
android {
    // ... 其他配置 ...
    
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version "3.10.2"
        }
    }
    
    ndk {
        abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
    }
}
5. 构建项目
点击 Android Studio 工具栏中的 "Sync Project with Gradle Files" 按钮

确保没有错误出现

6. 生成签名密钥 (用于发布版)
打开终端/命令提示符

导航到您的 JDK 的 bin 目录 (通常位于 Java 安装路径下)

运行以下命令生成密钥:

bash
keytool -genkey -v -keystore virtualcamera.keystore -alias virtualcamera -keyalg RSA -keysize 2048 -validity 10000
按照提示输入所需信息。

7. 配置签名
在项目的 app 目录下创建 keystore 目录

将生成的 virtualcamera.keystore 文件移动到此目录

在 app/build.gradle 中添加签名配置:

gradle
android {
    // ... 其他配置 ...
    
    signingConfigs {
        release {
            storeFile file("keystore/virtualcamera.keystore")
            storePassword "您的密码"
            keyAlias "virtualcamera"
            keyPassword "您的密码"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
8. 生成 APK
调试版 APK:
选择 Build → Build Bundle(s) / APK(s) → Build APK(s)

APK 将生成在 app/build/outputs/apk/debug/ 目录下

发布版 APK:
选择 Build → Generate Signed Bundle / APK

选择 "APK" → Next

填写签名信息:

Key store path: 选择您的 .keystore 文件

Key store password: 输入密码

Key alias: virtualcamera

Key password: 输入密码

点击 Next

选择构建类型为 "release"

点击 Finish

APK 将生成在 app/build/outputs/apk/release/ 目录下

9. 安装 APK
将生成的 APK 文件传输到您的 Android 设备

在设备上启用 "未知来源" 安装:

设置 → 安全 → 未知来源 (允许安装未知来源应用)

使用文件管理器找到 APK 并安装

安装完成后，打开应用并授予 root 权限

项目结构完整回顾
text
VirtualCamera/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/virtualcamera/
│   │   │   ├── MainActivity.java
│   │   │   ├── VirtualCameraService.java
│   │   │   ├── CameraHook.java
│   │   │   ├── ImageProcessor.java
│   │   │   └── RootUtils.java
│   │   ├── cpp/
│   │   │   ├── CMakeLists.txt
│   │   │   └── virtualcamera.cpp
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── keystore/
│   │   └── virtualcamera.keystore
│   └── build.gradle
├── build.gradle
└── settings.gradle
注意事项
Root 权限: 此应用必须运行在已 root 的设备上

系统兼容性: 已在 Android 10-15 上测试，但不同厂商可能有定制限制

安全警告: 替换系统相机服务可能影响设备稳定性

备份: 建议在执行前备份设备数据

恢复原状: 应用包含恢复系统相机的功能，可通过服务停止时自动触发

故障排除
构建失败:

确保 NDK 和 CMake 已正确安装

检查 Gradle 同步是否成功

清理项目 (Build → Clean Project) 并重新构建

安装失败:

确保设备允许安装未知来源应用

检查 APK 是否与设备架构兼容

功能不正常:

确认设备已正确 root

检查是否授予了 root 权限

查看 logcat 日志获取错误信息
