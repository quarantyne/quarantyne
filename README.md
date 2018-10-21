# quarantyne [![Build Status](https://travis-ci.org/quarantyne/quarantyne.svg?branch=master)](https://travis-ci.org/quarantyne/quarantyne) 
Fast and unobtrusive web fraud detection and labelling.

__TL;DR__ Quarantyne is a reverse-proxy that looks for signs of fraudulent behavior in web traffic running through it. If detected, Quarantyne labels in real-time the fraud in the HTTP request headers, like `X-Quarantyne-Labels: FAS,HDR`, for use by downstream.

## Requirements
- Java 8
- Web traffic

## Installation and Execution
Quarantyne ships as a single 0-dependencies executable jar. Clone this repo and run the following commands

```bash
$ ./gradlew build
$ java -jar build/libs/quarantyne.jar

"2018-10-20T18:41:17.830-0700" [main] INFO com.quarantyne.proxy.Main - ==> quarantyne
"2018-10-20T18:41:17.833-0700" [main] INFO com.quarantyne.proxy.Main - ==> proxy   @ 127.0.0.1:8080
"2018-10-20T18:41:17.833-0700" [main] INFO com.quarantyne.proxy.Main - ==> remote  @ httpbin.org:80
"2018-10-20T18:41:17.834-0700" [main] INFO com.quarantyne.proxy.Main - ==> admin   @ http://127.0.0.1:3231
```

The default configuration starts the proxy on 127.0.0.1:8080 and proxies traffic to http://httpbin.org/. This is just an example. For latency reasons you will probably run Quarantyne inside your private network, a hop and a few milliseconds away from the backend you want to protect.

Send a few requests to http://127.0.0.1:8080/headers and you should see Quarantyne injecting
its headers if fraud is detected. Hint: try with curl, or from an AWS.

## Configuration
[in progress...]

## Features
[in progress...]

## Contributions
[in progress...]
