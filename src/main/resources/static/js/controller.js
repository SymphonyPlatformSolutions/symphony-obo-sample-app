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