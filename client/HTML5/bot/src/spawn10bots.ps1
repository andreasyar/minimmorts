param([string] $n = $(throw "Please specify count of bots!"))
for ($i = 0; $i -le $n; $i++) {
    C:\Users\Sorc\AppData\Local\Google\Chrome\Application\chrome.exe http://2.92.40.121/bot/client.html
}
# "$(pwd)\client.html"