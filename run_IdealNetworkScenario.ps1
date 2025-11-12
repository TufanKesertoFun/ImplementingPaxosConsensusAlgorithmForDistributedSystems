Write-Host "=== Scenario 1: Ideal Network (Reliable) ==="
mvn -q clean package
New-Item -Force -ItemType Directory -Path logs | Out-Null

$members = 1..9 | ForEach-Object { "M$_" }

foreach ($m in $members) {
  Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "java -cp target\classes au.edu.adelaide.paxos.app.CouncilMember $m --profile reliable --config network.config *>> logs/$m.log"
  )
}

Start-Sleep -Seconds 1
.\send_proposal.ps1 -port 9004 -line "M5"

Write-Host "Consensus initiated by M4 for M5. Check logs folder for election results."
