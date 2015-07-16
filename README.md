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

