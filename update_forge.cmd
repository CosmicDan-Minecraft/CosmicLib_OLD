@echo off
call gradlew.bat setupDecompWorkspace
call gradlew.bat eclipse
echo.
echo.
echo.
echo Done!
pause
