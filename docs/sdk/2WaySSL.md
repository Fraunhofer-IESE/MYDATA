# SSL Client Authentication For MYDATA-Component-Authentication

The HTTPS-communication between mydata-components is secured by two way-SSL. In common SSL, the client verifies the server's identity by a server-certificate. In contrary to this, using two-way-SSL means, that also the client has to let the server verify his identity. TO create the certificate, OpenSSL and the Java-keytool from Java-1.7 or higher has to be installed properly and to be in your Path.

## Java-Keystores

The common way in java to deal with keys is using a java-keystore. Usually we have 2 files using the keystore-format. One is is the truststore, containing all the trusted certificates of other communication instances and one keystore with our own certificates and corresponding private keys. Please use the password "ind2uce" for every store-file.

## The Certificate Authority (CA)

### Why Do We Need A Certificate Authority ?

During the SSL-handshake procedure, we will get the certificate of somebody else who wants to start communicating with us. Because everybody can easily create such a certificate, we need some more meaningful reasons for trusting. This is why, the certificate should be signed by an authority, we already know and trust, namely the CA (Certificate Authority).

### Why Do We Use An OWN CA ?
Inside our application, we want a little bit more. Of course it's not enough that an identity is evident. We want to reject any identity which is not part of our application. This is the reason why we create an own application-wide CA instead letting certificates signed by a public signing authority like e.g. "Verysign".

### Creating A CA

The following command creates a key-pair, we will use as CA.

```shell
openssl  req  -new -x509  -keyout  iese-ca-key.pem -out  iese-ca-root.pem -subj "/CN#fraunhofer-iese.de/OU#IESE/O#Fraunhofer-Institut/L#Kaiserslautern/ST#Rheinland-Pfalz/C#DE"
```

### Creating A Truststore

The following command imports the public key of the CA in the truststore.

```shell
keytool -import -trustcacerts  -keystore truststore -file iese-ca-root.pem  -alias theCARoot
```
Once the trusstore is created, it can be used as a trusstore all over the application for client and server, because it contains the CA's certificate, only. It just has to be write protected because it contains only public information.

## The Client-Keystore
### Create A Client Certificate

We start with creating the key-pair for the client. We use the keytool, so the private key and the public key is directly stored in keystore.
```shell
keytool -keystore clientkeystore.jks -genkey -dname "CN#ind2uce.client, OU#IESE, O#Fraunhofer-Institut, L#Kaiserslautern, ST#Rheinland-Pfalz, C#DE"  -noprompt -alias client -keyalg RSA
```
### The Certification-Request

Because we want our client certificate to be signed by our CA, we have to create a certification-request. This is needed for creating a signed certificate. The client key now is stored in a java-keystore, so we use the java-keytool for creating the request.
```shell
keytool -keystore clientkeystore.jks -certreq -alias client -keyalg rsa -file client.csr
```

`client.csr` is the name of the resulting certification-request.

### Signing The Certification-Request
We use openssl for signing. The result is the signed client-certificate `client.cer`:
```shell
openssl  x509  -req  -CA iese-ca-root.pem  -CAkey iese-ca-key.pem  -in client.csr -out client.cer -CAcreateserial
```
### Creating the client keystore
The client keystore must of cause contain the client-certificate. But it must also contain the certificate of the CA (and not the CA's private key), which has to be imported first:
```shell
keytool -import -trustcacerts  -keystore clientkeystore.jks -file iese-ca-root.pem  -alias theCARoot

keytool -import -keystore clientkeystore.jks -file client.cer -alias client
```
This procedure should be done for every component. For a client, the trusstore and the keystore has to placed in classpath and must have the filenames "clientkeystore.jks" and "trusstore.jks" and the password "ind2uce".

## The Server-Keystore

Creating the server-keystore is similar to creating the client-keystore. As a truststore the same file can be used, as it just contains the CA's public key. The following commands finally create a server-keystore:
```shell
keytool -keystore serverkeystore.jks -genkey  -dname "CN#ind2uce.server, OU#IESE, O#Fraunhofer-Institut, L#Kaiserslautern, ST#Rheinland-Pfalz, C#DE" -alias server -keyalg RSA

#Create a Server-Signing-Request:
keytool -keystore serverkeystore.jks -certreq -alias server -keyalg rsa -file server.csr

# Sign the server's key
openssl  x509  -req  -CA iese-ca-root.pem  -CAkey iese-ca-key.pem  -in server.csr -out server.cer -CAcreateserial

# Import root-CA into server-trusstore
keytool -import -trustcacerts -keystore serverkeystore.jks -file iese-ca-root.pem  -alias theCARoot

# Import server-cert into server-keystore
keytool -import -keystore serverkeystore.jks -file server.cer -alias server
```
## Using Swagger-UI
For using the Swagger-UI, your browser also has to use a private key and a certificate, signed by the CA. Depending on your Browser you will have to use DER or pkcs12. This who these
files can be created :
```shell
# For Browsers. You have also to import the iese-ca-root.pem as a signing instance
# For Chrome, Safari and Firefox

keytool -importkeystore -srckeystore clientkeystore.jks -destkeystore client.p12 -srcstoretype JKS -deststoretype PKCS12 -deststorepass ind2uce -srcalias client -destalias client

# For Safari and IE

openssl x509 -in client.cer -outform der -out client.der
```
