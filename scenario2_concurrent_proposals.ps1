# === Scenario 2: Concurrent Proposals (Reliable) ===
# M4 proposes M5, M6 proposes M7 on startup using --propose.

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

    if ($i -eq 4) {
        # M4 proposes candidate M5
        $args = "$m --profile reliable --config network.config --propose M5"
    }
    elseif ($i -eq 9) {
        # M9 proposes candidate M7
        $args = "$m --profile reliable --config network.config --propose M7"
    }
    else {
        # Other members just join the cluster
        $args = "$m --profile reliable --config network.config"
    }

    $cmd = "[console]::Title='$m'; " +
           "java -cp target\classes $javaClass $args " +
           "| Tee-Object -FilePath '$logPath' -Append"

    Start-Process powershell.exe -WorkingDirectory $root `
        -ArgumentList "-NoLogo", "-NoExit", "-Command", $cmd
}

Write-Host ""
Write-Host "=== Scenario 2 started: M4 proposes M5, M6 proposes M7 (concurrently on startup) ==="
Write-Host "Check M1-M9 windows and logs/*.log for PREPARE/ACCEPT and CONSENSUS."
