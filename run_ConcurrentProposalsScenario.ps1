Write-Host "=== Scenario 2: Concurrent Proposals ==="

# assumes all reliable members from Scenario 1 are running
Start-Sleep -Seconds 1

.\send_proposal.ps1 -port 9001 -line "M1"
Start-Sleep -Milliseconds 150
.\send_proposal.ps1 -port 9008 -line "M8"

Write-Host "Both proposals triggered (M1 and M8). Observe which wins consensus in logs."
