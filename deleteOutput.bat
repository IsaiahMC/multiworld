@echo off
SETLOCAL EnableDelayedExpansion
REM Clean build output script - by Isaiah.

REM Main script execution starts here
set targetDir=%cd%
ECHO Starting directory scan in: %targetDir%

for /D %%d in ("%targetDir%\*") do ( CALL :ProcessFolder "%%d")
for /D %%d in ("%targetDir%\fabric\*") do ( CALL :ProcessFolder "%%d")
for /D %%d in ("%targetDir%\forge\*") do ( CALL :ProcessFolder "%%d")
for /D %%d in ("%targetDir%\neoforge\*") do ( CALL :ProcessFolder "%%d")

REM del %cd%\output\*.jar

ECHO Script finished.
GOTO :EOF REM Exit the main script cleanly

:ProcessFolder
REM The input parameter passed via CALL is accessed using %1
set currentFolder=%1
set libsDir=%currentFolder%\build\libs

REM Check if the directory exists using the parameter directly
IF EXIST %libsDir% (
	ECHO Processing folder: %libsDir%
	DEL  %libsDir%\*.jar
) ELSE (
	REM ECHO Error: Directory not found: %currentFolder%
)

REM Return execution to the line immediately following the CALL command
EXIT /B 0

