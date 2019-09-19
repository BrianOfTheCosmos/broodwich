# Introduction

## Warning

Broodwich is highly experimental, and may result in errors requiring application server and/or application restart to 
eliminate.

## Summary

Broodwich is an in-band, in-memory implant for Java post-exploitation. It installs a malicious servlet filter or Struts 
interceptor in a vulnerable web application, which can then surveil requests and take commands over the application's 
normal HTTP(S) communication channel.

## Goals
* **Steal sensitive data** by regex-matching parameter names and saving their values for later retrieval.
* **Evade network security monitoring** by hijacking the application's normal HTTP(S) communication channel rather than 
connecting to an external C2 server.
* **Evade endpoint monitoring** by not creating any new processes or files (unless directed to with a post-exploitation 
module)

## Terminology
* **Client**: the command-line interface used to generate and control implants
* **Dropper**: the Java code used to install the implant filter / interceptor in a particular application server / 
framework such as Tomcat, Jetty, or Struts
* **Payload**: the RCE payload which contains the compressed bytecode of the dropper and implant and runs the dropper
* **Module**: post-exploitation code that can be run through the implant, either built-in (pattern-matching functionality)
or dynamically loaded from Java bytecode

# Documentation

## Building
Broodwich requires JDK 11 to be built, though the droppers, payloads, and implants target Java 6 and higher. It references
sun.* classes, so AdoptOpenJDK users must use the HotSpot distribution rather than the OpenJ9 distribution.

Clone the repository, and then run `./gradlew client:build jar` within it. The client JAR file -- which also contains the 
dropper and payload class files -- will be saved to client/build/lib/broodwich.jar

## Tested platforms

Broodwich still needs extensive testing for platform support. So far, its droppers have been tested primarily against:
* Tomcat 8.5
* Jetty 9.4.12.v20180830
* Struts 2.5.10

Broodwich is built to be bytecode- and API-compatible back to Java 6, but has been tested primarily on Java 8.

## Usage

In-CLI documentation is forthcoming. In the meantime, here is the general usage.

### Generating a payload

`java -jar broodwich.jar payload <payload type> <dropper type> <path pattern> <password>`

#### Current payloads

* XMLDecoderPayload
* OGNLPayload
* JavascriptPayload

The XMLDecoder and OGNL payloads are both built upon the JavaScript payload. Many Java RCEs should be relatively easy to 
turn into Broodwich payloads by replacing calls to, e.g., `java.lang.Runtime.getRuntime().exec()` with calls to, e.g., 
new `javax.script.ScriptEngineManager().getEngineByName("JavaScript").eval()`.

Note that the OGNL payload -- for Struts advisory S2-045 -- is a "trampoline" that evaluates JavaScript in the request body,
like so: `curl -i -X POST --url http://localhost:8080/demo-struts2/index.action -H "Content-Type: ""$(cat /tmp/header.txt)" --data @/tmp/body.txt`.
The JavaScript needs to be generated separately from the OGNL.

#### Current droppers

* TomcatDropper
* JettyDropper
* StrutsDropper

#### Other options

The path pattern is used by filter-based droppers (TomcatDropper and TomcatDropper) to determine which requests the filter 
will affect. Consider leaving the original RCE location out of the path pattern to give yourself the opportunity to remove 
the filter should something go wrong (a formal "un-dropper" mechanism is planned but not yet developed). It has no effect 
on the Struts interceptor dropper.

The supplied password is required for subsequent communication with the implant. Its primary purpose is to impede network 
scanners looking for Broodwich-infected apps. Note that Broodwich does not encrypt or MAC its communications.

### Loading modules

No modules except for the parameter-sniffing modules are built into the implant, in order to keep the payloads slightly 
less than gargantuan. Additional modules are sent to the implant over HTTP before use. Each module is simply a Java class 
containing a method with the signature `public static String run(List<String> params)`.

`java -jar broodwich.jar loadmodule <module name> <app url> <password>`

### Running modules

`java -jar broodwich.jar runmodule <module name> <app url> <password> <parameters>`

#### Current modules and parameters

* `cmd <cmd> [cmd...]`
* `cat <path>` (output is Base64-encoded)
* `ls <path>`
* `rm <path>`
* `prop` (list system properties)
* `heapdump <path> [liveness]` (use with [even more] caution, dumping the heap will pause the target JVM and potentially use large 
amounts of disk space)
* `setpattern <regex>` (built-in) (save the values of request parameters with names matching the supplied regex)
* `flushmatches` (built-in) (print the values captured by the aforementioned and delete them from the implant's memory)

## Select known issues / planned improvements

* Client commands hang for a few seconds after completion, likely an issue with the way Broodwich is using picocli
* Hanging commands can hang the cmd module; may need to use a hard timeout rather than waitFor()
* Testing needed with additional app server, framework, and JRE versions, and real-world applications
* Additional modules needed, esp. copy and ssrf modules
* Additional RCE payloads needed
* In-CLI documentation needed
* "Un-droppers" needed
* Test / demo targets need to be cleaned up and/or moved to a separate repo
* And so on... when I said experimental, I meant it

## License

Copyright 2019 Brian D. Hysell, licensed under the MPL 2.0