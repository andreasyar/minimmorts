param([string] $n = $(throw "Please specify count of bots!"))
for ($i = 0; $i -lt $n; $i++) {
    C:\Users\Sorc\AppData\Local\Google\Chrome\Application\chrome.exe "$(pwd)\client.html"
}
# 
# http://2.92.40.121/bot/client.html
