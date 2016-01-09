@echo off
@set START_DIR=%cd%
@set CALL_PATH=%~dp0

@cd %CALL_PATH%
@rem "Get build generated content of classpath file in order to run Java Program:"
@rem The classpath can be long. Set command on windows has limit to 1024. The for loop is workaround.
@set CLASSPATHVAL=
@FOR /F "tokens=*" %%G IN ('type classpath.txt') DO set CLASSPATHVAL=%CLASSPATHVAL%%%G
@rem "Replace UNIX Java library separator (:) with Windows specific separator (;)"
@set CLASSPATHVAL=%CLASSPATHVAL:/=\%
@rem "Convert relative path to absolute based on content of CALL_PATH variable:"
@set mycmd=set CLASSPATHVAL=%%CLASSPATHVAL:REPLACEMENT_TOKEN\=%CALL_PATH%%%
@call %mycmd%
@cd %START_DIR%

@java -classpath "%CLASSPATHVAL%" com.pitsu.tools.JndiEvaluator %*