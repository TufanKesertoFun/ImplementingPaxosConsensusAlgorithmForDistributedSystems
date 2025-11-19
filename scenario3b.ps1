Write-Host "=== Scenario 3b: Mixed Profiles, M2 (latent) proposes ==="

mvn -q clean package

if (Test-Path "logs") { Remove-Item "logs" -Recurse -Force }
New-Item -Force -ItemType Directory -Path logs | Out-Null

function Get-ProfileForMember($m) {
    switch ($m) {
        "M1" { "reliable" }
        "M2" { "latent" }
        "M3" { "failure" }
        default { "standard" }
    }
}

$members = 1..9 | ForEach-Object { "M$_" }
$procs = @{}

foreach ($m in $members) {
    $profile = Get-ProfileForMember $m

    if ($m -eq "M2") {
        # Latent member proposer
        $args = "$m --profile $profile --config network.config --propose M6"
    } else {
        $args = "$m --profile $profile --config network.config"
    }

    # Build a simple command: set window title, run Java, tee to log
    $cmd = "& { " +
           "[Console]::Title = '$m'; " +
           "java -cp target\classes au.edu.adelaide.paxos.app.CouncilMember $args 2>&1 | " +
           "Tee-Object -FilePath 'logs/$m.log' -Append" +
           " }"

    $proc = Start-Process powershell -PassThru -ArgumentList @(
        "-NoLogo",
        "-NoExit",
        "-Command",
        $cmd
    )

    $procs[$m] = $proc
}

Write-Host "Scenario 3b running (M2 is latent proposer, may be slower)..."
Start-Sleep -Seconds 35
Write-Host "Scenario 3b done  check logs/ for CONSENSUS."
