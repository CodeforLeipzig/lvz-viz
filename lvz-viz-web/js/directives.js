app.directive('myButtons', function(){
    // Runs during compile
    return {
         templateUrl: 'templates/dateRangeButtons.html'
    };
});
app.directive('myResultlist', function(){
    // Runs during compile
    return {
         templateUrl: 'templates/myResultlist.html'
    };
});