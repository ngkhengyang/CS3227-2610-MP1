param(
    [string] $PlantUmlVersion = '1.2026.7'
)

$diagramDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$temporaryDirectory = Join-Path $env:TEMP 'degree-progress-plantuml'
$plantUmlJar = Join-Path $temporaryDirectory "plantuml-$PlantUmlVersion.jar"
$downloadUrl = "https://github.com/plantuml/plantuml/releases/download/"
$downloadUrl = "$downloadUrl" + "v$PlantUmlVersion/plantuml-$PlantUmlVersion.jar"
$sourcePath = Join-Path $diagramDirectory 'degree-progress-architecture.puml'

New-Item -ItemType Directory -Force $temporaryDirectory | Out-Null
if (-not (Test-Path -LiteralPath $plantUmlJar)) {
    Write-Host "Downloading PlantUML $PlantUmlVersion..."
    Invoke-WebRequest -Uri $downloadUrl -OutFile $plantUmlJar
}

Write-Host 'Rendering degree-progress-architecture.svg...'
& java -jar $plantUmlJar -tsvg -nometadata $sourcePath
if ($LASTEXITCODE -ne 0) {
    throw "PlantUML exited with status $LASTEXITCODE."
}
