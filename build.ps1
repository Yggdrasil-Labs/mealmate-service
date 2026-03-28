# ============================================
# Build Script (Windows PowerShell)
# ============================================

param(
    [string]$Tag = "yggdrasil-labs/midgard-backend-template:latest",
    [switch]$NoCache
)

$GITHUB_ACTOR = $env:GITHUB_ACTOR
$GITHUB_TOKEN = $env:GITHUB_TOKEN

# Debug: 显示环境变量状态
if ($GITHUB_ACTOR) {
    Write-Host "Found GITHUB_ACTOR from environment" -ForegroundColor Green
} else {
    Write-Host "GITHUB_ACTOR not found in environment" -ForegroundColor Yellow
    $GITHUB_ACTOR = Read-Host "Enter GitHub username"
}

if ($GITHUB_TOKEN) {
    Write-Host "Found GITHUB_TOKEN from environment" -ForegroundColor Green
} else {
    Write-Host "GITHUB_TOKEN not found in environment" -ForegroundColor Yellow
    $GITHUB_TOKEN = Read-Host "Enter GitHub Token" -AsSecureString
    $GITHUB_TOKEN = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($GITHUB_TOKEN))
}

# Proxy settings (modify according to your VPN)
$PROXY = "http://host.docker.internal:7890"

Write-Host "Building image: $Tag" -ForegroundColor Green
Write-Host "Using proxy: $PROXY" -ForegroundColor Yellow
Write-Host "BuildKit cache enabled for faster dependency downloads" -ForegroundColor Cyan

# 启用 BuildKit（支持缓存挂载）
$env:DOCKER_BUILDKIT = "1"

$buildArgs = @(
    "build"
    "--build-arg", "GITHUB_ACTOR=$GITHUB_ACTOR"
    "--build-arg", "GITHUB_TOKEN=$GITHUB_TOKEN"
    "--build-arg", "HTTP_PROXY=$PROXY"
    "--build-arg", "HTTPS_PROXY=$PROXY"
    "-t", $Tag
)

if ($NoCache) {
    $buildArgs += "--no-cache"
}

$buildArgs += "."

docker @buildArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild succeeded!" -ForegroundColor Green
    Write-Host "Run: docker run -d --name <container-name> -p 8081:8081 -p 20880:20880 -e SPRING_PROFILES_ACTIVE=dev $Tag"
} else {
    Write-Host "`nBuild failed!" -ForegroundColor Red
}
