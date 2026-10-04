param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string[]]$Versions = @(),
    [string]$RunId = ('run-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 6))
)

$ErrorActionPreference = 'Stop'
$multiWorkspace = Split-Path -Parent $PSScriptRoot
$multiSettings = Get-Content -Raw (Join-Path $multiWorkspace 'settings.json') | ConvertFrom-Json
if ($Versions.Count -eq 0) { $Versions = @($multiSettings.versions) }
foreach ($multiVersion in $Versions) {
    if ($multiVersion -notin $multiSettings.versions) { throw "Unsupported version: $multiVersion" }
}
if ($RunId -notmatch '^[a-zA-Z0-9_-]+$') { throw 'Invalid RunId' }
if ([string]::IsNullOrWhiteSpace($JavaHome)) { throw 'Set JAVA_HOME or pass -JavaHome (JDK 25)' }

$multiTemporary = Join-Path $multiWorkspace 'build/tmp'
$multiReports = Join-Path $multiWorkspace "build/multiplayer-tests/$RunId"
New-Item -ItemType Directory -Path $multiTemporary, $multiReports -Force | Out-Null
$multiOldJavaHome = $env:JAVA_HOME
$multiOldJavaOptions = $env:JAVA_TOOL_OPTIONS
$multiExit = 1
try {
    $env:JAVA_HOME = $JavaHome
    $multiTemporaryJava = $multiTemporary.Replace('\', '/')
    $env:JAVA_TOOL_OPTIONS = "$multiOldJavaOptions -Djava.io.tmpdir=`"$multiTemporaryJava`" -Djdk.net.unixdomain.tmpdir=`"$multiTemporaryJava`""
    $multiTasks = @($Versions | ForEach-Object { ":${_}:runMultiplayerTest" })
    Push-Location $multiWorkspace
    try {
        & ./gradlew.bat --no-daemon --continue '-Pteamcraft_multiplayer_tests=true' "-Pteamcraft_test_run=$RunId" @multiTasks 2>&1 |
            Tee-Object -FilePath (Join-Path $multiReports 'gradle.log')
        $multiExit = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $multiOldJavaHome
    $env:JAVA_TOOL_OPTIONS = $multiOldJavaOptions
}

$multiSummary = @()
foreach ($multiVersion in $Versions) {
    $multiResultFile = Join-Path $multiWorkspace "versions/$multiVersion/build/multiplayer-tests/$RunId/test-result.txt"
    if (Test-Path -LiteralPath $multiResultFile) {
        $multiLines = @(Get-Content -LiteralPath $multiResultFile)
        $multiPassed = @($multiLines | Where-Object { $_.StartsWith('PASS: ') }).Count
        $multiSummary += [pscustomobject]@{ Version = $multiVersion; Status = $multiLines[0]; Assertions = $multiPassed; ResultFile = $multiResultFile }
    } else {
        $multiSummary += [pscustomobject]@{ Version = $multiVersion; Status = 'NO_RESULT'; Assertions = 0; ResultFile = $multiResultFile }
    }
}
$multiSummary | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $multiReports 'summary.json') -Encoding utf8
$multiMarkdown = @(
    '# TeamCraft multiplayer server integration results',
    '',
    "Run: $RunId",
    '',
    'Real dedicated servers, four server-side test players with in-memory connections.',
    'GUI request handlers and codecs are exercised directly. This is NOT graphical-client or TCP end-to-end testing.',
    '',
    '| Minecraft | Result | Passed assertions |',
    '| --- | --- | ---: |'
)
foreach ($multiRow in $multiSummary) { $multiMarkdown += "| $($multiRow.Version) | $($multiRow.Status) | $($multiRow.Assertions) |" }
$multiMarkdown += @('', 'Not covered: real client login/handshake, GUI rendering/input, keybindings, latency/disconnect behavior, restart persistence, third-party mod compatibility.')
$multiMarkdown | Set-Content -LiteralPath (Join-Path $multiReports 'report.md') -Encoding utf8
$multiSummary | Format-Table Version, Status, Assertions
Write-Output "Report: $(Join-Path $multiReports 'report.md')"
if ($multiExit -ne 0 -or @($multiSummary | Where-Object Status -NE 'PASS').Count -gt 0) {
    throw 'One or more multiplayer integration tests failed; see the report and gradle.log'
}
