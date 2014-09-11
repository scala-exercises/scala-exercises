$(function() {
    
    $.when(
		$.getScript('http://yandex.st/highlightjs/8.0/highlight.min.js'),
        $.ready.promise()
    ).then(function(){
		
        loadNav();
        init();
        // colorea();
		// buscaReplace();
    });
});


var koans_groups = ["Asserts", "Val and Var", "Classes", "Options", "Objects", "Tuples", "Higher Order Functions", "Lists", "Maps", "Sets", "Formatting", "Pattern Matching", "Case Classes", "Range", "Partially Applied Functions", "Partial Functions", "Implicits", "Traits", "For Expressions", "Infix Prefix and Postfix Operators", "Infix Types", "Mutable Maps", "Mutable Sets", "Sequences and Arrays", "Iterables", "Traversables", "Named and Default Arguments", "Manifests", "Preconditions", "Extractors", "ByName Parameter", "Repeated  Parameters", "Parent Classes", "Empty Values", "Type Signatures", "Uniform Access Principle", "Literal Booleans", "Literal Numbers", "Literal Strings", "Type Variance", "Enumerations", "Constructors"];
var koans = false;





// HASH

$(window).on('hashchange', function() {
    init();
});


function loadNav() {
    var count = koans_groups.length;
    $.each(koans_groups, function(index, kg) {
        drawNavItem(index, kg);
    });
    $('#nav').tooltip({ selector: '.item' });
}


function routes () {
    var hash = window.location.hash.substring(1);
    switch (hash) {
        case '1': case '2': case '4': case '5':     chageView(function(){ cargaVistaCandidato(); }); break;
        default:                                    chageView(function(){ cargaVistaPortada(); });
    }
}

function init() {
    
}

// window.location.hash = tb;



// Nav
function drawNavItem(index, kg) {
    console.log(kg);
    var item = $('<div></div>').attr({'class':'item', 'data-toggle':'tooltip', 'data-placement':'bottom', 'title':kg}).text(index+1); $('#nav').append(item);
    if(index%6==0) item.addClass('pending');
    if(index%13==0) item.addClass('completed');
    if(index==16) item.addClass('current');
}


//Koans


function drawKoans() {
	$.each(koans, function(index, koan) {
		if(index){
			drawKoan(index, koan);
		}
	});
	
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