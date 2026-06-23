param(
    [string]$BaseUrl = "http://localhost:8080"
)

$pass = 0
$fail = 0
$results = @()

function Invoke-Api {
    param($Method, $Url, $Body)

    $req = [System.Net.WebRequest]::Create($Url)
    $req.Method = $Method
    $req.ContentType = "application/json"
    $req.Timeout = 30000

    if ($Body) {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($Body)
        $req.ContentLength = $bytes.Length
        $stream = $req.GetRequestStream()
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Close()
    }

    try {
        $resp = $req.GetResponse()
        $statusCode = [int]$resp.StatusCode
        $reader = [System.IO.StreamReader]::new($resp.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Close()
        $resp.Close()
        return @{ StatusCode = $statusCode; Content = $content }
    } catch [System.Net.WebException] {
        $ex = $_.Exception
        $resp = $ex.Response
        if ($resp) {
            $statusCode = [int]$resp.StatusCode
            $reader = [System.IO.StreamReader]::new($resp.GetResponseStream())
            $content = $reader.ReadToEnd()
            $reader.Close()
            $resp.Close()
            return @{ StatusCode = $statusCode; Content = $content }
        }
        return @{ StatusCode = 0; Content = $ex.Message }
    }
}

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Url,
        [string]$Body = $null,
        [int]$ExpectedStatus = 200,
        [ScriptBlock]$Validate = $null
    )

    $resp = Invoke-Api -Method $Method -Url $Url -Body $Body

    if ($resp.StatusCode -ne $ExpectedStatus) {
        $script:fail++
        $detail = if ($resp.Content) { "$Method $Url -> expected $ExpectedStatus, got $($resp.StatusCode) [$($resp.Content)]" } else { "$Method $Url -> expected $ExpectedStatus, got $($resp.StatusCode)" }
        return [PSCustomObject]@{ Status = "FAIL"; Name = $Name; Detail = $detail }
    }

    if ($Validate -and $resp.Content) {
        try {
            $obj = $resp.Content | ConvertFrom-Json
            $obj | ForEach-Object { & $Validate }
        } catch {
            $script:fail++
            return [PSCustomObject]@{ Status = "FAIL"; Name = $Name; Detail = "Validation error: $($_.Exception.Message)" }
        }
    }

    $script:pass++
    return [PSCustomObject]@{ Status = "PASS"; Name = $Name; Detail = "$Method $Url -> $($resp.StatusCode)" }
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  CMCC Smoke Test - $BaseUrl" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Actuator Health
$results += Test-Endpoint -Name "Actuator Health" -Url "$BaseUrl/actuator/health" -Validate {
    if ($_.status -ne "UP") { throw "Health status is not UP (got '$($_.status)')" }
}

# 2. Actuator Info
$results += Test-Endpoint -Name "Actuator Info" -Url "$BaseUrl/actuator/info" -Validate {
    if (-not $_.app) { throw "Missing app metadata" }
}

# 3. List Services
$global:services = $null
$results += Test-Endpoint -Name "List Services" -Url "$BaseUrl/api/services" -Validate {
    $global:services = $_
    if ($_.Count -eq 0) { throw "No services returned" }
}

# 4. Get Service by ID (first service from list)
if ($global:services -and $global:services.Count -gt 0) {
    $firstId = $global:services[0].id
    $results += Test-Endpoint -Name "Get Service by ID" -Url "$BaseUrl/api/services/$firstId" -Validate {
        if (-not $_.id -or $_.id -ne $firstId) { throw "Service ID mismatch" }
    }
}

# 5. Get Service by ID - Not Found
$results += Test-Endpoint -Name "Get Service 404" -Url "$BaseUrl/api/services/00000000-0000-0000-0000-000000000000" -ExpectedStatus 404 -Validate {
    if ($_.status -ne 404 -or $_.error -ne "Not Found") { throw "Wrong error format" }
}

# 6. Create Service - Valid
$global:createdId = $null
$results += Test-Endpoint -Name "Create Service" -Method "POST" -Url "$BaseUrl/api/services" -Body '{"name":"Smoke Test Service","url":"https://httpbin.org/status/200","category":"Testing"}' -ExpectedStatus 201 -Validate {
    $global:createdId = $_.id
    if ($_.status -ne "UNKNOWN") { throw "New service should be UNKNOWN (got '$($_.status)')" }
    if (-not $_.id) { throw "No ID returned" }
}

# 7. Create Service - Blank Name (Validation Error)
$results += Test-Endpoint -Name "Create Service - Blank Name" -Method "POST" -Url "$BaseUrl/api/services" -Body '{"name":"","url":"https://example.com/health","category":"Testing"}' -ExpectedStatus 400 -Validate {
    if ($_.status -ne 400 -or $_.error -ne "Bad Request") { throw "Wrong error format" }
    if ($_.message -notlike "*name*") { throw "Message should mention 'name'" }
}

# 8. Create Service - Invalid URL (Validation Error)
$results += Test-Endpoint -Name "Create Service - Invalid URL" -Method "POST" -Url "$BaseUrl/api/services" -Body '{"name":"Test","url":"not-a-url","category":"Testing"}' -ExpectedStatus 400 -Validate {
    if ($_.status -ne 400) { throw "Wrong error format" }
    if ($_.message -notlike "*url*") { throw "Message should mention 'url'" }
}

# 9. Update Service
if ($global:createdId) {
    $results += Test-Endpoint -Name "Update Service" -Method "PUT" -Url "$BaseUrl/api/services/$global:createdId" -Body '{"name":"Updated Smoke Test","url":"https://httpbin.org/status/200","category":"Updated"}' -Validate {
        if ($_.name -ne "Updated Smoke Test") { throw "Name not updated" }
    }
}

# 10. Force Re-check
if ($global:createdId) {
    $results += Test-Endpoint -Name "Force Re-check" -Method "POST" -Url "$BaseUrl/api/services/$global:createdId/check" -Validate {
        if ($_.serviceId -ne $global:createdId) { throw "Service ID mismatch" }
        if ($_.status -ne "UP" -and $_.status -ne "DOWN") { throw "Invalid status" }
        if (-not $_.latencyMs) { throw "Missing latency" }
    }
}

# 11. Force Re-check - Not Found
$results += Test-Endpoint -Name "Force Re-check 404" -Method "POST" -Url "$BaseUrl/api/services/00000000-0000-0000-0000-000000000000/check" -ExpectedStatus 404 -Validate {
    if ($_.status -ne 404) { throw "Wrong error format" }
}

# 12. Delete Service
if ($global:createdId) {
    $results += Test-Endpoint -Name "Delete Service" -Method "DELETE" -Url "$BaseUrl/api/services/$global:createdId" -ExpectedStatus 204
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  RESULTS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

foreach ($r in $results) {
    $color = if ($r.Status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "[$($r.Status)] $($r.Name)" -ForegroundColor $color
    Write-Host "       $($r.Detail)" -ForegroundColor Gray
}

Write-Host "`n------------------------------------------" -ForegroundColor Cyan
$totalColor = if ($fail -eq 0) { "Green" } else { "Red" }
Write-Host "  Passed: $pass  |  Failed: $fail  |  Total: $($pass+$fail)" -ForegroundColor $totalColor
Write-Host "------------------------------------------" -ForegroundColor Cyan

if ($fail -gt 0) {
    exit 1
}
