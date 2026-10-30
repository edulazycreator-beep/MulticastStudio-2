@echo off
setlocal
set GRADLE_VERSION=9.5.0
if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set DIST_ROOT=%GRADLE_USER_HOME%\manual-dists
set GRADLE_HOME=%DIST_ROOT%\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%GRADLE_HOME%\bin\gradle.bat
if not exist "%GRADLE_BIN%" (
  if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip'; Expand-Archive -Force '%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip' '%DIST_ROOT'; Remove-Item '%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_BIN%" %*
endlocal
