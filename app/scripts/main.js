$(function() {
    
    loadStepIconAnimation();
    loadIconGeneralAnimation();
});


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
