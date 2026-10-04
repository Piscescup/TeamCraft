param([string]$JavaHome = $env:JAVA_HOME)

$ErrorActionPreference = 'Stop'
$inviteGuiWorkspace = Split-Path -Parent $PSScriptRoot
$inviteGuiClasses = Join-Path $inviteGuiWorkspace 'build/tmp/invitation-gui-check/classes'
$inviteGuiSources = @(
    (Join-Path $inviteGuiWorkspace 'src/main/java/io/github/piscescup/fabricmc/teamcraft/team/TeamInvitations.java'),
    (Join-Path $inviteGuiWorkspace 'scripts/checks/InvitationGuiChecks.java')
)
if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    $inviteGuiCompiler = (Get-Command javac -ErrorAction Stop).Source
    $inviteGuiJava = (Get-Command java -ErrorAction Stop).Source
} else {
    $inviteGuiCompiler = Join-Path $JavaHome 'bin/javac.exe'
    $inviteGuiJava = Join-Path $JavaHome 'bin/java.exe'
}
New-Item -ItemType Directory -Path $inviteGuiClasses -Force | Out-Null
& $inviteGuiCompiler -encoding UTF-8 -d $inviteGuiClasses @inviteGuiSources
if ($LASTEXITCODE -ne 0) { throw 'Invitation GUI check compilation failed' }
& $inviteGuiJava -cp $inviteGuiClasses io.github.piscescup.fabricmc.teamcraft.team.InvitationGuiChecks
if ($LASTEXITCODE -ne 0) { throw 'Invitation GUI checks failed' }
