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
var koans_groups_id =[];
var koan = false;
var first = getId(koans_groups[0]);
var hash = false;



// HASH

$(window).on('hashchange', function() {
    console.log('Hash cambiado');
    init();
});


function loadNav() {
    var count = koans_groups.length;
    $.each(koans_groups, function(index, kg) {
        drawNavItem(index, kg);
    });
    $('#nav').tooltip({ selector: '.item' });
}



function init() {
    if(restoreNav()){
        writeCurrent(hash);
        readKoanJSON();
    }
    
}

// window.location.hash = tb;



// Nav
function drawNavItem(index, kg) {
    var item = $('<div></div>').attr({'class':'item', 'data-id':getId(kg), 'data-toggle':'tooltip', 'data-placement':'bottom', 'title':kg}).text(index+1); $('#nav').append(item);
    item.click(function(){
       window.location.hash = $(this).attr('data-id');
    });
}

function restoreNav() {
    var dev = false;
    var items = $('#nav .item');
    items.attr('class','item');
    var newhash = window.location.hash.substring(1);
    if(newhash.length==0){
        setInitialKoan();
    }
    else{
        hash = newhash;
        var item = $('#nav .item[data-id="'+hash+'"]');
        item.addClass('current');
        updateCompletes();
        dev = true;
    }
    return dev;
    
}

function setInitialKoan() {
    var hash = false;
    var current = readCurrent();
    if (current) hash = current;
    else hash = first;
    window.location.hash = hash;
    return false;
}

function updateCompletes() {
    $.each(koans_groups, function(index, kg) {
        var id = getId(kg);
        var navItem = $('#nav .item[data-id="'+id+'"]');
        if(isCompleteKoan(id)) navItem.addClass('completed');
        else navItem.removeClass('completed');
    });
}

//Koans

function readKoanJSON() {
    console.log('entro en readKoanJSON: '+hash);
    runReadingMode();
    var url = "json/"+hash+".json";
    
    
    $.getJSON(url, function(data) {
        hideReadingMode();
        koan = data;
        drawKoan();
        
    })
    .error(function (xhr, ajaxOptions, thrownError){
        if(xhr.status==404) {
            
            console.log("JSON not found");
        }
    });
    
}

function drawKoan() {
    console.log('entro en drawKoan: ');
	var wrapper = $('#content');
    wrapper.empty();
	var item = $('<div></div>').attr({'class':'item'}); wrapper.append(item);
	var container = $('<div></div>').attr({'class':'container'}); item.append(container);
	var h2 = $('<h2></h2>').text(koan.title); container.append(h2);
	var content = $('<div></div>').attr({'class':'content'}); container.append(content);
	
	if(koan.modules){
		$.each(koan.modules, function(index_module, module) {
			drawModule(content, index_module, module);
			
		});
	}
    colorea();
	searchInputs();
    
    setTimeout(function(){
      activeInputs();
    }, 500);
    
    console.log('FIN');
    //reloadComments();
}

function drawModule(content, index_module, module) {
	var module_wrapper = $('<div></div>').attr({'class':'module'}); content.append(module_wrapper);
	if(module.preparagraph){
		var preparapraph = $('<div></div>').attr({'class':'preparapraph'}).html(marked(module.preparagraph)); module_wrapper.append(preparapraph);
	}
	var pre = $('<pre></pre>'); module_wrapper.append(pre);
	var code = $('<code></code>').attr({'data-index':index_module}).html(module.code); pre.append(code);
	if(module.postparagraph){
		var postparagraph = $('<div></div>').attr({'class':'postparagraph'}).html(marked(module.postparagraph)); module_wrapper.append(postparagraph);
	}
	

}


// Sintaxis
function colorea() {
    console.log('entro en colorea: ');
	hljs.initHighlightingOnLoad();	
	$('pre code').each(function(i, block) {
        $(block).addClass('ruby');
		hljs.highlightBlock(block);
	});
}

function searchInputs() {
    console.log('entro en searchInputs: ');
	$('#content pre code').each(function(i, block) {
		
        var code = $(block);
        var module_index=code.attr('data-index');
	    var texto = code.text();
		var reemplazado = texto.replace(/\__/g, '<input data-module-index="'+module_index+'" type="text" value="" />');
	    code.html(reemplazado);	
        hljs.highlightBlock(block);
        
	});
	
}

function activeInputs() {
    console.log('entro en activeInputs: ');
    
    $('#content .module').each(function(module_index) {
        var module = $(this);
        var codes = module.find('pre code');
        module.find('pre code').each(function(code_index) {
            var code = $(this);
            
            
            var pre = code.parent();
            var inputs = code.find('input');
            if(inputs.length>0){
                pre.addClass('koan');
            	inputs.each(function(input_index) {
                    var input = $(this);
                    input.attr('data-index',input_index);
                    var moduleIndex = input.attr('data-module-index');
                    var inputSize = calculeSize(moduleIndex, input_index);
                    // input.width(inputSize);
                    input.animate({width: inputSize+"px"}, 300 );
                
                    var answer = readSolution(hash,moduleIndex,input_index);
                    if(answer.length>0){
                        input.val(answer);
                    }

                    studySolution(input.get(0));

                    input.unbind();
                    input.off();
                    input.on({
                        blur: function() {
                            studySolution(this);
                        },
                        keyup: function() {
                            studySolution(this);
                        }
                    });

            	});
                console.log('Salgo ');
            }
            else{
                console.log('CODE sin input');
            }
            console.log('Salgo de un CODE');
            
            
            
            
            
        });
        
    });
    
    
    console.log('preFIN');
    
}

function calculeSize(moduleIndex, index) {
    var size = 0;
    var module = koan.modules[moduleIndex];
    if(module.solutions){
        var solution = module.solutions[index];
        size = 6+solution.length * 12;
    }
    return size;
}


function studySolution(element) {
    var input = $(element);
    var answer = input.val();
    var index = input.attr('data-index');
    var module_index = input.attr('data-module-index');
    var module = koan.modules[module_index];
    if(module.solutions){
        var solution = module.solutions[index];
        writeSolution(hash,module_index,index,answer);
        if(solution==answer){
            input.addClass('success');
        }
        else{
            input.removeClass('success');
        }
        studyModule(input);
    }
}

function studyModule(input) {
    var precode = input.closest('pre.koan');
    var inputs = precode.find('input');
    var count = 0
    if(inputs.length>0){
        inputs.each(function(index) {
            var input = $(this);
            if(input.hasClass('success')) count++;
        });
        if(count<inputs.length) precode.removeClass('success');
        else precode.addClass('success');
    }
    else precode.removeClass('success');
    studyKoanStatus();
}

function studyKoanStatus() {
    var precodes = $('pre.koan');
    var count = 0;
    if(precodes.length>0){
        precodes.each(function(index) {
            var precode = $(this);
            if(precode.hasClass('success')) count++;
        });
        if(count<precodes.length) removeKoanComplete(hash);
        else writeKoanComplete(hash);
    }
    updateCompletes();
}

// Local Storage

function writeCurrent(koan) {
    localStorage.setItem("current", koan);
}

function readCurrent() {
    var dev = false
    if(localStorage.getItem("current")) dev = localStorage.getItem("current");
    return dev;
}

function isCompleteKoan(koan) {
    var dev = false
    if(localStorage.getItem('complete.'+koan)) dev = true;
    return dev;
}

function removeKoanComplete(koan) {
    localStorage.removeItem('complete.'+koan);
}

function writeKoanComplete(koan) {
    localStorage.setItem('complete.'+koan,true);
}

function writeSolution(koan, module, solution, answer) {
    if(answer.length>0) localStorage.setItem('answer.'+koan+'.'+module+'.'+solution, answer);
    else localStorage.removeItem('answer.'+koan+'.'+module+'.'+solution);
}

function readSolution(koan,module,solution) {
    var dev = ""
    if(localStorage.getItem('answer.'+koan+'.'+module+'.'+solution)) dev = localStorage.getItem('answer.'+koan+'.'+module+'.'+solution);
    return dev;
}

function cleanLocalStorage() {
    localStorage.clear();
}



// Views Status

function runReadingMode() {
    $('#waiting').removeClass();
}


function hideReadingMode() {
    $('#waiting').addClass('almosthidden');
}


//Disqus

function reloadComments() {
    if(typeof DISQUS === 'undefined' || true){
        // console.log('Disqus not defined');
    }else{
        DISQUS.reset({
          reload: true,
          config: function () {  
            this.page.identifier = hash;  
            this.page.url = "http://example.com/#!"+hash;
          }
        });
    }
    
}

// Utils

function getId(kg) {
    return kg.replace(/\s+/g, '').toLowerCase();
}

function moveToDiv(id) {
	$('html, body').animate({scrollTop:$("#"+id).offset().top}, 500);
	return false;
}