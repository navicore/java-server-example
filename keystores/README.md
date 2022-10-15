Create truststore for actor1
-----------------

generate a private key and self signed certificate for actor1

```bash
keytool -genkeypair -noprompt -alias self -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname "CN=actor1" -validity 365 -keypass password -keystore actor1_privatekey.jks -storepass password -storetype JKS
```

export self-signed cert

```bash
keytool -exportcert -noprompt -rfc -alias self -file actor1.crt -keystore actor1_privatekey.jks -storepass password -storetype JKS
```

trust self-signed cert

```bash
keytool -importcert -noprompt -alias actor1 -file actor1.crt -keypass password -keystore actor1_trust.jks -storepass password -storetype JKS
```

migrate to pks12

```bash
keytool -importkeystore -srckeystore actor1_privatekey.jks -destkeystore actor1_privatekey.jks -deststoretype pkcs12
```

Create truststore for actor2
-----------------

generate a private key and self signed certificate for actor2

```bash
keytool -genkeypair -noprompt -alias self -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname "CN=actor2" -validity 365 -keypass password -keystore actor2_privatekey.jks -storepass password -storetype JKS
```

export self-signed cert

```bash
keytool -exportcert -noprompt -rfc -alias self -file actor2.crt -keystore actor2_privatekey.jks -storepass password -storetype JKS
```

trust self-signed cert

```bash
keytool -importcert -noprompt -alias actor2 -file actor2.crt -keypass password -keystore actor2_trust.jks -storepass password -storetype JKS
```

migrate to pks12

```bash
keytool -importkeystore -srckeystore actor2_privatekey.jks -destkeystore actor2_privatekey.jks -deststoretype pkcs12
```

make actor1 and actor2 trust each other
------------------

actor1 should trust actor2

```bash
keytool -importcert -noprompt -alias actor2 -file actor2.crt -keypass password -keystore actor1_trust.jks -storepass password -storetype JKS
```

actor2 should trust actor1

```bash
keytool -importcert -noprompt -alias actor1 -file actor1.crt -keypass password -keystore actor2_trust.jks -storepass password -storetype JKS
```

You should now have two *trust* stores: actor1_trust.jks and actor2_trust.jks to deploy for your mTLS test.


to also support public cert authority...

```bash
keytool -importkeystore -noprompt -srckeystore /etc/ssl/certs/java/cacerts -destkeystore actor1_trust.jks -deststoretype JKS -srcstorepass changeit -deststorepass password
```
