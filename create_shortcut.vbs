Set oWS = WScript.CreateObject("WScript.Shell")
sLinkFile = oWS.SpecialFolders("Desktop") & "\News Visualizer.lnk"
Set oLink = oWS.CreateShortcut(sLinkFile)
oLink.TargetPath = "C:\Users\kriti\Desktop\NewsVisualizer\run.bat"
oLink.WorkingDirectory = "C:\Users\kriti\Desktop\NewsVisualizer"
oLink.Description = "News Visualizer - Analyze and Visualize News Data"
oLink.IconLocation = "C:\Windows\System32\shell32.dll,13"
oLink.Save

WScript.Echo "Desktop shortcut created successfully!"