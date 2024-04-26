# bbb-jmeter
Jmeter script to load test a BigBlueButton server by stressing the html5-client websocket, this can be useful to test some specific messages and compare performance between changes/versions made to the client to find bottlenecks.

Docker based with grafana and influxdb integration to visualize results:
![image](https://user-images.githubusercontent.com/2726293/133854607-2b409a34-b381-4f89-a112-ac4b4c2ad6a8.png)
![image](https://user-images.githubusercontent.com/2726293/133854692-fefe0366-f75e-4ca4-ab10-a3a9d1539e69.png)

## What does it do?
- The `test/bbb_23.jmx` script creates a meeting for each line in `test/meetings.csv` and join them with users according to the number of threads defined by `ThreadsOwner` and `ThreadsGuest`.

Then, each user/thread:
- Performs the initial default meteor websocket communication and subscribe to all collections;
- Sends a "Hello" message to chat and change the emojiStatus;
- Enter in a loop to send VoidConnection and a Chat Message for `PingLoopOwner` and `PingLoopGuest` times.

When the Guest/Viewer loop ends, they leave the meeting.  
When the Owner loop ends, they end the meeting.

![image](https://user-images.githubusercontent.com/2726293/133846846-159916bd-0620-4c9d-92da-76d0f2a4d1f6.png)

![image](https://user-images.githubusercontent.com/2726293/133843603-019a2371-4a9c-4f63-9c9d-79599df74f8e.png)

## Prerequisites
Docker and docker-compose
## Running
- `git clone https://github.com/mconf/bbb-jmeter.git && cd bbb-jmeter`
- Edit `.env` file:
  - Add the `hostname` and `salt` of the server you want to test against
  - Configure the threads and ping variables as desired
- Edit `test/meetings.csv` if you want to create more than one meeting (and increase `ThreadsOwner` accordingly)
  - format: `meetingName,meetingId,voiceBridge,moderatorUserNamePrefix,guestUserNamePrefix`
- Run `docker-compose up -d`

Open http://localhost:30000/ in your browswer to see live results while test is running, the jmeter logs are stored locally in `$LOG_DIR` folder, and metrics data in the `influx-grafana/influx-data`, so the historical data from previous test are kept and browseable in grafana.
 - default user/password: admin/admin

## Editing test script with Jmeter GUI locally
1. Download latest jmeter https://jmeter.apache.org/download_jmeter.cgi (requires Java 8)
1. You will also need the Webscockets Plugin:
   1. Download Jars: https://jmeter-plugins.org/get/ and https://bitbucket.org/pjtr/jmeter-websocket-samplers/downloads/ (need to extract)
   1. Copy the jars to the `lib/ext` folder inside jmeter
1. Open `jmeter.sh`|`jmeter.bat`

While editing, to make small tests and debug, disable `Backend Listener` and enable `View Results Tree/Table` to see the exact errors.

##

In your BigBlueButton server, make sure you remove the 3 connections per user.
Edit /usr/share/bigbluebutton/nginx/bbb-html5.nginx, then comment `limit_conn`, and then run `sudo systemctl reload nginx`.
https://github.com/bigbluebutton/bigbluebutton/pull/15977/files#diff-b5ecf26038a7030cd5dda45561b30819cefd6109ae8fc2abbbc0fdae36302fa0R7
If you don't do it, you'll start having errors on Open Connection.

## Credits

This work is based on scripts developed by the staff of RNP - Rede Nacional de Ensino e Pesquisa (https://www.rnp.br/en), the Brazilian NREN, in the context of stress testing Conferência Web (https://conferenciaweb.rnp.br/).

##
