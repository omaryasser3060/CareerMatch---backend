# ============================================================
# CareerMatch Backend - API Test Script
# ============================================================
# Tests all endpoints and generates a detailed report
# Usage: .\test-api.ps1
# ============================================================

param(
    [string]$BaseUrl = "http://localhost:8080"
)

# ============================================================
# Setup
# ============================================================
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# Colors
$script:ColorSuccess = "Green"
$script:ColorFail = "Red"
$script:ColorInfo = "Cyan"
$script:ColorWarn = "Yellow"
$script:ColorHeader = "Magenta"

# Counters
$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0
$script:Warnings = 0

# Results
$script:Results = @()

# Storage for tokens/IDs
$script:Token = $null
$script:RefreshToken = $null
$script:UserId = $null
$script:CvId = $null
$script:JobId = $null
$script:MatchId = $null

# ============================================================
# Helper Functions
# ============================================================
function Write-Header {
    param([string]$Text)
    Write-Host ""
    Write-Host ("=" * 70) -ForegroundColor $script:ColorHeader
    Write-Host " $Text" -ForegroundColor $script:ColorHeader
    Write-Host ("=" * 70) -ForegroundColor $script:ColorHeader
}

function Write-Section {
    param([string]$Text)
    Write-Host ""
    Write-Host "--- $Text ---" -ForegroundColor $script:ColorInfo
}

function Write-Pass {
    param([string]$TestName, [string]$Details = "")
    $script:TotalTests++
    $script:PassedTests++
    Write-Host "  [PASS] $TestName" -ForegroundColor $script:ColorSuccess
    if ($Details) {
        Write-Host "         $Details" -ForegroundColor DarkGray
    }
    $script:Results += [PSCustomObject]@{
        Test = $TestName
        Status = "PASS"
        Details = $Details
    }
}

function Write-Fail {
    param([string]$TestName, [string]$Details = "")
    $script:TotalTests++
    $script:FailedTests++
    Write-Host "  [FAIL] $TestName" -ForegroundColor $script:ColorFail
    if ($Details) {
        Write-Host "         $Details" -ForegroundColor DarkRed
    }
    $script:Results += [PSCustomObject]@{
        Test = $TestName
        Status = "FAIL"
        Details = $Details
    }
}

function Write-Warn {
    param([string]$TestName, [string]$Details = "")
    $script:Warnings++
    Write-Host "  [WARN] $TestName" -ForegroundColor $script:ColorWarn
    if ($Details) {
        Write-Host "         $Details" -ForegroundColor DarkYellow
    }
    $script:Results += [PSCustomObject]@{
        Test = $TestName
        Status = "WARN"
        Details = $Details
    }
}

function Write-Info {
    param([string]$Text)
    Write-Host "  [INFO] $Text" -ForegroundColor $script:ColorInfo
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{},
        [switch]$SkipAuth
    )

    $uri = "$BaseUrl$Endpoint"
    $params = @{
        Uri = $uri
        Method = $Method
        ContentType = "application/json"
        ErrorAction = "Stop"
    }

    if ($Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 10)
    }

    $allHeaders = @{}
    if (-not $SkipAuth -and $script:Token) {
        $allHeaders["Authorization"] = "Bearer $($script:Token)"
    }
    foreach ($key in $Headers.Keys) {
        $allHeaders[$key] = $Headers[$key]
    }
    if ($allHeaders.Count -gt 0) {
        $params.Headers = $allHeaders
    }

    try {
        $response = Invoke-RestMethod @params
        return @{
            Success = $true
            Data = $response
            StatusCode = 200
        }
    }
    catch {
        $statusCode = 0
        $errorMessage = $_.Exception.Message

        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }

        return @{
            Success = $false
            Data = $null
            StatusCode = $statusCode
            Error = $errorMessage
        }
    }
}

# ============================================================
# Main Test Flow
# ============================================================

Clear-Host

Write-Header "CareerMatch Backend - API Test Suite"
Write-Host "  Base URL: $BaseUrl" -ForegroundColor Gray
Write-Host "  Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray

# ============================================================
# 1. Health Check
# ============================================================
Write-Section "1. Health Check"

$health = Invoke-Api -Method "GET" -Endpoint "/actuator/health" -SkipAuth

if ($health.Success -and $health.Data.status -eq "UP") {
    Write-Pass "GET /actuator/health" "Status: UP"

    if ($health.Data.components.db.status -eq "UP") {
        Write-Pass "Database connection" "PostgreSQL: $($health.Data.components.db.details.database)"
    } else {
        Write-Fail "Database connection" "DB status: $($health.Data.components.db.status)"
    }
} else {
    Write-Fail "GET /actuator/health" "Status: $($health.Data.status) - $($health.Error)"
}

# ============================================================
# 2. Authentication - Register
# ============================================================
Write-Section "2. Authentication - Register"

$testEmail = "testuser_$(Get-Random)@careermatch.com"
$registerBody = @{
    name = "Test User"
    email = $testEmail
    password = "Password123!"
    confirmPassword = "Password123!"
    acceptTerms = $true
}

$register = Invoke-Api -Method "POST" -Endpoint "/api/auth/register" -Body $registerBody -SkipAuth

if ($register.Success) {
    Write-Pass "POST /api/auth/register" "User: $testEmail"
    $script:UserId = $register.Data.data.user.id
    $script:Token = $register.Data.data.accessToken
    $script:RefreshToken = $register.Data.data.refreshToken
    Write-Info "User ID: $($script:UserId)"
    Write-Info "Access Token: $($script:Token.Substring(0, 30))..."
} else {
    Write-Fail "POST /api/auth/register" "Status: $($register.StatusCode) - $($register.Error)"
}

# ============================================================
# 3. Authentication - Login
# ============================================================
Write-Section "3. Authentication - Login"

$loginBody = @{
    email = "test@careermatch.com"
    password = "Password123!"
}

$login = Invoke-Api -Method "POST" -Endpoint "/api/auth/login" -Body $loginBody -SkipAuth

if ($login.Success) {
    Write-Pass "POST /api/auth/login" "User: test@careermatch.com"
    $script:Token = $login.Data.data.accessToken
    $script:RefreshToken = $login.Data.data.refreshToken
    $script:UserId = $login.Data.data.user.id
    Write-Info "Token: $($script:Token.Substring(0, 30))..."
} else {
    Write-Fail "POST /api/auth/login" "Status: $($login.StatusCode) - $($login.Error)"
}

# ============================================================
# 4. Authentication - Invalid Login (should fail)
# ============================================================
Write-Section "4. Authentication - Invalid Login (Negative Test)"

$badLoginBody = @{
    email = "test@careermatch.com"
    password = "WrongPassword123!"
}

$badLogin = Invoke-Api -Method "POST" -Endpoint "/api/auth/login" -Body $badLoginBody -SkipAuth

if (-not $badLogin.Success -and ($badLogin.StatusCode -eq 401 -or $badLogin.StatusCode -eq 400)) {
    Write-Pass "Invalid login rejected" "Status: $($badLogin.StatusCode)"
} else {
    Write-Warn "Invalid login" "Expected 401/400, got: $($badLogin.StatusCode)"
}

# ============================================================
# 5. User Profile
# ============================================================
Write-Section "5. User Profile"

$profile = Invoke-Api -Method "GET" -Endpoint "/api/user/profile"

if ($profile.Success) {
    Write-Pass "GET /api/user/profile" "User: $($profile.Data.data.email)"
    Write-Info "Name: $($profile.Data.data.name)"
    Write-Info "Role: $($profile.Data.data.role)"
} else {
    Write-Fail "GET /api/user/profile" "Status: $($profile.StatusCode) - $($profile.Error)"
}

# ============================================================
# 6. User Profile - Unauthorized (Negative Test)
# ============================================================
Write-Section "6. User Profile - Unauthorized (Negative Test)"

$oldToken = $script:Token
$script:Token = $null
$profileNoAuth = Invoke-Api -Method "GET" -Endpoint "/api/user/profile"
$script:Token = $oldToken

if (-not $profileNoAuth.Success -and ($profileNoAuth.StatusCode -eq 401 -or $profileNoAuth.StatusCode -eq 403)) {
    Write-Pass "Unauthorized access rejected" "Status: $($profileNoAuth.StatusCode)"
} else {
    Write-Warn "Unauthorized access" "Expected 401/403, got: $($profileNoAuth.StatusCode)"
}

# ============================================================
# 7. User Settings
# ============================================================
Write-Section "7. User Settings"

$settings = Invoke-Api -Method "GET" -Endpoint "/api/user/settings"

if ($settings.Success) {
    Write-Pass "GET /api/user/settings"
} else {
    Write-Fail "GET /api/user/settings" "Status: $($settings.StatusCode) - $($settings.Error)"
}

# ============================================================
# 8. Jobs - List (Public)
# ============================================================
Write-Section "8. Jobs - List All (Public)"

$jobs = Invoke-Api -Method "GET" -Endpoint "/api/jobs?page=0&size=10" -SkipAuth

if ($jobs.Success) {
    $totalJobs = $jobs.Data.data.totalCount
    $resultCount = $jobs.Data.data.results.Count
    Write-Pass "GET /api/jobs" "Total jobs: $totalJobs, Returned: $resultCount"

    if ($totalJobs -ge 15) {
        Write-Pass "Seed jobs present" "Expected: 15, Found: $totalJobs"
    } else {
        Write-Warn "Seed jobs" "Expected: 15, Found: $totalJobs"
    }

    # Save first job ID for later
    if ($resultCount -gt 0) {
        $script:JobId = $jobs.Data.data.results[0].id
        Write-Info "First job ID: $($script:JobId)"
    }
} else {
    Write-Fail "GET /api/jobs" "Status: $($jobs.StatusCode) - $($jobs.Error)"
}

# ============================================================
# 9. Jobs - Search
# ============================================================
Write-Section "9. Jobs - Search"

$search = Invoke-Api -Method "GET" -Endpoint "/api/jobs?query=backend" -SkipAuth

if ($search.Success) {
    Write-Pass "GET /api/jobs?query=backend" "Found: $($search.Data.data.totalCount) jobs"
} else {
    Write-Fail "GET /api/jobs?query=backend" "Status: $($search.StatusCode)"
}

# ============================================================
# 10. Jobs - Filter by Location
# ============================================================
Write-Section "10. Jobs - Filter by Location"

$filter = Invoke-Api -Method "GET" -Endpoint "/api/jobs?location=Cairo" -SkipAuth

if ($filter.Success) {
    Write-Pass "GET /api/jobs?location=Cairo" "Found: $($filter.Data.data.totalCount) jobs"
} else {
    Write-Fail "GET /api/jobs?location=Cairo" "Status: $($filter.StatusCode)"
}

# ============================================================
# 11. Jobs - Get Details
# ============================================================
Write-Section "11. Jobs - Get Details"

if ($script:JobId) {
    $jobDetail = Invoke-Api -Method "GET" -Endpoint "/api/jobs/$($script:JobId)" -SkipAuth

    if ($jobDetail.Success) {
        Write-Pass "GET /api/jobs/$($script:JobId)" "Title: $($jobDetail.Data.data.title)"
    } else {
        Write-Fail "GET /api/jobs/$($script:JobId)" "Status: $($jobDetail.StatusCode)"
    }
}

# ============================================================
# 12. Jobs - Get Non-Existent (Negative Test)
# ============================================================
Write-Section "12. Jobs - Get Non-Existent (Negative Test)"

$notFound = Invoke-Api -Method "GET" -Endpoint "/api/jobs/nonexistent-job-999" -SkipAuth

if (-not $notFound.Success -and $notFound.StatusCode -eq 404) {
    Write-Pass "Non-existent job returns 404" "Status: $($notFound.StatusCode)"
} else {
    Write-Warn "Non-existent job" "Expected 404, got: $($notFound.StatusCode)"
}

# ============================================================
# 13. Jobs - Distinct Companies
# ============================================================
Write-Section "13. Jobs - Distinct Companies"

$companies = Invoke-Api -Method "GET" -Endpoint "/api/jobs/companies" -SkipAuth

if ($companies.Success) {
    Write-Pass "GET /api/jobs/companies" "Found: $($companies.Data.data.Count) companies"
} else {
    Write-Fail "GET /api/jobs/companies" "Status: $($companies.StatusCode)"
}

# ============================================================
# 14. Jobs - Distinct Locations
# ============================================================
Write-Section "14. Jobs - Distinct Locations"

$locations = Invoke-Api -Method "GET" -Endpoint "/api/jobs/locations" -SkipAuth

if ($locations.Success) {
    Write-Pass "GET /api/jobs/locations" "Found: $($locations.Data.data.Count) locations"
} else {
    Write-Fail "GET /api/jobs/locations" "Status: $($locations.StatusCode)"
}

# ============================================================
# 15. CV - List User CVs
# ============================================================
Write-Section "15. CV - List User CVs"

$cvList = Invoke-Api -Method "GET" -Endpoint "/api/cv"

if ($cvList.Success) {
    Write-Pass "GET /api/cv"

    # Save first CV ID
    if ($cvList.Data.data.content -and $cvList.Data.data.content.Count -gt 0) {
        $script:CvId = $cvList.Data.data.content[0].id
        Write-Info "First CV ID: $($script:CvId)"
    }
} else {
    Write-Fail "GET /api/cv" "Status: $($cvList.StatusCode)"
}

# ============================================================
# 16. CV - Get Profile
# ============================================================
Write-Section "16. CV - Get Profile"

if ($script:CvId) {
    $cvProfile = Invoke-Api -Method "GET" -Endpoint "/api/cv/$($script:CvId)/profile"

    if ($cvProfile.Success) {
        Write-Pass "GET /api/cv/$($script:CvId)/profile"
    } else {
        Write-Warn "GET /api/cv/$($script:CvId)/profile" "Status: $($cvProfile.StatusCode)"
    }
} else {
    Write-Info "No CV found - skipping profile test"
}

# ============================================================
# 17. Match - Get History
# ============================================================
Write-Section "17. Match - Get History"

$matchHistory = Invoke-Api -Method "GET" -Endpoint "/api/match/history"

if ($matchHistory.Success) {
    Write-Pass "GET /api/match/history"

    if ($matchHistory.Data.data.results -and $matchHistory.Data.data.results.Count -gt 0) {
        $script:MatchId = $matchHistory.Data.data.results[0].matchId
        Write-Info "First match ID: $($script:MatchId)"
    }
} else {
    Write-Fail "GET /api/match/history" "Status: $($matchHistory.StatusCode)"
}

# ============================================================
# 18. Match - Get Match Result
# ============================================================
Write-Section "18. Match - Get Match Result"

if ($script:MatchId) {
    $matchResult = Invoke-Api -Method "GET" -Endpoint "/api/match/$($script:MatchId)"

    if ($matchResult.Success) {
        Write-Pass "GET /api/match/$($script:MatchId)" "Score: $($matchResult.Data.data.overallMatchScore)"
    } else {
        Write-Fail "GET /api/match/$($script:MatchId)" "Status: $($matchResult.StatusCode)"
    }
} else {
    Write-Info "No match found - skipping"
}

# ============================================================
# 19. Match - Analyze (New Match)
# ============================================================
Write-Section "19. Match - Analyze (Create New Match)"

if ($script:CvId -and $script:JobId) {
    $analyzeBody = @{
        cvId = $script:CvId
        jobId = $script:JobId
    }

    Write-Info "This might take 10-30 seconds (AI analysis)..."
    $analyze = Invoke-Api -Method "POST" -Endpoint "/api/match/analyze" -Body $analyzeBody

    if ($analyze.Success) {
        Write-Pass "POST /api/match/analyze" "Score: $($analyze.Data.data.overallMatchScore)"
    } else {
        Write-Warn "POST /api/match/analyze" "Status: $($analyze.StatusCode) - $($analyze.Error)"
    }
} else {
    Write-Info "Missing CV or Job ID - skipping analysis"
}

# ============================================================
# 20. Swagger UI
# ============================================================
Write-Section "20. Swagger UI"

try {
    $swagger = Invoke-WebRequest -Uri "$BaseUrl/swagger-ui.html" -UseBasicParsing -ErrorAction Stop
    if ($swagger.StatusCode -eq 200) {
        Write-Pass "GET /swagger-ui.html" "Status: 200"
    } else {
        Write-Warn "GET /swagger-ui.html" "Status: $($swagger.StatusCode)"
    }
} catch {
    Write-Warn "GET /swagger-ui.html" "Not accessible"
}

# ============================================================
# 21. API Docs
# ============================================================
Write-Section "21. OpenAPI Docs"

$apiDocs = Invoke-Api -Method "GET" -Endpoint "/api-docs" -SkipAuth

if ($apiDocs.Success) {
    Write-Pass "GET /api-docs" "OpenAPI spec available"
} else {
    Write-Warn "GET /api-docs" "Status: $($apiDocs.StatusCode)"
}

# ============================================================
# Report
# ============================================================
Write-Header "Test Report"

$passRate = if ($script:TotalTests -gt 0) {
    [math]::Round(($script:PassedTests / $script:TotalTests) * 100, 2)
} else { 0 }

Write-Host ""
Write-Host "  Total Tests:    $($script:TotalTests)" -ForegroundColor White
Write-Host "  Passed:         $($script:PassedTests)" -ForegroundColor Green
Write-Host "  Failed:         $($script:FailedTests)" -ForegroundColor Red
Write-Host "  Warnings:       $($script:Warnings)" -ForegroundColor Yellow
Write-Host "  Pass Rate:      $passRate%" -ForegroundColor $(if ($passRate -ge 80) { "Green" } elseif ($passRate -ge 50) { "Yellow" } else { "Red" })

Write-Host ""
Write-Host ("=" * 70) -ForegroundColor $script:ColorHeader

# Detailed results
Write-Host ""
Write-Host "  Detailed Results:" -ForegroundColor White
Write-Host ""

$script:Results | Format-Table -Property Test, Status, Details -AutoSize

# Save report to file
$reportPath = "test-report-$(Get-Date -Format 'yyyy-MM-dd-HHmmss').txt"
$script:Results | Format-Table -Property Test, Status, Details -AutoSize | Out-File -FilePath $reportPath

Write-Host ""
Write-Host "  Report saved to: $reportPath" -ForegroundColor Cyan
Write-Host "  Completed: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host ""
Write-Host ("=" * 70) -ForegroundColor $script:ColorHeader

# Exit code
if ($script:FailedTests -gt 0) {
    exit 1
} else {
    exit 0
}