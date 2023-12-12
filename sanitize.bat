
@echo off
setlocal

:: Check if at least one parameter is provided
if "%~1"=="" (
    echo Usage : sanitize final_xml cir_xml  
	echo the cir_xml is optional
    exit /b
)

:: Assign parameters to variables
set "param1=%~1"
set "param2=%~2"

:: Display the parameters
echo First parameter: %param1%

if not "%param2%"=="" (
	bin\java  XMLCharacterValidator --cir "%2" --properties ".\app.properties"  "%1"
) else (
	bin\java  XMLCharacterValidator --properties ".\app.properties"  "%1"
)

endlocal


