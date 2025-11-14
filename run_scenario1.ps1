# === Scenario 1: Ideal Network (Reliable) ===

# 1) Build the project
mvn -q -DskipTests package

# 2) Ensure logs folder exists and is clean
New-Item -Force -ItemType Directory -Path logs | Out-Null
Remove-Item -Path "logs\*.log" -Force -ErrorAction SilentlyContinue

# 3) Launch M1..M9 each in a titled PowerShell window
$root = Get-Location
$javaClass = "au.edu.adelaide.paxos.app.CouncilMember"

for ($i = 1; $i -le 9; $i++) {
    $m = "M$i"
    $logPath = "logs\$m.log"

    $cmd = "[console]::Title='$m'; java -cp target\classes $javaClass $m --profile reliable --config network.config | Tee-Object -FilePath '$logPath' -Append"

    Start-Process powershell -WorkingDirectory $root -ArgumentList "-NoLogo", "-NoExit", "-Command", $cmd
}

# 4) Give members time to start and then propose M5 from M4
Start-Sleep -Seconds 2
.\send_proposal.ps1 -port 9004 -line "M5"

Write-Host "`n✅ Scenario 1 started."
Write-Host "Each window title shows M1–M9."
Write-Host "M4 proposes M5 — watch its window for CONSENSUS."
Write-Host "Logs are being written under the logs folder."
