enter `localhost` for 'name' - this will become the CN

```bash
keytool -noprompt -validity 365 -dname "CN=localhost" -genkey -alias server-alias -keyalg RSA -keypass changeit -storepass changeit -keystore keystore.jks
```

```bash
keytool -export -alias server-alias -storepass changeit -file server.cer -keystore keystore.jks
```

```bash
keytool -import -v -trustcacerts -alias server-alias -file server.cer -keystore cacerts.jks -keypass changeit -storepass changeit
```

or 
---------------------------------------------------

enter `actor1` for 'name' - this will become the CN


create actor1 files

```bash
keytool -noprompt -dname "CN=actor1" -validity 365 -genkey -alias actor1 -keyalg RSA -keypass changeit -storepass changeit -keystore actor1_keystore.jks

keytool -export -alias actor1 -storepass changeit -file actor1_server.cer -keystore actor1_keystore.jks

keytool -import -v -trustcacerts -alias actor1 -file actor1_server.cer -keystore actor1_cacerts.jks -keypass changeit -storepass changeit
```

create actor2 files

```bash
keytool -noprompt -dname "CN=actor2" -validity 365 -genkey -alias actor2 -keyalg RSA -keypass changeit -storepass changeit -keystore actor2_keystore.jks

keytool -export -alias actor2 -storepass changeit -file actor2_server.cer -keystore actor2_keystore.jks

keytool -import -v -trustcacerts -alias actor2 -file actor2_server.cer -keystore actor2_cacerts.jks -keypass changeit -storepass changeit
```

make actors trust each other

```bash
keytool -importcert -v -trustcacerts -alias actor2 -file actor2_cacerts.jks -keystore actor1_keystore.jks -keypass changeit -storepass changeit

keytool -importcert -v -trustcacerts -alias actor1 -file actor1_cacerts.jks -keystore actor2_keystore.jks -keypass changeit -storepass changeit
```


