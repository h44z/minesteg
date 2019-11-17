@echo off
REM fetch received buffer every minute

@echo %date%-%time% - Starting windows log session! >> received_text.txt

set /a mins = 10
:loop
    @echo %date%-%time% - Received: >> received_text.txt
    java -jar messageclient-1.0.0.jar -p 1098 -t >> received_text.txt
    timeout /T 60 /nobreak
    set /a mins -= 1

    IF "%mins%" == "0" (
        echo #################################
        echo       10 Minutes are over!!!!    
        echo #################################
    )
goto loop
