param([string]$JavaHome = $env:JAVA_HOME)

$ErrorActionPreference = 'Stop'
$inviteWorkspace = Split-Path -Parent $PSScriptRoot
$inviteClasses = Join-Path $inviteWorkspace 'build/tmp/invitation-check/classes'
$invitePackage = 'io/github/piscescup/fabricmc/teamcraft/team'
$inviteSources = @(
    (Join-Path $inviteWorkspace "src/main/java/$invitePackage/TeamInvitations.java"),
    (Join-Path $inviteWorkspace "src/test/java/$invitePackage/TeamInvitationChecks.java")
)
if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    $inviteCompiler = (Get-Command javac -ErrorAction Stop).Source
    $inviteJava = (Get-Command java -ErrorAction Stop).Source
} else {
    $inviteCompiler = Join-Path $JavaHome 'bin/javac.exe'
    $inviteJava = Join-Path $JavaHome 'bin/java.exe'
}
New-Item -ItemType Directory -Path $inviteClasses -Force | Out-Null
& $inviteCompiler -encoding UTF-8 -d $inviteClasses @inviteSources
if ($LASTEXITCODE -ne 0) { throw 'Invitation check compilation failed' }
& $inviteJava -cp $inviteClasses io.github.piscescup.fabricmc.teamcraft.team.TeamInvitationChecks
if ($LASTEXITCODE -ne 0) { throw 'Invitation checks failed' }
