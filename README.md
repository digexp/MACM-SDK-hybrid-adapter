# MACM SDK hybrid adapter 

The MACM adapter is an IBM MobileFirst Platform Foundation 7.0 application to retrieve content from a MACM server.

 

### Table Of Contents

1. [Installation](#installation)
    1. [Configuration](#configuration)
    2. [Adapter](#dapter)
    3. [Sample app](#sample-app)
2. [Getting started](#getting-started)
    1. [Accessing the adapter from a mobile client](#accessing-the-adapter-from-a-mobile-client)
        1. [Hybrid (JavaScript) client](#hybrid-javascript-client)
        2. [Native Android client](#native-android-client)
        3. [Native iOS client](#native-ios-client)
    2. [Retrieving Content](#retrieving-content)
        1. [List of content items by path](#querying-a-list-of-content-items-by-path)
        2. [List of content items by id](#querying-a-list-of-content-items-by-id)
        3. [Single content item by id](#querying-a-single-content-item-by-id)
        4. [Single content item by path](#querying-a-single-content-item-by-path)
        5. [Querying an asset (image) by its url](#querying-an-asset-image-by-its-url)
3. [Miscellaneous](#miscellaneous)
	1. [HTTPS connections](#allowing-certificates-with-https-connections)
		1. [Add Certificate on Java KeyStore](add-certificate-on-java-keystore)
		2. [Turn off certificate validation in HTTPS connections](#turn-off-certificates-with-https-connections)
		
## Installation

### Configuration

To work with your MACM instance, you must use the worklight.properties file to specify the configuration parameters (Credentials and MACM Server URL) needed to access to the MACM
application server. This file is in the server/conf folder in your MFP project. To configure the URL of a service MACM, you need to create a properties named **ibm.macm.serverurl**. This property should contains the MACM
instance URL (Example: https://mymacm.myserver:100039). You also need to specify credentials throughout two new (custom) properties, **ibm.macm.username** and **ibm.macm.password**.

```
###############################################################
# MACM CONFIGURATION
###############################################################
ibm.macm.username=wpsadmin
ibm.macm.password=wpsadmin
ibm.macm.serverurl=https://mymacm.myserver:100039
```

### Adapter

The MACM's adapter (CaaS.adapter) must be deployed on the MFP server and this can be done from the MobileFirst Operations Console.

## Getting started

### Accessing the adapter from a mobile client
#### Hybrid (JavaScript) client

```javascript
/*Constructs a new resource request with the specified URL,
using the specified HTTP method.*/
var request = new WLResourceRequest("adapters/CaaS/items", WLResourceRequest.GET);
//Sets the values of the given query parameter name to the given value.
request.setQueryParameter("type", "Offer");
request.setQueryParameter("lib", "MACM Default Application");
request.setQueryParameter("element", "price,image,summary");
//Send this resource request asynchronously
request.send().then(
function(response) {
// success flow, the result can be found in response.responseJSON
//Print response's payload
alert(JSON.stringify(response));
// Show details of the first item
alert(JSON.parse(response.responseJSON).items[0].title);
alert(JSON.parse(response.responseJSON).items[0].summary);
alert(JSON.parse(response.responseJSON).items[0].properties.summary);
alert(JSON.parse(response.responseJSON).items[0].properties.image);
alert(JSON.parse(response.responseJSON).items[0].properties.price);
},
function(error) {
// failure flow
// the error code and description can be found in error.errorCode and error.errorMsg fields alert(JSON.stringify(error));
}
);
```

#### Native Android client

```java

WLResourceRequest req = new WLResourceRequest(new URI("adapters/CaaS/items"),
WLResourceRequest.GET);
req.setQueryParameter("type", "Offer");
req.setQueryParameter("lib", "MACM Default Application");
req.setQueryParameter("element", "price,image,summary");
req.send(new WLResponseListener(){
@Override
public void onSuccess(WLResponse response) {
// handle success
//Display the original response text from the server.
Toast.makeText(getApplicationContext(), response.getResponseText(),
Toast.LENGTH_LONG).show();
}
@Override
public void onFailure(WLFailResponse response) {
// handle failure
}});
```


#### Native iOS client

```objective-c
//Define the URI of the resource.
NSString static *const RESOURCE_URL = @"adapters/CaaS/items";
//Create a WLResourceRequest object with a GET HTTP method
WLResourceRequest *request =
[WLResourceRequest requestWithURL:[NSURL URLWithString:RESOURCE_URL]
method:WLHttpMethodGet];
//Add the required parameters.
[request setQueryParameterValue:@"type" forName:@"Offer"];
[request setQueryParameterValue:@"lib" forName:@"MACM Default Application"];
[request setQueryParameterValue:@"element" forName:@"price,image,summary"];
//Trigger the request with a call to the sendWithCompletionHandler method.
//Specify a completionHandler instance.
[request sendWithCompletionHandler:^(WLResponse *response, NSError *error) {
NSString *httpStatus = [NSString stringWithFormat:@"%d", [response status]];
NSString* resultText;
self.httpStatusTextField.text = httpStatus;
if (error != nil) {
resultText = @"Invocation failure.";
resultText = [resultText stringByAppendingString: error.description];
} else {
resultText = @"Invocation success.";
resultText = [resultText stringByAppendingString:response.responseText];
}
[self updateView:resultText];
}];
```


### Retrieving content
#### Querying a list of content items by path

```javascript
/*Constructs a new resource request with the specified URL,
using the specified HTTP method.*/
var request = new WLResourceRequest("adapters/CaaS/items", WLResourceRequest.GET);
//Sets the values of the given query parameter name to the given value.
request.setQueryParameter("type", "Offer");
request.setQueryParameter("lib", "MACM Default Application");
request.setQueryParameter("element", "price,image,summary");
//Send this resource request asynchronously
request.send().then(
function(response) {
// success flow, the result can be found in response.responseJSON
//Print response's payload
alert(JSON.stringify(response));
// Show details of the first item
alert(JSON.parse(response.responseJSON).items[0].title);
alert(JSON.parse(response.responseJSON).items[0].summary);
alert(JSON.parse(response.responseJSON).items[0].properties.summary);
alert(JSON.parse(response.responseJSON).items[0].properties.image);
alert(JSON.parse(response.responseJSON).items[0].properties.price);
},
function(error) {
// failure flow
// the error code and description can be found in error.errorCode and error.errorMsg fields alert(JSON.stringify(error));
}
);
```
#### Querying a list of content items by id

```javascript
var request = new WLResourceRequest(adapters/CaaS/items", WLResourceRequest.GET);
request.setQueryParameter("oid", id);
request.send().then(
function(response) {
alert(JSON.stringify(response));
var itemsList = JSON.parse(response.responseJSON);
},
function(error) {
alert(JSON.stringify(error));
}
);
```

#### Querying a single content item by id

```javascript
var request = new WLResourceRequest(adapters/CaaS/mypoc/item", WLResourceRequest.GET);
request.setQueryParameter("oid", id);
request.send().then(
function(response) {
var offer = JSON.parse(response.responseText);

// Display the offer data in the response.
alert(offer.item[0].properties.Body);
alert(offer.item[0].keywords);
alert(offer.item[0].lastmodifieddate);
alert(offer.item[0].properties.Price);

},
function(error) {
alert(JSON.stringify(error));
}
);
```


### Querying an asset (image) by its url

```
GET adapters/CaaS/asset?assetURL=<image-Url>;

https://mymacm.myserver:100039/DXBanking/adapters/CaaS/asset?assetURL=/wps/wcm/myconnect/cf78b16b5/auto.jpg?MOD=AJPERES
```

## Miscellaneous

### Allowing certificates with HTTPS connections

By default, Java adapters only allows trusted certificates whose certificate authority is in its trusted list.
If the server's certificate chain has not previously been installed in the truststore before accessing an HTTPS URL, an exception is raised. 

#### Add Certificate on Java KeyStore

You should update the CACERT file in your **JRE_HOME/lib/security** directory :

1. Hit the URL in your browser, retrieve the certificate in the browser's option and then export it.
2. Go to your **JRE_HOME/bin** or **JDKxx/JRE/bin** and execute following keytool command to insert certificate into trusted keystore.

```
keytool -keystore ..\lib\security\cacerts -import -alias yourSSLServerName -file .\relative-path-to-cert-file\yourSSLServerName.crt
```

3. Verify that the certificate was added to the truststore.

```
keytool -list -keystore ..\lib\security\cacerts
```

*default password of keystore is* "__Changeit__".

#### Turn off certificates with https connections

If you want to turn off certificate validation and have access to an HTTPS URL without having the certificate installed in the truststore, you need to override the default SSL manager and the default SSL hostname verifier.
This can be done by adding the following code before to open the connection stream:

```java
// Override SSL Trust manager without certificate chains validation
TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
    public X509Certificate[] getAcceptedIssuers(){return null;}
    public void checkClientTrusted(X509Certificate[] certs, String authType){}
    public void checkServerTrusted(X509Certificate[] certs, String authType){}
}};

// Initializes this context with all-trusting host verifier.
try {
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
} catch (Exception e) {;}
...
```

**_Warning: In order to ease the adapter use, the certificate validation has been disabled but you have to be aware of what using this workaround means. The drawbacks switching off the certificate validation and host verification for SSL implied that you will be not preventing man in the middle attacks or not be sure that you are connected to the host you think you are._**
