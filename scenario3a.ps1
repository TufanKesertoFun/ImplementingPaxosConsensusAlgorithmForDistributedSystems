# === Scenario 3a: Mixed Profiles, M4 (standard) proposes M5 automatically ===

# 1) Build the project
mvn -q -DskipTests package

# 2) Ensure logs folder exists and is clean
New-Item -Force -ItemType Directory -Path logs | Out-Null
Remove-Item -Path "logs\*.log" -Force -ErrorAction SilentlyContinue

# 3) Launch M1..M9 each in a titled PowerShell window with mixed profiles
$root = Get-Location
$javaClass = "au.edu.adelaide.paxos.app.CouncilMember"

for ($i = 1; $i -le 9; $i++) {
    $m = "M$i"
    $logPath = "logs\$m.log"

    # Decide profile for each member
    if ($i -eq 1) {
        $profile = "reliable"
    }
    elseif ($i -eq 2) {
        $profile = "latent"
    }
    elseif ($i -eq 3) {
        $profile = "failure"
    }
    else {
        $profile = "standard"
    }

    if ($i -eq 4) {
        # M4 (standard) automatically proposes M5
        $args = "$m --profile $profile --config network.config --propose M5"
    }
    else {
        # Other members just join the cluster
        $args = "$m --profile $profile --config network.config"
    }

    $cmd = "[console]::Title='$m'; " +
           "java -cp target\classes $javaClass $args " +
           "| Tee-Object -FilePath '$logPath' -Append"

    Start-Process powershell.exe -WorkingDirectory $root `
        -ArgumentList "-NoLogo", "-NoExit", "-Command", $cmd
}

Write-Host ""
Write-Host "=== Scenario 3a started: mixed profiles, M4 (standard) auto-proposes M5 ==="
Write-Host "Check M1-M9 windows and logs/*.log for PREPARE/ACCEPT and CONSENSUS."
