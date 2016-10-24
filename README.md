# Kony Paypal PoC Custom Services

This project defines the custom integration services for Kony Mobile Fabric to be used with the [Kony PayPal PoC project](https://github.com/mig82/kony-paypal-poc).

## Custom Java PayPal Connector

The PoC project depends on the definitions of the custom Java service defined in this project. You will have to compile, jar and import this project into Kony Studio, build it and publish PaypalServices.jar to your Kony Server's lib/userlibs folder. 

**Note:** That when attempting to test your custom Java service from Kony Studio you'll get an error with a stack trace that looks like this:

    response-code: 0    details: null
    at com.paypal.base.rest.OAuthTokenCredential.generateOAuthToken(OAuthTokenCredential.java:247)
    ...
    Caused by: com.paypal.base.exception.HttpErrorException: retry fails..  check log for more information
    at com.paypal.base.HttpConnection.executeWithStream(HttpConnection.java:197)
    ...
    Caused by: javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are     inappropriate)
    at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
    ...
    Caused by: javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are     inappropriate)
    at sun.security.ssl.Handshaker.activate(Handshaker.java:470)
    ...

Do not worry too much about this. I suspect it has to do with security settings in the Jetty server embedded in Kony Studio and [I've posted the issue to the forum](http://community.kony.com/developer/forum/oauthtokencredential-sslhandshakeexception). So -until the question to the forum has an answer- you won't be able to test your service from Studio, but once you publish to your Kony/Tomcat Server you'll see that there it works fine.

## PayPal Java SDK

The PaypalServices project has a dependency on [PayPal's Java SDK](https://github.com/paypal/PayPal-Java-SDK). You can get the latest sources of the SDK from Github and use Maven to build the jar. For convenience I've also attached the jar I built from version 1.4.1 -which is the latest available at the time. You'll also have to add this jar to your Kony Server's lib/userlibs folder.

## Google Gson

The PaypalServices project also uses [Gson](https://github.com/google/gson) to parse any stringified JSON objects posted by the client app into java objects of a class from PayPal's Java SDK.
