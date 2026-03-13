# PowerShell script to create desktop shortcut for News Visualizer

$WshShell = New-Object -comObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("$([Environment]::GetFolderPath('Desktop'))\News Visualizer.lnk")
$Shortcut.TargetPath = "C:\Users\kriti\Desktop\NewsVisualizer\run.bat"
$Shortcut.WorkingDirectory = "C:\Users\kriti\Desktop\NewsVisualizer"
$Shortcut.Description = "News Visualizer - Analyze and Visualize News Data"
$Shortcut.IconLocation = "C:\Windows\System32\shell32.dll,13"
$Shortcut.Save()

Write-Host "Desktop shortcut created successfully!" -ForegroundColor Green
Write-Host "You can now double-click 'News Visualizer' on your desktop to launch the application." -ForegroundColor Yellow