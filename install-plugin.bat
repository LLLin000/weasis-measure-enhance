@echo off
:: ============================================================================
:: Weasis Measure Enhance Plugin - One-Click Installer
:: Weasis 测量增强插件 - 一键安装脚本
:: ============================================================================
:: 
:: 功能说明 / Description:
::   此脚本会自动完成以下操作:
::   This script will automatically:
::     1. 查找插件 JAR 文件 / Find the plugin JAR file
::     2. 创建 Weasis 配置文件 / Create Weasis configuration file
::     3. 生成快捷启动脚本 / Generate launcher script
::     4. 在桌面创建快捷方式 / Create desktop shortcut
::
:: 使用方法 / Usage:
::   直接双击运行此脚本即可
::   Just double-click this script to run
::
:: 前提条件 / Prerequisites:
::   - 已安装 Weasis 4.6.5 或更高版本 / Weasis 4.6.5+ installed
::   - JAR 文件位于 target/ 目录或当前目录 / JAR file in target/ or current directory
::
:: 作者 / Author: LLLin000
:: 项目 / Project: https://github.com/LLLin000/weasis-measure-enhance
:: ============================================================================

chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ============================================
echo   Weasis Measure Enhance Plugin Installer
echo   Weasis 测量增强插件安装程序
echo ============================================
echo.

:: ----------------------------------------------------------------------------
:: 步骤 1: 获取脚本所在目录
:: Step 1: Get the directory where this script is located
:: ----------------------------------------------------------------------------
:: %~dp0 返回脚本所在的完整路径（带末尾反斜杠）
:: %~dp0 returns the full path of the script (with trailing backslash)
set "SCRIPT_DIR=%~dp0"
:: 移除末尾的反斜杠 / Remove trailing backslash
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

echo [INFO] 脚本目录 / Script directory: %SCRIPT_DIR%
echo.

:: ----------------------------------------------------------------------------
:: 步骤 2: 查找插件 JAR 文件
:: Step 2: Find the plugin JAR file
:: ----------------------------------------------------------------------------
:: 优先查找 target/ 目录（Maven 编译输出）
:: First check target/ directory (Maven build output)
set "JAR_FILE=%SCRIPT_DIR%\target\weasis-measure-enhance-fixed-1.0.0.jar"

if not exist "%JAR_FILE%" (
    :: 如果 target/ 中没有，查找当前目录（用户可能直接下载了 JAR）
    :: If not in target/, check current directory (user may have downloaded JAR directly)
    set "JAR_FILE=%SCRIPT_DIR%\weasis-measure-enhance-fixed-1.0.0.jar"
)

if not exist "%JAR_FILE%" (
    echo [ERROR] 找不到插件 JAR 文件！ / Plugin JAR file not found!
    echo.
    echo 请确保 JAR 文件在以下位置之一:
    echo Please ensure the JAR file is in one of these locations:
    echo   - %SCRIPT_DIR%\target\weasis-measure-enhance-fixed-1.0.0.jar
    echo   - %SCRIPT_DIR%\weasis-measure-enhance-fixed-1.0.0.jar
    echo.
    echo 您可以从 GitHub Releases 下载:
    echo You can download from GitHub Releases:
    echo   https://github.com/LLLin000/weasis-measure-enhance/releases
    echo.
    pause
    exit /b 1
)

echo [OK] 找到插件 / Found plugin: 
echo      %JAR_FILE%
echo.

:: ----------------------------------------------------------------------------
:: 步骤 3: 转换路径格式
:: Step 3: Convert path format
:: ----------------------------------------------------------------------------
:: Weasis 使用 file:/// URI 格式，需要把 Windows 反斜杠 \ 转换为正斜杠 /
:: Weasis uses file:/// URI format, need to convert Windows backslash \ to forward slash /
:: 例如 / Example: D:\plugins\xxx.jar -> D:/plugins/xxx.jar
set "JAR_PATH=%JAR_FILE:\=/%"

:: ----------------------------------------------------------------------------
:: 步骤 4: 创建配置文件目录
:: Step 4: Create configuration directory
:: ----------------------------------------------------------------------------
:: 配置文件存放在用户目录下的 .weasis-plugins 文件夹
:: Configuration files are stored in .weasis-plugins folder under user directory
:: 例如 / Example: C:\Users\YourName\.weasis-plugins\
set "CONFIG_DIR=%USERPROFILE%\.weasis-plugins"

if not exist "%CONFIG_DIR%" (
    mkdir "%CONFIG_DIR%"
    echo [OK] 创建配置目录 / Created config directory: %CONFIG_DIR%
) else (
    echo [OK] 配置目录已存在 / Config directory exists: %CONFIG_DIR%
)
echo.

:: ----------------------------------------------------------------------------
:: 步骤 5: 创建 Weasis 配置文件 (JSON 格式)
:: Step 5: Create Weasis configuration file (JSON format)
:: ----------------------------------------------------------------------------
:: 此配置文件告诉 Weasis 在启动时自动加载指定的插件
:: This config file tells Weasis to auto-load the specified plugin at startup
::
:: 配置说明 / Configuration explanation:
::   - felix.auto.start.13: OSGi Felix 框架的自动启动属性
::                          OSGi Felix framework auto-start property
::   - 13 是启动优先级/槽位号，可以是任意未被占用的数字
::     13 is the start level/slot number, can be any unused number
::   - value: 插件 JAR 文件的 file:/// URI 路径
::            file:/// URI path to the plugin JAR file
::
set "CONFIG_FILE=%CONFIG_DIR%\measure-enhance.json"

echo [INFO] 创建配置文件 / Creating configuration file...
echo.

:: 使用 echo 逐行写入 JSON 文件 / Write JSON file line by line using echo
echo {> "%CONFIG_FILE%"
echo   "weasisPreferences": [>> "%CONFIG_FILE%"
echo     {>> "%CONFIG_FILE%"
echo       "code": "felix.auto.start.13",>> "%CONFIG_FILE%"
echo       "value": "file:///%JAR_PATH%",>> "%CONFIG_FILE%"
echo       "description": "Weasis Measure Enhance Plugin">> "%CONFIG_FILE%"
echo     }>> "%CONFIG_FILE%"
echo   ]>> "%CONFIG_FILE%"
echo }>> "%CONFIG_FILE%"

echo [OK] 配置文件已创建 / Configuration file created:
echo      %CONFIG_FILE%
echo.

:: ----------------------------------------------------------------------------
:: 步骤 6: 创建快捷启动脚本
:: Step 6: Create launcher script
:: ----------------------------------------------------------------------------
:: 此脚本使用 weasis:// 协议启动 Weasis 并加载插件配置
:: This script uses weasis:// protocol to launch Weasis with plugin config
::
:: 命令格式说明 / Command format explanation:
::   weasis://$weasis:config pro="felix.extended.config.properties file:///path/to/config.json"
::
::   - weasis://         : Weasis 协议处理器 / Weasis protocol handler
::   - $weasis:config    : 配置命令 / Configuration command  
::   - pro=              : 属性参数 / Property parameter
::   - felix.extended.config.properties : 告诉 Felix 加载扩展配置
::                                        Tells Felix to load extended config
::
set "LAUNCH_SCRIPT=%CONFIG_DIR%\启动Weasis-含测量增强插件.bat"

echo [INFO] 创建启动脚本 / Creating launcher script...

:: 写入启动脚本内容 / Write launcher script content
echo @echo off> "%LAUNCH_SCRIPT%"
echo :: Weasis Measure Enhance Plugin Launcher>> "%LAUNCH_SCRIPT%"
echo :: 自动生成的启动脚本 / Auto-generated launcher script>> "%LAUNCH_SCRIPT%"
echo :: 配置文件 / Config: %CONFIG_FILE%>> "%LAUNCH_SCRIPT%"
echo.>> "%LAUNCH_SCRIPT%"
echo chcp 65001 ^>nul>> "%LAUNCH_SCRIPT%"
echo echo.>> "%LAUNCH_SCRIPT%"
echo echo 正在启动 Weasis (含测量增强插件)...>> "%LAUNCH_SCRIPT%"
echo echo Starting Weasis (with Measure Enhance Plugin)...>> "%LAUNCH_SCRIPT%"
echo echo.>> "%LAUNCH_SCRIPT%"
echo start "" "weasis://$weasis:config pro=\"felix.extended.config.properties file:///%CONFIG_DIR:\=/%/measure-enhance.json\"">> "%LAUNCH_SCRIPT%"

echo [OK] 启动脚本已创建 / Launcher script created:
echo      %LAUNCH_SCRIPT%
echo.

:: ----------------------------------------------------------------------------
:: 步骤 7: 复制到桌面（可选）
:: Step 7: Copy to desktop (optional)
:: ----------------------------------------------------------------------------
set "DESKTOP=%USERPROFILE%\Desktop"
set "DESKTOP_SCRIPT=%DESKTOP%\启动Weasis-含测量增强插件.bat"

copy "%LAUNCH_SCRIPT%" "%DESKTOP_SCRIPT%" >nul 2>&1
if %errorlevel%==0 (
    echo [OK] 桌面快捷方式已创建 / Desktop shortcut created:
    echo      %DESKTOP_SCRIPT%
) else (
    echo [INFO] 无法创建桌面快捷方式，请手动复制
    echo        Could not create desktop shortcut, please copy manually
    echo        From: %LAUNCH_SCRIPT%
)

:: ----------------------------------------------------------------------------
:: 完成 / Done
:: ----------------------------------------------------------------------------
echo.
echo ============================================
echo   安装完成！ / Installation Complete!
echo ============================================
echo.
echo 使用方法 / How to use:
echo   1. 双击桌面的 "启动Weasis-含测量增强插件.bat"
echo      Double-click "启动Weasis-含测量增强插件.bat" on desktop
echo.
echo   2. 或运行 / Or run: 
echo      %LAUNCH_SCRIPT%
echo.
echo 文件位置 / File locations:
echo   插件 JAR  / Plugin JAR  : %JAR_FILE%
echo   配置文件 / Config file : %CONFIG_FILE%
echo   启动脚本 / Launcher    : %LAUNCH_SCRIPT%
echo.
echo 如需卸载，删除以下文件夹即可:
echo To uninstall, just delete this folder:
echo   %CONFIG_DIR%
echo.
pause
