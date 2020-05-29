# symphony-obo-sample-app
A sample OBO app + bot that calls list_user_streams on behalf of a user.

## Prerequisites:
* JDK 1.8 + Maven3
* Bot user account + RSA Public/Private Key Pair
* IDE of your choice
* Application Permissions: https://developers.symphony.com/symphony-developer/docs/obo-overview#obo-app-permissions 
    * ACT_AS_USER and LIST_USER_STREAMS
* Enable application for designated user + bot: https://developers.symphony.com/extension/docs/application-management

## Subscribe to 'extended-user-info' service in order to obtain user identity: 

(resources/js/controller.js):

```
var oboApp = 'oboApp:controller';
var oboService = SYMPHONY.services.register(oboApp);

let appToken = undefined;
const appTokenPromise = fetch('https://localhost:4000/appToken')
.then(res => res.json())
.then(res => {
    appToken = res.token;
});

Promise.all([ SYMPHONY.remote.hello(), appTokenPromise ]).then(function(data) {
    SYMPHONY.application.register(
        { appId: 'oboApp', tokenA: appToken },
        [ 'entity', 'extended-user-info' ],
        [ oboApp ]
    )
    .then(function(response) {
        var userInfoService = SYMPHONY.services.subscribe('extended-user-info');
        var userId = undefined;

        userInfoService.getJwt().then(response => {
        var jwt = JSON.parse(window.atob(response.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
        userId = jwt.user.id;

     });
    }
  });
  ```
  ## Add a corresponding Request Mapping to Perform App Authentication: 
  
  (web/WebController.java)
  ```
  @RestController
  public class WebController {
    private static final Logger LOG = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/")
    public String home(){
        return "Hello World";
    }

    @GetMapping("/appToken")
    public Map<String, String> getAppToken(){
        SymExtensionAppRSAAuth appAuth = new SymExtensionAppRSAAuth(OboBot.getConfig());
        Map<String, String> map = new HashMap<>();
        map.put("token", appAuth.appAuthenticate().getAppToken());
        return map;
    }

}
```

## Add an IMListener to return Map of of user streams {"stream_name": "stream_id"}: 

(bot/IMListenerImpl.java)

```
public void onIMMessage(InboundMessage inboundMessage) {
        LOGGER.info(inboundMessage.getMessageText());
        OutboundMessage message;
        //List<String> userStreams = new ArrayList<String>();
        HashMap<String, String> streamInfo = new HashMap<String, String>();

        if (inboundMessage.getMessageText().equalsIgnoreCase("/streams")) {
            SymOBOUserRSAAuth userAuth = OboBot.getOboAuth().getUserAuth(inboundMessage.getUser().getUserId());
            SymOBOClient oboClient = SymOBOClient.initOBOClient(OboBot.getConfig(), userAuth);

            List<StreamListItem> streamsMap = oboClient.getStreamsClient().getUserStreams(Arrays.asList("IM", "ROOM"), false);
            for (StreamListItem item : streamsMap){
                if (item.getType().equals("ROOM")) {
                    streamInfo.put(item.getRoomAttributes().getName(), item.getId());
                }
            }
            LOGGER.info(streamInfo.toString());

        }
    }
```
## Sample Log Output: Type '/streams' in (1-1 IM) (Calling User -> OBO Bot):

```
{77=UC0fwg27RpuqQWl4-pIYHn___pO8Vah0dA, 88=ZKzsV0Tdx9BFxMXb5RsMbH___pO8UjGhdA, 78=ueQF0BUJLt-k6lY7b2Inbn___pPQ6I9ndA, sales-bot-demo-room=4RBWbCSEzY14B9F2RE8FXH___pGaF5k4dA, option_jpy/nok_69.57399284877=sojMwI1YtKp8iDjNRE54sH___pGQLfmudA, option_gbp/nzd_153.3509694672=pj4O5jh6AmyG_uTAQLUsKX___pGUMftAdA, option_cny/try_166.4922677676=1Aq3nRj2cpIi2WkS7exk2X___pGP-bEwdA, Innovate 2019 Hackathon=jPC-UJQZ7xhTLC_UjpOyZ3___pMn1NxFdA, option_sek/ils_88.94111329455=J5_NtZK_GMhJKIY6T3ZWKX___pGP-d9FdA, swap_brl/jpy_151.27258008029=y58xMDgCU7ZcF0rjh-zNMn___pGP-GwNdA, hack-test-room=0FujVaUBhAYYXRXqESu17X___pJEqN1kdA, Trade: $AMZN Amazon.com, Inc.=UIjPEgsgSV0r2WpB3WdHgX___pQiiLGOdA, swap_cop/dkk_40.90657112608=6z1FcMRb7gXOX-UKevicUX___pGP-ZX2dA, swap_usd/eur_14.08856955263=rPkUcgwhK5Ui1gSTIKdo_H___pGP-Z2OdA, atmentionroom=sOKpwRk_5_N838P10ATuFX___pNk9zJndA, Updating room=szER4pjkPrnydrPs-VO-BH___pJn7BPddA, option_nzd/sgd_64.36759183204=BSlrHPztPzvLLMar1c_qe3___pGP-adLdA, option_jpy/nok_72.40775271464=35mMOHtyhm3vukeVyfs_43___pGT9M0kdA}
```

