Write-Host "=== Scenario 3c: M3 (failure) proposes then crashes; M4 recovers ==="

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

    if ($m -eq "M3") {
        # Failing member starts the first proposal
        $args = "$m --profile $profile --config network.config --propose M5"
    }
    elseif ($m -eq "M4") {
        # Recovery proposer (standard)
        $args = "$m --profile $profile --config network.config --propose M7"
    }
    else {
        $args = "$m --profile $profile --config network.config"
    }

    # Use Console.Title instead of $host escaping
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

Write-Host "Scenario 3c running: letting M3 send PREPAREs..."
Start-Sleep -Seconds 4

Write-Host "Simulating crash: killing M3..."
Stop-Process -Id $procs["M3"].Id -Force

Write-Host "Waiting for M4 to drive a new consensus..."
Start-Sleep -Seconds 25

Write-Host "Scenario 3c done - check logs/ for:"
Write-Host "   M3: PREPAREs then silence (crash)"
Write-Host "   All: final CONSENSUS from M4's proposal"
