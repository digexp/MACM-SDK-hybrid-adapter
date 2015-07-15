# MACM-SDK-hybrid-adapter

**Accessing Java adapter from client**

* Hybrid (JavaScript) client 

```JavaScript
var request = new WLResourceRequest("adapters/CaasHybrid/items?path=Offer", WLResourceRequest.GET);

request.send().then(
function(response) {
alert(JSON.stringify(response));
},
function(error) {
alert(JSON.stringify(error));
}
);
```

* Native Android client 

```Java
WLResourceRequest req = new WLResourceRequest(new URI("adapters/CaasHybrid/items?path=Offer"), WLResourceRequest.GET);
req.send(new WLResponseListener(){
@Override
public void onSuccess(WLResponse response) {
// handle success

}
@Override
public void onFailure(WLFailResponse response) {
// handle failure
}});
```


* Native iOS client

```Objective-C
NSString static *const RESOURCE_URL = @"adapters/CaasHybrid/items?path=Offer";
WLResourceRequest *request = [WLResourceRequest requestWithURL:[NSURL URLWithString:RESOURCE_URL] method:WLHttpMethodGet];
[request sendWithCompletionHandler:^(WLResponse *response, NSError *error) {
NSString *httpStatus = [NSString stringWithFormat:@"%d", [response status]];
self.httpStatusTextField.text = httpStatus;
if (error != nil) {
[self updateView:[error description]];
} else {
[self updateView:[response responseText]];
}
}];
```


More details: http://www-01.ibm.com/support/knowledgecenter/SSHS8R_7.0.0/com.ibm.worklight.dev.doc/devref/c_adapters_endpoint.html?lang=pt-br