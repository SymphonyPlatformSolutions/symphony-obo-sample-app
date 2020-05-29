# symphony-obo-sample-app
A sample OBO app + bot that calls list_user_streams on behalf of a user.

## Subscribe to 'extended-user-info' service in order to obtain user identity: (resources/js/controller.js)

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
  ## Add a corresponding Request Mapping to Perform App Authentication: (web/WebController)
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

## Add an IMListener to return Map of of user streams {"stream_name": "stream_id"}: (bot/IMListenerImpl)

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
