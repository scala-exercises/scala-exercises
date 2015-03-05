$(function() {
    $.when(
        $('head').append('<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" type="text/css" />'),
        $.ready.promise()
    ).then(function(){
        $('#bottom').show();
        loadGitHubStats();
        loadStepIconAnimation();
        loadIconGeneralAnimation();
    });
});

var baseURL = location.protocol + '//' + location.host;

function loadStepIconAnimation() {
    $('.step-icon').each(function(index) {
        $(this).waypoint(function() {
            showAniming($(this), 'fadeInLeft');
        }, { offset: 'bottom-in-view', triggerOnce: true });
    });
}

function loadIconGeneralAnimation() {
    $('.icon-general').each(function(index) {
        var delay = index * 800;
        var item = $(this);
        item.waypoint(function() {
            setTimeout(function(){
                showAniming(item, 'fadeInDown');
            }, delay);
        }, { offset: 'bottom-in-view', triggerOnce: true });
    });
}

function showAniming(element, animation) {
    element.css('opacity','1.0').addClass('animated').addClass(animation);
    setTimeout(function(){
        element.removeClass('animatedss').removeClass(animation);
    }, 3000);
}

function moveToDiv(id) {
	$('html, body').animate({scrollTop:$("#"+id).offset().top}, 500);
	return false;
}

function shareSiteFacebook() {
    var title = '"Scala Exercises" offers hundreds of exercises covering the main concepts of Scala!';
    launchPopup('http://www.facebook.com/sharer/sharer.php?u='+baseURL+'&t='+title)
}

function shareSiteTwitter() {
    var text = '"Scala Exercises" offers hundreds of exercises covering the main concepts of Scala! '+baseURL;
    launchPopup('https://twitter.com/home?status='+text)
}

function shareSiteGoogle() {
    launchPopup('https://plus.google.com/share?url='+baseURL)
}

function launchPopup(url) {
    window.open(url, 'Social Share', 'height=320, width=640, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, directories=no, status=no');
}

function loadGitHubStats(){
        
 var gitHubAPI = "https://api.github.com/repos/47deg/scala-exercises?callback=?";
  $.getJSON( gitHubAPI).done(function( data ) {
    $('#eyes').text(data.data.watchers_count );    
    $('#stars').text(data.data.stargazers_count );    
    $('#forks').text(data.data.forks);
      setTimeout(function(){
          showAniming($('#gitHubLayer'), 'fadeInDown');
      }, 500);
  });
    
}