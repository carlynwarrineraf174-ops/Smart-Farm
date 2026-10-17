@echo off
cd /d "%~dp0"
if not exist out mkdir out
xcopy /E /I /Y src\style.css out\style.css >nul 2>&1
javac -encoding UTF-8 -d out -sourcepath src src\Main.java 2>build.err
if %errorlevel%==0 (
    start "" javaw -Dfile.encoding=UTF-8 -cp out Main
    exit
)
echo Build failed, see build.err
pause
