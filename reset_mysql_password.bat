@echo off
echo 正在尝试重置MySQL root用户密码...
echo.

REM 停止MySQL服务
echo 停止MySQL服务...
net stop mysql

REM 以安全模式启动MySQL（跳过权限验证）
echo 以安全模式启动MySQL...
start /b mysqld --skip-grant-tables --skip-networking

REM 等待MySQL启动
echo 等待MySQL启动...
timeout /t 5

REM 连接到MySQL并重置密码
echo 重置密码为: 123456
mysql -u root -e "UPDATE mysql.user SET authentication_string=PASSWORD('123456') WHERE User='root';"
mysql -u root -e "FLUSH PRIVILEGES;"

REM 停止安全模式的MySQL
echo 停止安全模式MySQL...
taskkill /f /im mysqld.exe

REM 重新启动正常的MySQL服务
echo 重新启动MySQL服务...
net start mysql

echo.
echo 密码重置完成！新密码是: 123456
pause