@echo off
echo ==========================================
echo   CMCC Smoke Test
echo ==========================================
echo.
echo Prerequisites:
echo   1. Run "mvn spring-boot:run" in a separate terminal
echo   2. Wait until app starts on http://localhost:8080
echo.
echo Press any key to run the smoke tests...
pause >nul

powershell -ExecutionPolicy Bypass -File "%~dp0smoke-test.ps1"

if %errorlevel% neq 0 (
    echo.
    echo Some tests FAILED. Check details above.
) else (
    echo.
    echo All tests PASSED!
)

pause
