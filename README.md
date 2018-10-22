# quarantyne [![Build Status](https://travis-ci.org/quarantyne/quarantyne.svg?branch=master)](https://travis-ci.org/quarantyne/quarantyne) 
__Fast and unobtrusive web fraud detection and labelling.__

__TL;DR__ Quarantyne is a reverse-proxy that looks for signs of 
fraudulent behavior in web traffic. If detected, 
the fraudulent act is labelled in real-time via HTTP request headers injection, 
like `X-Quarantyne-Labels: FAS,HDR` for your HTTP service(s) running behind 
Quarantyne to incorporate, like not triggering analytics or flagging an order for second-review.

- [Requirements](#requirements)
- [Quick Run](#quick-run)
- [Configuration](#configuration)
- [Features](#features)
- [Metrics](#metrics)
- [License](#license)

## Requirements
- Java 8
- Web traffic

## Quick run
Quarantyne ships as a single 0-dependencies executable jar. 

```bash
$ java -jar build/libs/quarantyne.jar

"2018-10-20T18:41:17.830-0700" [main] INFO com.quarantyne.proxy.Main - ==> quarantyne
"2018-10-20T18:41:17.833-0700" [main] INFO com.quarantyne.proxy.Main - ==> proxy   @ 127.0.0.1:8080
"2018-10-20T18:41:17.833-0700" [main] INFO com.quarantyne.proxy.Main - ==> remote  @ httpbin.org:80
"2018-10-20T18:41:17.834-0700" [main] INFO com.quarantyne.proxy.Main - ==> admin   @ http://127.0.0.1:3231
```

You are all set! The default configuration runs Quarantyne on 
http://127.0.0.1:8080, and proxies traffic to http://httpbin.org.

Send a few requests to http://127.0.0.1:8080/headers via various means, and
if fraudulent behavior is detected, you should see Quarantyne headers
in the request received by the service fronted by Quarantyne. 
Hint: try with curl.

## Configuration
Quarantyne is configured using command line arguments like 
`java -jar quarantyne --remoteHost myservice.com --remotePort 8081`. 

The current arguments and their default values are:

- `--proxyHost [127.0.0.1]` The host/ip Quarantyne's HTTP server binds to.
- `--proxyPort [8080]` The TCP port Quarantyne listens to. Note: SSL termination is not supported for the time being. Default
- `--remoteHost [httpbin.org]` The host/ip of your HTTP service Quarantyne proxies traffic to.
- `--remotePort [80]` The TCP port of the your HTTP service Quarantyne proxies traffic to.
- `--remoteSsl [false]` When your HTTP service is SSL but not using port 443.
- `--adminPort [3231]` TCP port for the internal admin endpoint, where metrics and health are published.

## Features
### Fraud detection
Quarantyne inspects inbound HTTP requests in real-time to 
detect signs of fraud. Instead of incorporating these rulesets 
into your application and distributing it across services, Quarantyne 
externalizes this burden for you.

|Label | Definition | Behavior | Implemented |
 ----- | :-------: | :-----: | :---
__LBD__ | Large Body Data  | Overload target's form processor with POST/PUT request with body > 1MB | yes
__FAS__ | Fast Browsing | Request rate faster than regular human browsing | yes
__CPW__ | Compromised Password | Password used is known from previous data breach | yes
__DMX__ | Disposable Email | Email used is a disposable emails service | yes
__IPR__ | IP Address Rotation | Same visitor is rotating its IP addresses | no
__AHD__ | Suspicious Request Headers| Abnormal HTTP Request headers  | yes
__SUA__ | Suspicious User-Agent | User Agent not from a regular web browser | yes
__PCX__ | Public Cloud Execution | IP address belongs to a public cloud service like AWS or GCP | no
__IPD__ | IP/Country discrepancy | Country inferred from visitor IP is different from country field in submitted form | no

Quarantyne add extra HTTP headers to the request it proxies to your service. For example, an well-crafted HTTP request coming from AWS will bear the following headers:

- `X-Quarantyne-Labels: PCX`
- `X-Quarantyne-RequestId: 08a0e31a-f1a5-4660-9316-0fdf5d2a959d`

### Fraud redirection [in progress...]
Quarantyne can be configured to handle fraud by itself and stop requests 
from reaching your servers, saving you resources, metrics skew, 
and spam data scrubs. Instead of proxying the request to your server, 
Quarantyne can either display an internal error, or fetch and display 
a static page at the URL of your choice. 

### Metrics & health reporting
Quarantyne binds to an internal `adminPort`, where metrics (latencies, success rate...) as well as the health of the proxy are reported. 

### HTTPS + HTTP/2 Support [partial support]
Only HTTP requests proxied by Quarantyne to your service support SSL. 
See the `--ssl` command line argument. HTTPS support of traffic going 
to Quarantyne will be added in a future version.

HTTP/2 support will be added in a future version.

### 100% GDPR compliance
Quarantyne is offline software that runs inside your private network 
and does not communicate over the Internet with anyone to share data 
about your traffic, your business, or your users.

### Ops Friendly.
Single jar with 0 dependencies. Metrics are available on 
`[proxyHost]:[adminPort]/metrics`. Service health is available 
on `[proxyHost]:[adminPort]/health`

## License 
[Apache 2](https://github.com/quarantyne/quarantyne/blob/master/LICENSE)

