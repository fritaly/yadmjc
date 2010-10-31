if (top !=self) {
	if (window.DELFRM){
		if (window.DELFRMT){
			setTimeout("top.location.href = DELFRM",DELFRMT);
		}else{
			setTimeout("top.location.href = DELFRM",10000);
		}
	}else{
		top.location=self.location;
	}
}else{
	var	cloc=top.location.href;
	var	pcloc=cloc.indexOf('q=cache:');
	if (pcloc>0){
		cloc=cloc.substr(pcloc+8);
		cloc=cloc.substr(cloc.indexOf(':')+1);
		cloc='http://'+cloc;
		
		var	cext='.htm+';
		var	pcext=cloc.indexOf('.htm+');
		if (pcext<0){pcext=cloc.indexOf('.html+');cext='.html+';}
		if (pcext<0){pcext=cloc.indexOf('.php+');cext='.php+';}

		if (pcext>0){
			cloc=cloc.substr(0,cloc.indexOf(cext)+cext.length-1);
			top.location=cloc;
		}
	}else{


	}
	
}


function popup (popWidth,popHeight,Res,Scr,Stb,Params,site){
	leftPos=100;topPos=100;
	if (screen) {
		leftPos=(screen.availWidth-popWidth)/2;
		topPos=(screen.availHeight-popHeight)/2;
	}
	myPop=window.open('','myWindow','resizable='+Res+',width='+popWidth+',height='+popHeight+',top='+topPos+',left='+leftPos+',scrollbars='+Scr+',status='+Stb);
	if (Params=='')	{myPop.location.href = site+'/frames.php';}else{myPop.location.href = site+'/frames.php?'+Params;}
	if (!myPop.opener){myPop.opener = self;}
	myPop.focus();
}


 function addEvent(obj, eventType, afunction, isCapture) {
  // W3C DOM
  if (obj.addEventListener) {
   obj.addEventListener(eventType, afunction, isCapture);
   return true;
  }
  // Internet Explorer
  else if (obj.attachEvent) {
   return obj.attachEvent("on"+eventType, afunction);
  }
  else return false;
 }

 function removeEvent(obj, eventType, afunction, isCapture) {
  if (obj.removeEventListener) {
   obj.removeEventListener(eventType, afunction, isCapture);
   return true;
  }
  else if (obj.detachEvent) {
   return obj.detachEvent("on"+eventType, afunction);
  }
  else return false;
 }

 function processAnchorEvent(event) {
  if ((event.type=="mouseover") || (event.type=="focus")) {
   // Internet Explorer
   if ((event.srcElement.innerText==null)||(event.srcElement.innerText=="")){
     if ((event.srcElement.getAttribute("title")!=null)&&(event.srcElement.getAttribute("title")!="")){
	   window.status=event.srcElement.getAttribute("title");  
     }
   }else{
    if (event.srcElement) window.status=event.srcElement.innerText;
   }
   // W3C DOM
   if (event.currentTarget) {
    window.status=event.currentTarget.getAttribute("title");
    event.stopPropagation();
    if (event.cancelable) event.preventDefault();
   }
  }
  if ((event.type=="mouseout") || (event.type=="blur")) {
   if (event.srcElement) window.status="";
   if (event.currentTarget) {
    window.status="";
    event.stopPropagation();
    if (event.cancelable) event.preventDefault();
    // in case of mouseout anything should be done for focused links.
   }
  }
  return true; // for IE, identical to event.preventDefault()
 }



function CIFRContent(IFRWindowObject,URLLocation){
	IFRWindowObject.location.href=URLLocation;
	return false;
}




function markEndDynData(){
	var sarray=new Array();
	if (window.GLB_SELI){
		if (GLB_SELI.length>2){
			GLB_SELI=decode(GLB_SELI);
			sarray=GLB_SELI.split('=');
		}
	}
	for(i=1;i<=20;i++){
		iele=window.IFR_DYN_DATA.document.getElementById('FAM'+i);
		img=document.getElementById('AM'+i);
		if ((img)&&(iele)){
			img.className='';
			if (iele.width>3){
				img.width=iele.width;
				img.height=iele.height;
				img.src=iele.src;
			}else{
				found=false;
				for (j=0;j<5; j++){
					iele=window.IFR_DYN_DATA.document.getElementById('FAM'+i+'-'+j);
					if ((img)&&(iele)){
						if (iele.width>3){
							img.width=iele.width;
							img.height=iele.height;
							img.src=iele.src;
							found=true;
							break;
						}
					}
				}
				if (found==false){
					iele=window.IFR_DYN_DATA.document.getElementById('NOIM');
					img.width=iele.width;
					img.height=iele.height;
					img.src=iele.src;
				}
			}

		}

	}
};


function procAmImages(){
	if (window.GLB_ILOC){
		CIFRContent(window.frames.IFR_DYN_DATA,window.GLB_ILOC);
	}
}

 function initAnchorStatus() {
  var anchorList, aTitle, i;
  if (window.scrollbox){ScrollInit();}
  if (document.getElementsByTagName) {
   anchorList=document.getElementsByTagName("a");
   if (anchorList.length>0)
    for (i=0; i<anchorList.length; i++) {
     aTitle=anchorList[i].innerText;
     if ((aTitle==null) || (aTitle=="")){aTitle=anchorList[i].getAttribute("title");}
     if ((aTitle!=null) && (aTitle!="")) {
	  anchorList[i].title=aTitle;
      addEvent(anchorList[i], "mouseover", processAnchorEvent, false);
      addEvent(anchorList[i], "focus", processAnchorEvent, false);
      addEvent(anchorList[i], "mouseout", processAnchorEvent, false);
      addEvent(anchorList[i], "blur", processAnchorEvent, false);
     }
    }
  }
  if (top == self){
	  if (window.enable_redirect){enable_redirect_f();}
  }
  if (window.ufunction){ufunction();}
  procAmImages();
}


var SubmitCount___Data=0;
onload = initAnchorStatus;


var base64s ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
function decode(encStr) {
  var bits, decOut = '', i = 0;
  for(; i<encStr.length; i += 4){
    bits =
     (base64s.indexOf(encStr.charAt(i))    & 0xff) <<18 |
     (base64s.indexOf(encStr.charAt(i +1)) & 0xff) <<12 | 
     (base64s.indexOf(encStr.charAt(i +2)) & 0xff) << 6 |
      base64s.indexOf(encStr.charAt(i +3)) & 0xff;
	 decOut += String.fromCharCode((bits & 0xff0000) >>16, (bits & 0xff00) >>8, bits & 0xff);
  }
  if(encStr.charCodeAt(i -2) == 61)
    undecOut=decOut.substring(0, decOut.length -2);
  else if(encStr.charCodeAt(i -1) == 61)
    undecOut=decOut.substring(0, decOut.length -1);
  else undecOut=decOut;
  return undecOut;
}

function addbookmark(bookmarkurl,bookmarktitle){
//	if (document.all) window.external.AddFavorite(bookmarkurl,bookmarktitle)
	if (window.sidebar) {
		// Mozilla Firefox Bookmark
		window.sidebar.addPanel(bookmarktitle, bookmarkurl,"");
	}else if( window.external ) {
		// IE Favorite
		window.external.AddFavorite( bookmarkurl, bookmarktitle);
	}else if(window.opera && window.print) {
		// Opera
		var a = document.createElement("A");
		a.rel = "sidebar";
		a.target = "_search";
		a.title = bookmarktitle;
		a.href = bookmarkurl;
		a.click();
	}
}

function printit(){  
if (window.print) {
    window.print() ;  
} else {
    var WebBrowser = '<OBJECT ID="WebBrowser1" WIDTH=0 HEIGHT=0 CLASSID="CLSID:8856F961-340A-11D0-A96B-00C04FD705A2"></OBJECT>';
	document.body.insertAdjacentHTML('beforeEnd', WebBrowser);
    WebBrowser1.ExecWB(6, 2);
}
}


function readCookie(name){
var nameEQ = name + "=";
var ca = document.cookie.split(';');
for(var i=0;i < ca.length;i++){
  var c = ca[i];
  while (c.charAt(0)==' ') c = c.substring(1,c.length);
  if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
}
return null;
}


function dht_pop(menu){
	if (document.getElementById){
		document.getElementById(menu).className = "mvv";
	}else{
		document[menu].className = "mvv";
	}
}

function dht_shut(menu){
	if (document.getElementById){
		document.getElementById(menu).className = "mhh";
	}else{
		document[menu].className = "mhh";
	}
}

var nsx;
var nsy;
if (!document.all) {
  window.captureEvents(Event.MOUSEMOVE);
  window.onmousemove= get_mouse;
}
function get_mouse(e) {
	nsx=e.pageX-10;
	nsy=e.pageY+5;
}

function gl(id){
	var res
	if(document.getElementById) res=document.getElementById(id)
	if(document.all) res=document.all[id]
	if (res) return res.style
	return null
}

function h_in(id) {
	var hlp=gl('hid'+id)
	if (hlp) {
		if (document.all) {
			nsy=event.y+document.body.scrollTop;
			nsx=event.x+document.body.scrollLeft;
		}
		hlp.top=nsy+20+'px';
		hlp.left=(nsx>610?nsx-470:140)+'px';
		hlp.visibility='visible';
	}
}

function h_out(id) {
	var hlp=gl('hid'+id);
	if(hlp) hlp.visibility='hidden';
}


function MA(ACT,QPARA){
	aobj=document.___smap.AUT;
	if (aobj){
		pp=aobj.value;
		if (!((typeof pp == 'string')&&(pp.length>2))){return false;}
	}
	if (ACT!=null){
		if (typeof(QPARA)!="string"){QPARA='';}
		if (ACT.indexOf('?')>0){
			QPARA1=ACT.substring(ACT.indexOf('?')+1,ACT.length);
			ACT=ACT.substring(0,ACT.indexOf('?'));
		}else{QPARA1='';}
		if (QPARA!=''){if ((QPARA.charAt(0)=='?')||(QPARA.charAt(0)=='&')){QPARA=QPARA.substring(1,QPARA.length);}}
		if (QPARA1.length>1){if (QPARA.length>1){QPARA=QPARA1+'&'+QPARA;}else{QPARA=QPARA1;}}
		if (QPARA.length>1){
			if (readCookie('RULE')==null){
				top.location=ACT+'?AUT='+document.___smap.AUT.value+'&FF_UMX=Y&'+QPARA;
			}else{
				top.location=ACT+'?'+QPARA+'&FF_UMX=Y';
			}
		}else{
			if (readCookie('RULE')==null){
				top.location=ACT+'?AUT='+document.___smap.AUT.value+'&FF_UMX=Y';
			}else{
				top.location=ACT;
			}
		}
	}
	return false;
}

function C(U){U=U.replace(/\[/g,"/");U=U.replace(/\]/g,".");top.location.href=U;}