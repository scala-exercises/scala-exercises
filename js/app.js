;(function(){
	activamenu();
	
	
    $.when(
		$.getScript('https://cdn.firebase.com/js/client/1.0.19/firebase.js'),
		$.getScript('http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min.js'),
		$.getScript('http://yandex.st/highlightjs/8.0/highlight.min.js'),
        $.ready.promise()
    ).then(function(){
		initKoans();
        
		
		activaCarousel();
        // colorea();
		// buscaReplace();
    });
	
	


})(jQuery)


var urlFB = 'https://doingscala.firebaseio.com/'
var koansURL = urlFB+'koans';
var koansRef = false;
var koans = false
//Koans

function initKoans() {
	koansRef = new Firebase(koansURL);
	getKoans();
}

function getKoans() {
	koansRef.on('value', function (snapshot) {
		koans = snapshot.val();
		drawKoans();
	}, function (errorObject) {
	  console.log('The read failed: ' + errorObject.code);
	});
}

function drawKoans() {
	$.each(koans, function(index, koan) {
		if(index){
			drawKoan(index, koan);
		}
	});
	
	//buscaReplace();
	//colorea();
}

function drawKoan(index, koan) {
	var wrapper = $('#carousel-koans>.carousel-inner');
	var item = $('<div></div>').attr({'class':'item', 'data-index':index}); wrapper.append(item);
	var container = $('<div></div>').attr({'class':'container'}); item.append(container);
	var h2 = $('<h2></h2>').text(koan.title); container.append(h2);
	var content = $('<div></div>').attr({'class':'content'}); container.append(content);
	
	if(koan.modules){
		$.each(koan.modules, function(index_module, module) {
			drawModule(content, index_module, module);
			
		});
	}
	
	
  	// <div class="item active">
// 		<div class="container">
//     		<h2>Presentation</h2>
// 			<p>Parrafo</p>
// 			<pre><code>Codigo __</code></pre>
// 		</div>
//     </div>

}

function drawModule(content, index_module, module) {
	var module_wrapper = $('<div></div>').attr({'class':'module'}); content.append(module_wrapper);
	if(module.preparagraph){
		var preparapraph = $('<div></div>').attr({'class':'preparapraph'}).html(module.preparagraph); module_wrapper.append(preparapraph);
	}
	var pre = $('<pre></pre>'); module_wrapper.append(pre);
	var code = $('<code></code>').attr({'data-index':index_module}).html(module.code); pre.append(code);
	if(module.postparagraph){
		var postparagraph = $('<div></div>').attr({'class':'postparagraph'}).html(module.postparagraph); module_wrapper.append(postparagraph);
	}
	
}


// Sintaxis
function colorea() {
	hljs.initHighlightingOnLoad();	
	$('pre code').each(function(i, block) {
		hljs.highlightBlock(block);
	});
}

// Menu
function activamenu() {
    $("#menu-close").click(function(e) {
        e.preventDefault();
        $("#sidebar-wrapper").toggleClass("active");
    });
	
    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#sidebar-wrapper").toggleClass("active");
    });
}

// Carousel
function activaCarousel() {
	$('.carousel').each(function(){
        $(this).carousel({
            interval: false
        });
    });
		
	$('.carousel').on('slide.bs.carousel', function () {
		moveToDiv('koans');
	});
	
	$('.carousel').on('slid.bs.carousel', function () {
		var active = $(this).find('.item.active');

		active.find('pre code').each(function(i, block) {
            buscaReplace(block);
            activaInputs(block);
		});
		
	})
}



function buscaReplace(block) {
    console.log('entro aqui');
    var code = $(block);
    var module_index=code.attr('data-index');
    var koan_index = code.closest('.item').attr('data-index');
    var inputs = code.find('input');
    if(inputs.length>0){
        // console.log('ya tiene inputs');
    }
    else{
	    var texto = code.text();
		var reemplazado = texto.replace(/\__/g, '<input data-koan-index="'+koan_index+'" data-module-index="'+module_index+'" type="text" value="" />');
	    code.html(reemplazado);	
        hljs.highlightBlock(block);	
    }
	
}

function activaInputs(block) {
    var code = $(block);
    var inputs = code.find('input');
    if(inputs.length>0){
		inputs.each(function(i) {
            var input = $(this);
            input.attr('data-index',i);
            input.unbind();
            input.off();
        	input.on({
        		change: function() {
        			studySolution(this);
        	  	}, keyup: function() {
        	    	studySolution(this);
        	  	}, blur: function() {
        	    	studySolution(this);
        	  	}, focus: function() {
        	    	studySolution(this);
        	  	}
        	});
			console.log('activado');
		});
    }
}

function studySolution(element) {
    var input = $(element);
    var answer = input.val();
    var index = input.attr('data-index');
    var koan_index = input.attr('data-koan-index');
    var module_index = input.attr('data-module-index');
    var module = koans[koan_index].modules[module_index];
    if(module.solutions){
        var solution = module.solutions[index];
        if(solution==answer) input.addClass('success');
        else input.removeClass('success');
    }

}

// Utils

function moveToDiv(id) {
	$('html, body').animate({scrollTop:$("#"+id).offset().top}, 500);
	return false;
}