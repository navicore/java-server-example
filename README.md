# java-server-example

Hello World to play with mTLS and K8S

A minimal hello world like example to refamiliarize myself with Java keystore / truststore
procesing via keytool.

DO NOT USE for real work - this is a toy server based on the ancient Sun
HttpsServer class.

See the [keytool instructions](keystores/README.md) for the self-signed cert
generation.

See this [gist](https://gist.github.com/navicore/a307d848d9562435137a60d710179d50) for the k8s instructions for installing a keystore jks file into a pod's file system via k8s secrets.

```bash
mvn package
java -jar ./target/java-server-example-1.0-SNAPSHOT-jar-with-dependencies.jar
```
or

```bash
mvn compile exec:java
```

update your `/etc/hosts` file to make hosts `actor1` and `actor2` work on localhost
