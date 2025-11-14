param(
    [Parameter(Mandatory = $true)]
    [int]$port,

    [Parameter(Mandatory = $true)]
    [string]$line
)

$client = New-Object System.Net.Sockets.TcpClient("localhost", $port)
$stream = $client.GetStream()
$writer = New-Object System.IO.StreamWriter($stream)
$writer.AutoFlush = $true

$writer.WriteLine($line)

$writer.Dispose()
$stream.Dispose()
$client.Close()
