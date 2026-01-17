# README

## Workflow

🔄 Application Flow
1. User uploads encrypted Signal database via SignalView (/signal)
2. App decrypts using SQLCipher with user-provided key 
3. Queries group chats and messages from decrypted database 
4. Parses Wordle scores from messages using regex 
5. Displays statistics and visualizations in SignalChatView (/signal/chat)
6. Users can compare multiple chats across timeframes (all-time, last 7/30 days, etc.)

## How to get your Key

Download signalbackup-tools_win.exe from https://github.com/bepaald/signalbackup-tools/releases/.
Put it literally anyway, open a terminal where it is and run the following command.

`signalbackup-tools_win.exe --showdesktopkey --ignorewal`

Enter the key that it returns in the text box where you uploaded your db.sqlite.

## Docker

build: `docker build -t signal-endpoint .`

run: `docker run -p 8080:8080 signal-endpoint`
