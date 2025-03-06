
"D:\Program Files\jdk-21\bin\jlink.exe" --module-path "D:\Program Files\jdk-21\jmods";bin --add-modules ok.kpaint --output jre
"D:\Program Files\Launch4j\launch4jc.exe" config.xml

ren KPaint.exe KPaint%1.exe
del KPaint*.zip
powershell "Compress-Archive -Path KPaint%1.exe,jre KPaint%1.zip"

