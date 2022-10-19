create self signed jks from pem
=======================

create cert and key

```bash
openssl req -newkey rsa:2048 -nodes -keyout tls.key -x509 -days 365 -out tls.crt
```

create one secret from two files

```bash
kubectl -n=demo create secret generic client.certificate --from-file=tls.key --from-file=tls.crt
```

```yaml

---
apiVersion: v1
kind: Pod
metadata:
  name: debug-jks-files
spec:
  
  initContainers:
  - name: init-myservice
    image: eclipse-temurin:18
    command: ['sh', '-c', 'cd /var/jks-volume && openssl pkcs12 -export -in /var/pem-volume/tls.crt -inkey /var/pem-volume/tls.key -out cert.p12 -name alias -passin pass: -passout pass: && keytool -importkeystore  -srckeystore cert.p12 -destkeystore cert.jks -srcstoretype PKCS12 -deststoretype jks -srcstorepass "" -deststorepass changeit -srcalias alias -destalias alias -srckeypass "" -destkeypass changeit && keytool -importkeystore -srckeystore cert.jks -destkeystore cert.jks -deststoretype pkcs12 -srcstorepass changeit -deststorepass changeit -srcalias alias -destalias alias -srckeypass changeit -destkeypass changeit']
    volumeMounts:
    - name: jks-volume
      mountPath: /var/jks-volume
    - name: pem-volume
      mountPath: /var/pem-volume
  
  containers:
  - command:
    - sleep
    - "3600"
    name: debug-jks-files
    image: eclipse-temurin:18
    volumeMounts:
    - name: jks-volume
      mountPath: /var/jks-volume
    - name: pem-volume
      mountPath: /var/pem-volume

  volumes:
  - name: pem-volume
    secret:
      secretName: client.certificate
  - name: jks-volume
    emptyDir: {}

```
