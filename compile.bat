@echo off
REM ──────────────────────────────────────────────
REM  Personal Task Manager — Build & Run (Windows)
REM ──────────────────────────────────────────────
REM Usage:
REM   compile.bat          → compile only
REM   compile.bat run      → compile + run
REM   compile.bat clean    → remove build output
REM ──────────────────────────────────────────────

SET PROJECT_ROOT=%~dp0
SET SRC_DIR=%PROJECT_ROOT%src
SET OUT_DIR=%PROJECT_ROOT%out
SET MAIN_CLASS=taskmanager.Main

IF "%1"=="clean" (
    echo Cleaning build output...
    IF EXIST "%OUT_DIR%" RMDIR /S /Q "%OUT_DIR%"
    echo Done.
    EXIT /B 0
)

echo Compiling sources...
IF NOT EXIST "%OUT_DIR%" MKDIR "%OUT_DIR%"

REM Collect all Java files
DIR /S /B "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

javac -d "%OUT_DIR%" @"%OUT_DIR%\sources.txt"
IF ERRORLEVEL 1 (
    echo Compilation FAILED.
    EXIT /B 1
)
echo Compilation successful.

IF "%1"=="run" (
    echo Launching application...
    CD /D "%PROJECT_ROOT%"
    java -cp "%OUT_DIR%" %MAIN_CLASS%
)
