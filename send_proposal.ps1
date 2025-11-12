param(
  [Parameter(Mandatory=$true)][int]$port,
  [Parameter(Mandatory=$true)][string]$line
)

$client = New-Object Net.Sockets.TcpClient("127.0.0.1", $port)
$sw = New-Object IO.StreamWriter($client.GetStream()); $sw.AutoFlush = $true
$sw.WriteLine($line)
$sw.Dispose(); $client.Dispose()
