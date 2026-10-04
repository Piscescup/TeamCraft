param([string]$JavaHome = $env:JAVA_HOME)

$ErrorActionPreference = 'Stop'
$guiWorkspace = Split-Path -Parent $PSScriptRoot
$guiClasses = Join-Path $guiWorkspace 'build/tmp/gui-layout-check/classes'
$guiPackage = 'src/main/java/io/github/piscescup/fabricmc/teamcraft/gui/layout'
$guiSources = @(
    (Join-Path $guiWorkspace "$guiPackage/TeamcraftBounds.java"),
    (Join-Path $guiWorkspace "$guiPackage/TeamcraftPageLayout.java"),
    (Join-Path $guiWorkspace "$guiPackage/TeamcraftScrollViewport.java"),
    (Join-Path $guiWorkspace 'src/test/java/io/github/piscescup/fabricmc/teamcraft/gui/layout/TeamcraftLayoutChecks.java')
)
if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    $guiCompiler = (Get-Command javac -ErrorAction Stop).Source
    $guiJava = (Get-Command java -ErrorAction Stop).Source
} else {
    $guiCompiler = Join-Path $JavaHome 'bin/javac.exe'
    $guiJava = Join-Path $JavaHome 'bin/java.exe'
}
New-Item -ItemType Directory -Path $guiClasses -Force | Out-Null
& $guiCompiler -encoding UTF-8 -d $guiClasses @guiSources
if ($LASTEXITCODE -ne 0) { throw 'GUI layout test compilation failed' }
& $guiJava -cp $guiClasses io.github.piscescup.fabricmc.teamcraft.gui.layout.TeamcraftLayoutChecks
if ($LASTEXITCODE -ne 0) { throw 'GUI layout checks failed' }
