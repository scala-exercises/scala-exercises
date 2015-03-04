$(function() {
    $.when(
		$.getScript('http://cdnjs.cloudflare.com/ajax/libs/marked/0.3.2/marked.min.js'),
        calculateWaitingFrame(),
        $('head').append('<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" type="text/css" />'),
        // loadDisqus(),
        $.ready.promise()
    ).then(function(){
        loadHighlightJS();
        loadNav();
    });
});

var koansGroups = ["Asserts", "Val and Var", "Classes", "Options", "Objects", "Tuples", "Higher Order Functions", "Lists", "Maps", "Sets", "Formatting", "Pattern Matching", "Case Classes", "Range", "Partially Applied Functions", "Partial Functions", "Implicits", "Traits", "For Expressions", "Infix Prefix and Postfix Operators", "Infix Types", "Mutable Maps", "Mutable Sets", "Sequences and Arrays", "Iterables", "Traversables", "Named and Default Arguments", "Manifests", "Preconditions", "Extractors", "ByName Parameter", "Repeated Parameters", "Parent Classes", "Empty Values", "Type Signatures", "Uniform Access Principle", "Literal Booleans", "Literal Numbers", "Literal Strings", "Type Variance", "Enumerations", "Constructors", "sources"];
var koansGroupsId =[];
var koan = false;
var first = getId(koansGroups[0]);
var hash = false;
var editing = false;
var disqusShortname = 'doingscala';
var baseURL = location.protocol + '//' + location.host;

var timeSearchInputs = 0;
var timeActiveInputs = 0;

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
        if(kg != "sources") var item = drawNavItem(index, kg);
        else var item = false;
        saveJSON(item, getId(kg));
    });
    init();
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
    var a = $('<a></a>').attr({'href':'#', 'data-id':getId(kg)}).html(kg); item.append(a);
    var i = $('<i></i>').attr({'class':'pull-right fa fa-spin'}); a.prepend(i);

    a.click(function(event){
        event.preventDefault();
        window.location.hash = $(this).attr('data-id');
    });
    
    return item;
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
    hideReadingMode(readCacheKoan(hash));
}

function saveJSON(item, id) {
    var url = "json/"+id+".json";
    $.getJSON(url, function(data) {
        writeCacheKoan(id, data);
        if(item) item.find('a i.fa-spin').removeClass('fa-spin');
    })
    .error(function (xhr, ajaxOptions, thrownError){
        if(xhr.status==404) console.log("JSON not found at cache");
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
	var callout = $('<div></div>').attr({'id': 'editLink', 'class':'bs-callout'}); content.append(callout);
    var h4 = $('<h4></h4>').text('Add exercises'); callout.append(h4);
    var id = getId(koan.title);
    var p = $('<p></p>').html('If you would like add other interesting exercises or improve the section "'+koan.title+'", feel free to edit "'+id+'.json" and submit a pull request: <a target="_blank" href="https://github.com/47deg/scala-exercises/edit/master/app/json/'+id+'.json"><i class="fa fa-pencil"></i> Edit</a>'); callout.append(p);

}

// Syntax
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
    activeInputs();
}

function activeInputs() {
    timeActiveInputs = Date.now();
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
                    input.val(readSolution(hash,moduleIndex, inputIndex));
                    studySolution(input.get(0), 'created');
                    input.unbind();
                    input.off();
                    input.on({
                        blur: function() {
                            studySolution(this, 'blur');
                        },
                        keyup: function() {
                            studySolution(this, 'keyup');
                        }
                    });
            	});
            }
        });
    });
    editing = true;
    reloadComments();
}

function mySize(length) {
    var initial = length-1;
    if(initial<0) initial=0;
    return 18 + initial*9;
}

function studySolution(element, event) {
    var input = $(element);
    var answer = input.val();
    var size = mySize(answer.length);
    input.width(size+"px");
    var index = input.attr('data-index');
    var moduleIndex = input.attr('data-module-index');
    var module = koan.modules[moduleIndex];
    if(module.solutions){
        var solution = module.solutions[index];
        writeSolution(hash,moduleIndex,index,answer);
        if(solution==answer) input.addClass('success');
        else input.removeClass('success');
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
}

function writeKoanComplete(koan) {
    localStorage.setItem('complete.'+koan,true);
    if(editing){
        editing = false;
        showCongrat();
    }
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

function writeCacheKoan(id, koan) {
    localStorage.setItem('koandata.'+id, JSON.stringify(koan));
}

function readCacheKoan(id) {
    var dev = ""
    if(localStorage.getItem('koandata.'+id)) dev = JSON.parse(localStorage.getItem('koandata.'+id));
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
    var url = document.URL;
    var title = 'I´ve just completed the "'+koan.title+'" module from Scala Exercises!';
    launchPopup('http://www.facebook.com/sharer/sharer.php?u='+baseURL+'&t='+title)
}

function shareStepTwitter() {
    var url = baseURL+'/koans%23'+hash;
    var text = 'I´ve just completed the "'+koan.title+'" module at ' + url;
    launchPopup('https://twitter.com/home?status='+text)
}

function shareStepGoogle() {
    var url = baseURL+'/koans%23'+hash;
    launchPopup('https://plus.google.com/share?url='+url)
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
    if(typeof data !== "object"){
        setTimeout(function(){
            init();
        }, 500);
    }
    else{
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
        loadDisqus();
        setTimeout(function(){
            reloadComments();
        }, 500);
        
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
// function openConsole() {
//     var consoleWrapper = $('#consoleWrapper');
//     consoleWrapper.toggleClass('shown');
// }


// Utils
function calculateWaitingFrame(){
    var height = $( window ).height();
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

function loadHighlightJS() {
    !function(e){"undefined"!=typeof exports?e(exports):(window.hljs=e({}),"function"==typeof define&&define.amd&&define([],function(){return window.hljs}))}(function(e){function n(e){return e.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;")}function t(e){return e.nodeName.toLowerCase()}function r(e,n){var t=e&&e.exec(n);return t&&0==t.index}function a(e){var n=(e.className+" "+(e.parentNode?e.parentNode.className:"")).split(/\s+/);return n=n.map(function(e){return e.replace(/^lang(uage)?-/,"")}),n.filter(function(e){return N(e)||/no(-?)highlight/.test(e)})[0]}function o(e,n){var t={};for(var r in e)t[r]=e[r];if(n)for(var r in n)t[r]=n[r];return t}function i(e){var n=[];return function r(e,a){for(var o=e.firstChild;o;o=o.nextSibling)3==o.nodeType?a+=o.nodeValue.length:1==o.nodeType&&(n.push({event:"start",offset:a,node:o}),a=r(o,a),t(o).match(/br|hr|img|input/)||n.push({event:"stop",offset:a,node:o}));return a}(e,0),n}function c(e,r,a){function o(){return e.length&&r.length?e[0].offset!=r[0].offset?e[0].offset<r[0].offset?e:r:"start"==r[0].event?e:r:e.length?e:r}function i(e){function r(e){return" "+e.nodeName+'="'+n(e.value)+'"'}l+="<"+t(e)+Array.prototype.map.call(e.attributes,r).join("")+">"}function c(e){l+="</"+t(e)+">"}function u(e){("start"==e.event?i:c)(e.node)}for(var s=0,l="",f=[];e.length||r.length;){var g=o();if(l+=n(a.substr(s,g[0].offset-s)),s=g[0].offset,g==e){f.reverse().forEach(c);do u(g.splice(0,1)[0]),g=o();while(g==e&&g.length&&g[0].offset==s);f.reverse().forEach(i)}else"start"==g[0].event?f.push(g[0].node):f.pop(),u(g.splice(0,1)[0])}return l+n(a.substr(s))}function u(e){function n(e){return e&&e.source||e}function t(t,r){return RegExp(n(t),"m"+(e.cI?"i":"")+(r?"g":""))}function r(a,i){if(!a.compiled){if(a.compiled=!0,a.k=a.k||a.bK,a.k){var c={},u=function(n,t){e.cI&&(t=t.toLowerCase()),t.split(" ").forEach(function(e){var t=e.split("|");c[t[0]]=[n,t[1]?Number(t[1]):1]})};"string"==typeof a.k?u("keyword",a.k):Object.keys(a.k).forEach(function(e){u(e,a.k[e])}),a.k=c}a.lR=t(a.l||/\b[A-Za-z0-9_]+\b/,!0),i&&(a.bK&&(a.b="\\b("+a.bK.split(" ").join("|")+")\\b"),a.b||(a.b=/\B|\b/),a.bR=t(a.b),a.e||a.eW||(a.e=/\B|\b/),a.e&&(a.eR=t(a.e)),a.tE=n(a.e)||"",a.eW&&i.tE&&(a.tE+=(a.e?"|":"")+i.tE)),a.i&&(a.iR=t(a.i)),void 0===a.r&&(a.r=1),a.c||(a.c=[]);var s=[];a.c.forEach(function(e){e.v?e.v.forEach(function(n){s.push(o(e,n))}):s.push("self"==e?a:e)}),a.c=s,a.c.forEach(function(e){r(e,a)}),a.starts&&r(a.starts,i);var l=a.c.map(function(e){return e.bK?"\\.?("+e.b+")\\.?":e.b}).concat([a.tE,a.i]).map(n).filter(Boolean);a.t=l.length?t(l.join("|"),!0):{exec:function(){return null}}}}r(e)}function s(e,t,a,o){function i(e,n){for(var t=0;t<n.c.length;t++)if(r(n.c[t].bR,e))return n.c[t]}function c(e,n){return r(e.eR,n)?e:e.eW?c(e.parent,n):void 0}function f(e,n){return!a&&r(n.iR,e)}function g(e,n){var t=x.cI?n[0].toLowerCase():n[0];return e.k.hasOwnProperty(t)&&e.k[t]}function p(e,n,t,r){var a=r?"":E.classPrefix,o='<span class="'+a,i=t?"":"</span>";return o+=e+'">',o+n+i}function d(){if(!w.k)return n(y);var e="",t=0;w.lR.lastIndex=0;for(var r=w.lR.exec(y);r;){e+=n(y.substr(t,r.index-t));var a=g(w,r);a?(B+=a[1],e+=p(a[0],n(r[0]))):e+=n(r[0]),t=w.lR.lastIndex,r=w.lR.exec(y)}return e+n(y.substr(t))}function h(){if(w.sL&&!R[w.sL])return n(y);var e=w.sL?s(w.sL,y,!0,L[w.sL]):l(y);return w.r>0&&(B+=e.r),"continuous"==w.subLanguageMode&&(L[w.sL]=e.top),p(e.language,e.value,!1,!0)}function v(){return void 0!==w.sL?h():d()}function b(e,t){var r=e.cN?p(e.cN,"",!0):"";e.rB?(M+=r,y=""):e.eB?(M+=n(t)+r,y=""):(M+=r,y=t),w=Object.create(e,{parent:{value:w}})}function m(e,t){if(y+=e,void 0===t)return M+=v(),0;var r=i(t,w);if(r)return M+=v(),b(r,t),r.rB?0:t.length;var a=c(w,t);if(a){var o=w;o.rE||o.eE||(y+=t),M+=v();do w.cN&&(M+="</span>"),B+=w.r,w=w.parent;while(w!=a.parent);return o.eE&&(M+=n(t)),y="",a.starts&&b(a.starts,""),o.rE?0:t.length}if(f(t,w))throw new Error('Illegal lexeme "'+t+'" for mode "'+(w.cN||"<unnamed>")+'"');return y+=t,t.length||1}var x=N(e);if(!x)throw new Error('Unknown language: "'+e+'"');u(x);for(var w=o||x,L={},M="",k=w;k!=x;k=k.parent)k.cN&&(M=p(k.cN,"",!0)+M);var y="",B=0;try{for(var C,j,I=0;;){if(w.t.lastIndex=I,C=w.t.exec(t),!C)break;j=m(t.substr(I,C.index-I),C[0]),I=C.index+j}m(t.substr(I));for(var k=w;k.parent;k=k.parent)k.cN&&(M+="</span>");return{r:B,value:M,language:e,top:w}}catch(A){if(-1!=A.message.indexOf("Illegal"))return{r:0,value:n(t)};throw A}}function l(e,t){t=t||E.languages||Object.keys(R);var r={r:0,value:n(e)},a=r;return t.forEach(function(n){if(N(n)){var t=s(n,e,!1);t.language=n,t.r>a.r&&(a=t),t.r>r.r&&(a=r,r=t)}}),a.language&&(r.second_best=a),r}function f(e){return E.tabReplace&&(e=e.replace(/^((<[^>]+>|\t)+)/gm,function(e,n){return n.replace(/\t/g,E.tabReplace)})),E.useBR&&(e=e.replace(/\n/g,"<br>")),e}function g(e,n,t){var r=n?x[n]:t,a=[e.trim()];return e.match(/(\s|^)hljs(\s|$)/)||a.push("hljs"),r&&a.push(r),a.join(" ").trim()}function p(e){var n=a(e);if(!/no(-?)highlight/.test(n)){var t;E.useBR?(t=document.createElementNS("http://www.w3.org/1999/xhtml","div"),t.innerHTML=e.innerHTML.replace(/\n/g,"").replace(/<br[ \/]*>/g,"\n")):t=e;var r=t.textContent,o=n?s(n,r,!0):l(r),u=i(t);if(u.length){var p=document.createElementNS("http://www.w3.org/1999/xhtml","div");p.innerHTML=o.value,o.value=c(u,i(p),r)}o.value=f(o.value),e.innerHTML=o.value,e.className=g(e.className,n,o.language),e.result={language:o.language,re:o.r},o.second_best&&(e.second_best={language:o.second_best.language,re:o.second_best.r})}}function d(e){E=o(E,e)}function h(){if(!h.called){h.called=!0;var e=document.querySelectorAll("pre code");Array.prototype.forEach.call(e,p)}}function v(){addEventListener("DOMContentLoaded",h,!1),addEventListener("load",h,!1)}function b(n,t){var r=R[n]=t(e);r.aliases&&r.aliases.forEach(function(e){x[e]=n})}function m(){return Object.keys(R)}function N(e){return R[e]||R[x[e]]}var E={classPrefix:"hljs-",tabReplace:null,useBR:!1,languages:void 0},R={},x={};return e.highlight=s,e.highlightAuto=l,e.fixMarkup=f,e.highlightBlock=p,e.configure=d,e.initHighlighting=h,e.initHighlightingOnLoad=v,e.registerLanguage=b,e.listLanguages=m,e.getLanguage=N,e.inherit=o,e.IR="[a-zA-Z][a-zA-Z0-9_]*",e.UIR="[a-zA-Z_][a-zA-Z0-9_]*",e.NR="\\b\\d+(\\.\\d+)?",e.CNR="(\\b0[xX][a-fA-F0-9]+|(\\b\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?)",e.BNR="\\b(0b[01]+)",e.RSR="!|!=|!==|%|%=|&|&&|&=|\\*|\\*=|\\+|\\+=|,|-|-=|/=|/|:|;|<<|<<=|<=|<|===|==|=|>>>=|>>=|>=|>>>|>>|>|\\?|\\[|\\{|\\(|\\^|\\^=|\\||\\|=|\\|\\||~",e.BE={b:"\\\\[\\s\\S]",r:0},e.ASM={cN:"string",b:"'",e:"'",i:"\\n",c:[e.BE]},e.QSM={cN:"string",b:'"',e:'"',i:"\\n",c:[e.BE]},e.PWM={b:/\b(a|an|the|are|I|I'm|isn't|don't|doesn't|won't|but|just|should|pretty|simply|enough|gonna|going|wtf|so|such)\b/},e.CLCM={cN:"comment",b:"//",e:"$",c:[e.PWM]},e.CBCM={cN:"comment",b:"/\\*",e:"\\*/",c:[e.PWM]},e.HCM={cN:"comment",b:"#",e:"$",c:[e.PWM]},e.NM={cN:"number",b:e.NR,r:0},e.CNM={cN:"number",b:e.CNR,r:0},e.BNM={cN:"number",b:e.BNR,r:0},e.CSSNM={cN:"number",b:e.NR+"(%|em|ex|ch|rem|vw|vh|vmin|vmax|cm|mm|in|pt|pc|px|deg|grad|rad|turn|s|ms|Hz|kHz|dpi|dpcm|dppx)?",r:0},e.RM={cN:"regexp",b:/\//,e:/\/[gimuy]*/,i:/\n/,c:[e.BE,{b:/\[/,e:/\]/,r:0,c:[e.BE]}]},e.TM={cN:"title",b:e.IR,r:0},e.UTM={cN:"title",b:e.UIR,r:0},e});hljs.registerLanguage("scala",function(e){var t={cN:"annotation",b:"@[A-Za-z]+"},a={cN:"string",b:'u?r?"""',e:'"""',r:10},r={cN:"symbol",b:"'\\w[\\w\\d_]*(?!')"},c={cN:"type",b:"\\b[A-Z][A-Za-z0-9_]*",r:0},i={cN:"title",b:/[^0-9\n\t "'(),.`{}\[\]:;][^\n\t "'(),.`{}\[\]:;]+|[^0-9\n\t "'(),.`{}\[\]:;=]/,r:0},l={cN:"class",bK:"class object trait type",e:/[:={\[(\n;]/,c:[{cN:"keyword",bK:"extends with",r:10},i]},n={cN:"function",bK:"def val",e:/[:={\[(\n;]/,c:[i]};return{k:{literal:"true false null",keyword:"type yield lazy override def with val var sealed abstract private trait object if forSome for while throw finally protected extends import final return else break new catch super class case package default try this match continue throws implicit"},c:[e.CLCM,e.CBCM,a,e.QSM,r,c,n,l,e.CNM,t]}});
}