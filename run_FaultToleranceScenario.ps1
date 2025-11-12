Write-Host "=== Scenario 3: Fault-Tolerance Test ==="
taskkill /F /IM java.exe 2>$null | Out-Null
Start-Sleep -Seconds 1

mvn -q clean package
New-Item -Force -ItemType Directory -Path logs | Out-Null

$profiles = @{
  "M1"="reliable"; "M2"="latent"; "M3"="failure";
  "M4"="standard"; "M5"="standard"; "M6"="standard";
  "M7"="standard"; "M8"="standard"; "M9"="standard"
}

foreach ($kv in $profiles.GetEnumerator()) {
  $m = $kv.Key; $p = $kv.Value
  Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "java -cp target\classes au.edu.adelaide.paxos.app.CouncilMember $m --profile $p --config network.config *>> logs/$m.log"
  )
}

Start-Sleep -Seconds 2
Write-Host "🧩 3a: M4 proposes M5"
.\send_proposal.ps1 -port 9004 -line "M5"
Start-Sleep -Seconds 3

Write-Host "🐢 3b: M2 (latent) proposes M2"
.\send_proposal.ps1 -port 9002 -line "M2"
Start-Sleep -Seconds 4

Write-Host "💥 3c: M3 (failure) proposes M3 then crashes"
.\send_proposal.ps1 -port 9003 -line "M3"
Start-Sleep -Milliseconds 600
Get-Process java | Where-Object { $_.MainWindowTitle -like "*M3 --profile failure*" } | Stop-Process -Force -ErrorAction SilentlyContinue

Start-Sleep -Seconds 1
Write-Host "🚑 M5 rescues and re-proposes M5"
.\send_proposal.ps1 -port 9005 -line "M5"
Start-Sleep -Seconds 4

Write-Host "Scenario 3 complete. Inspect logs/ for consensus results."
