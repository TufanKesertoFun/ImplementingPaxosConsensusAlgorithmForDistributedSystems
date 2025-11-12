Write-Host "=== Running All Paxos Scenarios Sequentially ==="

.\run_IdealNetworkScenario.ps1
Start-Sleep -Seconds 5
.\run_ConcurrentProposalsScenario.ps1
Start-Sleep -Seconds 5
.\run_FaultToleranceScenario.ps1

Write-Host "✅ All scenarios executed. Check logs/ folder."
