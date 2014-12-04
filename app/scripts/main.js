$(function() {
    
    $.when(
        $('head').append('<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" type="text/css" />'),
        $.ready.promise()
    ).then(function(){
        loadStepIconAnimation();
        loadIconGeneralAnimation();
    });
    
});

var base_url = location.protocol + '//' + location.host;


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
    var title = '"Doing Scala" offers hundreds of exercises that covering main concepts of Scala!';
    launchPopup('http://www.facebook.com/sharer/sharer.php?u='+base_url+'&t='+title)
}

function shareSiteTwitter() {
    var text = '"Doing Scala" offers hundreds of exercises that covering main concepts of Scala! '+base_url;
    launchPopup('https://twitter.com/home?status='+text)
}

function shareSiteGoogle() {
    launchPopup('https://plus.google.com/share?url='+base_url)
}

function launchPopup(url) {
    window.open(url, 'Social Share', 'height=320, width=640, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, directories=no, status=no');
}