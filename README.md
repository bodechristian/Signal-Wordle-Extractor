# README

## How to get your Key

Download signalbackup-tools_win.exe from https://github.com/bepaald/signalbackup-tools/releases/.
Put it literally anyway, open a terminal where it is and run the following command.

`signalbackup-tools_win.exe --showdesktopkey --ignorewal`

Enter the key that it returns in the text box where you uploaded your db.sqlite.

## Docker

build: `docker build -t signal-endpoint .`

run: `docker run -p 8080:8080 signal-endpoint`