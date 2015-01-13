$(function() {
    $.when(
		$.getScript('http://yandex.st/highlightjs/8.0/highlight.min.js'),
		$.getScript('http://cdnjs.cloudflare.com/ajax/libs/marked/0.3.2/marked.min.js'),
        calculateWaitingFrame(),
        $('head').append('<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" type="text/css" />'),
        loadDisqus(),
        $.ready.promise()
    ).then(function(){
        loadNav();
        init();
        activeMenu();
    });
});


var koansGroups = ["Asserts", "Val and Var", "Classes", "Options", "Objects", "Tuples", "Higher Order Functions", "Lists", "Maps", "Sets", "Formatting", "Pattern Matching", "Case Classes", "Range", "Partially Applied Functions", "Partial Functions", "Implicits", "Traits", "For Expressions", "Infix Prefix and Postfix Operators", "Infix Types", "Mutable Maps", "Mutable Sets", "Sequences and Arrays", "Iterables", "Traversables", "Named and Default Arguments", "Manifests", "Preconditions", "Extractors", "ByName Parameter", "Repeated Parameters", "Parent Classes", "Empty Values", "Type Signatures", "Uniform Access Principle", "Literal Booleans", "Literal Numbers", "Literal Strings", "Type Variance", "Enumerations", "Constructors"];
var koansGroupsId =[];
var koan = false;
var first = getId(koansGroups[0]);
var hash = false;
var editing = false;
var disqusShortname = 'doingscala';
var baseURL = location.protocol + '//' + location.host;

// HASH
$(window).on('hashchange', function() {
    init();
});

$(window).resize(function() {
    calculateWaitingFrame();
});

function loadNav() {
    var count = koansGroups.length;
    $.each(koansGroups, function(index, kg) {
        drawNavItem(index, kg);
    });
}

function init() {
    editing = false;
    if(restoreNav()){
        moveToTop()
        writeCurrent(hash);
        readKoanJSON();
    }
}

// Nav
function drawNavItem(index, kg) {
    var item = $('<li></li>').attr({'class':'item', 'data-id':getId(kg)}); $('#sidebar_menu').append(item);
    var a = $('<a></a>').attr({'href':'#', 'data-id':getId(kg)}).text(kg); item.append(a);
    var i = $('<i></i>').attr({'class':'pull-right fa'}); a.prepend(i);

    a.click(function(event){
        event.preventDefault();
        window.location.hash = $(this).attr('data-id');
    });
}

function restoreNav() {
    var dev = false;
    var items = $('#sidebar_menu .item');
    items.attr('class','item');
    var newhash = window.location.hash.substring(1);
    if(newhash.length==0) setInitialKoan();
    else{
        hash = newhash;
        var item = $('#sidebar_menu .item[data-id="'+hash+'"]');
        item.addClass('active');
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
    $.each(koansGroups, function(index, kg) {
        var id = getId(kg);
        var navItem = $('#sidebar_menu .item[data-id="'+id+'"]');
        if(isCompleteKoan(id)) navItem.addClass('completed');
        else navItem.removeClass('completed');
    });
}

//Koans
function readKoanJSON() {
    runReadingMode();
    var url = "json/"+hash+".json";
    $.getJSON(url, function(data) {
        hideReadingMode(data);
    })
    .error(function (xhr, ajaxOptions, thrownError){
        if(xhr.status==404) console.log("JSON not found");
    });
}

function drawKoan() {
	var wrapper = $('#module_content');
    wrapper.empty();
	var item = $('<div></div>').attr({'class':'item'}); wrapper.append(item);
    // var container = $('<div></div>').attr({'class':'container'}); item.append(container);
	var h2 = $('<h2></h2>').text(koan.title); item.append(h2);
	var content = $('<div></div>').attr({'class':'content'}); item.append(content);

	if(koan.modules){
		$.each(koan.modules, function(indexModule, module) {
			drawModule(content, indexModule, module);
		});
        drawEditLink(content);
	}
    colourify();
	searchInputs();

    setTimeout(function(){
      activeInputs();
    }, 500);

    reloadComments();
}

function drawModule(content, indexModule, module) {
    var moduleWrapper = $('<div></div>').attr({'class':'module'}); content.append(moduleWrapper);
    if(module.preparagraph){
        var preparapraph = $('<div></div>').attr({'class':'preparapraph'}).html(marked(module.preparagraph)); moduleWrapper.append(preparapraph);
    }
    var pre = $('<pre></pre>').html('<div class="ribbon ribbon-wrapper-green"><div class="ribbon-green">Done</div></div>'); moduleWrapper.append(pre);
    var code = $('<code></code>').attr({'data-index':indexModule}).html(module.code); pre.append(code);
    if(module.postparagraph){
        var postparagraph = $('<div></div>').attr({'class':'postparagraph'}).html(marked(module.postparagraph)); moduleWrapper.append(postparagraph);
    }
}

function drawEditLink(content) {
	var callout = $('<div></div>').attr({'id': 'editLink', 'class':'bs-callout'}).hide(); content.append(callout);
    var h4 = $('<h4></h4>').text('Add exercises'); callout.append(h4);
    var id = getId(koan.title);
    var p = $('<p></p>').html('If you would like add other interesting exercises about "'+koan.title+'", feel free to edit "'+id+'.json" and submit a pull request: <a target="_blank" href="https://github.com/47deg/scala-exercises/edit/master/app/json/'+id+'.json"><i class="fa fa-pencil"></i> Edit</a>'); callout.append(p);

}



// Sintaxis
function colourify() {
	hljs.initHighlightingOnLoad();
	$('pre code').each(function(i, block) {
        $(block).addClass('scala');
		hljs.highlightBlock(block);
	});
}

function searchInputs() {
	$('#module_content pre code').each(function(i, block) {

        var code = $(block);
        var moduleIndex=code.attr('data-index');
	    var text = code.text();
		var replaced = text.replace(/\__/g, '<input data-module-index="'+moduleIndex+'" type="text" value="" />');
	    code.html(replaced);
        hljs.highlightBlock(block);

	});

}

function activeInputs() {

    $('#module_content .module').each(function(moduleIndex) {
        var module = $(this);
        var codes = module.find('pre code');
        module.find('pre code').each(function(codeIndex) {
            var code = $(this);


            var pre = code.parent();
            var inputs = code.find('input');
            if(inputs.length>0){
                pre.addClass('koan');
            	inputs.each(function(inputIndex) {
                    var input = $(this);
                    input.attr('data-index',inputIndex);
                    var moduleIndex = input.attr('data-module-index');
                    var inputSize = calculeSize(moduleIndex, inputIndex);
                    // input.width(inputSize);
                    input.animate({width: inputSize+"px"}, 300 );

                    var answer = readSolution(hash,moduleIndex,inputIndex);
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
            }

        });


    });

    editing = true;

}

function calculeSize(moduleIndex, index) {
    var size = 0;
    var module = koan.modules[moduleIndex];
    if(module.solutions){
        var solution = module.solutions[index];
        size = mySize(solution.length);
    }
    return size;
}

function mySize(length) {
    var initial = length-1;
    if(initial<0) initial=0;
    return 18 + initial*9;
}

function studySolution(element) {
    var input = $(element);
    var answer = input.val();

    var size = mySize(answer.length);
    console.log(size);
    input.animate({width: size+"px"}, 100 );
    var index = input.attr('data-index');
    var moduleIndex = input.attr('data-module-index');
    var module = koan.modules[moduleIndex];
    if(module.solutions){
        var solution = module.solutions[index];
        writeSolution(hash,moduleIndex,index,answer);
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


// Sources

function drawSources(data) {
    console.log(data);
	var wrapper = $('#module_content');
    wrapper.empty();
	var item = $('<div></div>').attr({'class':'item'}); wrapper.append(item);
	var h2 = $('<h2></h2>').text(data.title); item.append(h2);
	var p = $('<p></p>').text('Modules were written from several Scala documentation sites. All of them contain an interesting stack of awesome links, listed below.'); item.append(p);
	var content = $('<ul></ul>').attr({'class':'content'}); item.append(content);

	var callout = $('<div></div>').attr({'class':'bs-callout'}); item.append(callout);
    var h4 = $('<h4></h4>').text('Add links'); callout.append(h4);
    var p = $('<p></p>').html('If you would like add other interesting links about Scala, feel free to edit the "sources.json" and pull request:  <a target="_blank" href="https://github.com/47deg/scala-exercises/edit/master/app/json/sources.json"><i class="fa fa-pencil"></i> Edit</a>'); callout.append(p);


    if(data.sources){
        $.each(data.sources, function(indexSource, source) {
            drawSource(content, indexSource, source);

        });
    }

    reloadComments();
}

function drawSource(content, indexSource, source) {
	var sourceWrapper = $('<li></li>').attr({'class':'source'}); content.append(sourceWrapper);
	if(source.title){
		var title = $('<h5></h5>').attr({'class':'title'}).html('<a href="'+source.url+'" target="_blank">'+source.title+'</a>'); sourceWrapper.append(title);
		var description = $('<p></p>').attr({'class':'title'}).html(source.description); sourceWrapper.append(description);
	}
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
    $('#editLink').hide();
}

function writeKoanComplete(koan) {
    localStorage.setItem('complete.'+koan,true);
    if(editing){
        editing = false;
        showCongrat();
    }
    $('#editLink').show();
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

//Modals
function showCongrat() {
    if(didComplete()) showCongratCourse();
    else showCongratModule();
}

function showCongratModule() {
    $('#myModuleModalLabel').text('"'+koan.title+'" completed!');
    $('#moduleModal').modal('show');
}

function showCongratCourse() {
    $('#courseModal').modal('show');
}

function goToNext() {
    var current = $('#sidebar_menu .item.active');
    var items = $('#sidebar_menu .item');
    var numItems = items.length;
    var index = current.index();
    var next = index+1;

    if(next<numItems){
        var nextItem = items.get(next);
        var nextLink = $(nextItem).find('a');
        $('#moduleModal').modal('hide');
        nextLink.click();
    }
}

function didComplete() {
    var completed = false;
    var items = $('#sidebar_menu .item');
    var numItems = items.length;
    var itemsCompleted = $('#sidebar_menu .item.completed');
    var numitemsCompleted = itemsCompleted.length;
    if(numItems == numitemsCompleted) completed = true;
    return completed
}

//Share

function shareStepFacebook() {
    // var url = document.URL;
    var title = "I've just completed some Scala exercises!";
    launchPopup('http://www.facebook.com/sharer/sharer.php?u='+baseURL+'&t='+title)
}

function shareStepTwitter() {
    var text = "I've just completed some Scala exercises at "+baseURL;
    launchPopup('https://twitter.com/home?status='+text)
}

function shareStepGoogle() {
    launchPopup('https://plus.google.com/share?url='+baseURL)
}

function shareCourseFacebook() {
    var title = "I've just completed all the Scala exercises!";
    launchPopup('http://www.facebook.com/sharer/sharer.php?u='+baseURL+'&t='+title)
}

function shareCourseTwitter() {
    var text = "I've just completed all the Scala exercises at "+baseURL;
    launchPopup('https://twitter.com/home?status='+text)
}

function shareCourseGoogle() {
    launchPopup('https://plus.google.com/share?url='+baseURL)
}

function launchPopup(url) {
    window.open(url, 'Social Share', 'height=320, width=640, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, directories=no, status=no');
}




// Views Status

function runReadingMode() {
    $('#waiting').removeClass();
    $('#bottom').hide();
    
}

function hideReadingMode(data) {
    setTimeout(function(){
        
        if(hash == 'sources') drawSources(data)
        else{
            koan = data;
            drawKoan();
        }

        setTimeout(function(){
            $('#waiting').addClass('almosthidden');
            $('#bottom').slideDown();
        }, 500);
    }, 500);

}


//Disqus

function loadDisqus() {
    var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
    dsq.src = '//' + disqusShortname + '.disqus.com/embed.js';
    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
}


function reloadComments() {




    if(typeof DISQUS === 'undefined'){
        console.log('Disqus not defined');

    }else{
        var discussionURL = baseURL + '/#!' + hash;
        DISQUS.reset({
          reload: true,
          config: function () {
              this.page.identifier = hash;
              this.page.url = discussionURL;
          }
        });
    }

}

//Console

function openConsole() {
    var consoleWrapper = $('#consoleWrapper');
    consoleWrapper.toggleClass('shown');
}

// Menu
function activeMenu() {
    $("#menu-close").click(function(e) {
        e.preventDefault();
        $("#sidebar-wrapper").toggleClass("active");
    });

    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#sidebar-wrapper").toggleClass("active");
    });
}

// Utils

function calculateWaitingFrame(){
    var height = $( window ).height();
    console.log(height);
    var waiting = $('#waiting');
    waiting.css('height', height+'px');
}

function getId(kg) {
    return kg.replace(/\s+/g, '').toLowerCase();
}

function moveToDiv(id) {
	$('html, body').animate({scrollTop:$("#"+id).offset().top}, 500);
	return false;
}

function moveToTop() {
	$('html, body').animate({scrollTop:0}, 500);
	return false;
}
