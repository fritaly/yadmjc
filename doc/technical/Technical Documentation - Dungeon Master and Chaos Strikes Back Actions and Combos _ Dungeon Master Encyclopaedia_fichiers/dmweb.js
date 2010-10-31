/**
* hoverIntent r5 // 2007.03.27 // jQuery 1.1.2
* <http://cherne.net/brian/resources/jquery.hoverIntent.html>
* 
* @param  f  onMouseOver function || An object with configuration options
* @param  g  onMouseOut function  || Nothing (use configuration options object)
* @return    The object (aka "this") that called hoverIntent, and the event object
* @author    Brian Cherne <brian@cherne.net>
*/
(function($){$.fn.hoverIntent=function(f,g){var cfg={sensitivity:7,interval:100,timeout:0};cfg=$.extend(cfg,g?{over:f,out:g}:f);var cX,cY,pX,pY;var track=function(ev){cX=ev.pageX;cY=ev.pageY;};var compare=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);if((Math.abs(pX-cX)+Math.abs(pY-cY))<cfg.sensitivity){$(ob).unbind("mousemove",track);ob.hoverIntent_s=1;return cfg.over.apply(ob,[ev]);}else{pX=cX;pY=cY;ob.hoverIntent_t=setTimeout(function(){compare(ev,ob);},cfg.interval);}};var delay=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);ob.hoverIntent_s=0;return cfg.out.apply(ob,[ev]);};var handleHover=function(e){var p=(e.type=="mouseover"?e.fromElement:e.toElement)||e.relatedTarget;while(p&&p!=this){try{p=p.parentNode;}catch(e){p=this;}}if(p==this){return false;}var ev=jQuery.extend({},e);var ob=this;if(ob.hoverIntent_t){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);}if(e.type=="mouseover"){pX=ev.pageX;pY=ev.pageY;$(ob).bind("mousemove",track);if(ob.hoverIntent_s!=1){ob.hoverIntent_t=setTimeout(function(){compare(ev,ob);},cfg.interval);}}else{$(ob).unbind("mousemove",track);if(ob.hoverIntent_s==1){ob.hoverIntent_t=setTimeout(function(){delay(ev,ob);},cfg.timeout);}}};return this.mouseover(handleHover).mouseout(handleHover);};})(jQuery);

$(document).ready(function(){
  // Enable Quick Menu for Content Categories links
  $("#quickmenu").append("<form id=\"ContentCategories\">\
<h2>Content Categories</h2>\
<table>\
<tr class=\"odd\"><td><input type=\"button\" value=\"Show\" id=\"ContentCategoriesSubmit\" /></td> \
<td>Click a category to display the list of pages in this category. \
Check several categories and click 'Show' to display the list of pages in all selected categories. \
Click anywhere outside this category list to hide it.</td></tr>\
<tr class=\"even\"><th>Games</th>\
<td>\
<input type=\"checkbox\" name=\"C17\" /><a href=\"?q=taxonomy/term/17\">Dungeon&nbsp;Master</a> \
<input type=\"checkbox\" name=\"C22\" /><a href=\"?q=taxonomy/term/22\">Chaos&nbsp;Strikes&nbsp;Back</a> \
<input type=\"checkbox\" name=\"C33\" /><a href=\"?q=taxonomy/term/33\">Dungeon&nbsp;Master&nbsp;II</a> \
<input type=\"checkbox\" name=\"C34\" /><a href=\"?q=taxonomy/term/34\">Theron's&nbsp;Quest</a> \
<input type=\"checkbox\" name=\"C35\" /><a href=\"?q=taxonomy/term/35\">Dungeon&nbsp;Master&nbsp;Nexus</a> \
</td></tr>\
<tr class=\"odd\"><th>Platforms</th>\
<td>\
<input type=\"checkbox\" name=\"C21\" /><a href=\"?q=taxonomy/term/21\">Amiga</a> \
<input type=\"checkbox\" name=\"C24\" /><a href=\"?q=taxonomy/term/24\">Apple&nbsp;IIGS</a> \
<input type=\"checkbox\" name=\"C20\" /><a href=\"?q=taxonomy/term/20\">Atari&nbsp;ST</a> \
<input type=\"checkbox\" name=\"C26\" /><a href=\"?q=taxonomy/term/26\">FM-Towns</a> \
<input type=\"checkbox\" name=\"C59\" /><a href=\"?q=taxonomy/term/59\">IBM&nbsp;PS/V</a> \
<input type=\"checkbox\" name=\"C29\" /><a href=\"?q=taxonomy/term/29\">Macintosh</a> \
<input type=\"checkbox\" name=\"C28\" /><a href=\"?q=taxonomy/term/28\">PC</a> \
<input type=\"checkbox\" name=\"C27\" /><a href=\"?q=taxonomy/term/27\">PC-9801&nbsp;/&nbsp;PC-9821</a> \
<input type=\"checkbox\" name=\"C30\" /><a href=\"?q=taxonomy/term/30\">Sega&nbsp;CD&nbsp;/&nbsp;Mega&nbsp;CD</a> \
<input type=\"checkbox\" name=\"C32\" /><a href=\"?q=taxonomy/term/32\">Sega&nbsp;Saturn</a> \
<input type=\"checkbox\" name=\"C25\" /><a href=\"?q=taxonomy/term/25\">Super&nbsp;NES&nbsp;/&nbsp;Super&nbsp;Famicom</a> \
<input type=\"checkbox\" name=\"C31\" /><a href=\"?q=taxonomy/term/31\">TurboGrafx&nbsp;/&nbsp;PC&nbsp;Engine</a> \
<input type=\"checkbox\" name=\"C23\" /><a href=\"?q=taxonomy/term/23\">X68000</a> \
</td></tr>\
<tr class=\"even\"><th>Other</th>\
<td>\
<input type=\"checkbox\" name=\"C40\" /><a href=\"?q=taxonomy/term/40\">Champions</a> \
<input type=\"checkbox\" name=\"C57\" /><a href=\"?q=taxonomy/term/57\">Creatures</a> \
<input type=\"checkbox\" name=\"C41\" /><a href=\"?q=taxonomy/term/41\">Items</a> \
<input type=\"checkbox\" name=\"C44\" /><a href=\"?q=taxonomy/term/44\">Spells</a> \
<input type=\"checkbox\" name=\"C42\" /><a href=\"?q=taxonomy/term/42\">Maps</a> \
<input type=\"checkbox\" name=\"C45\" /><a href=\"?q=taxonomy/term/45\">Hints</a> \
<input type=\"checkbox\" name=\"C46\" /><a href=\"?q=taxonomy/term/46\">Walkthroughs</a> \
<input type=\"checkbox\" name=\"C56\" /><a href=\"?q=taxonomy/term/56\">Reviews</a> \
<input type=\"checkbox\" name=\"C43\" /><a href=\"?q=taxonomy/term/43\">Screenshots</a> \
<input type=\"checkbox\" name=\"C18\" /><a href=\"?q=taxonomy/term/18\">Scans</a> \
<input type=\"checkbox\" name=\"C19\" /><a href=\"?q=taxonomy/term/19\">Manuals</a> \
<input type=\"checkbox\" name=\"C47\" /><a href=\"?q=taxonomy/term/47\">Downloads</a> \
<input type=\"checkbox\" name=\"C49\" /><a href=\"?q=taxonomy/term/49\">Music</a> \
<input type=\"checkbox\" name=\"C58\" /><a href=\"?q=taxonomy/term/58\">Videos</a> \
<input type=\"checkbox\" name=\"C84\" /><a href=\"?q=taxonomy/term/84\">Advertisements</a> \
<input type=\"checkbox\" name=\"C85\" /><a href=\"?q=taxonomy/term/85\">Related&nbsp;products</a> \
<input type=\"checkbox\" name=\"C86\" /><a href=\"?q=taxonomy/term/86\">Audio&nbsp;CD</a> \
<input type=\"checkbox\" name=\"C55\" /><a href=\"?q=taxonomy/term/55\">Hint&nbsp;books</a> \
<input type=\"checkbox\" name=\"C87\" /><a href=\"?q=taxonomy/term/87\">FTL&nbsp;Games</a> \
<input type=\"checkbox\" name=\"C74\" /><a href=\"?q=taxonomy/term/74\">Interviews</a> \
<input type=\"checkbox\" name=\"C36\" /><a href=\"?q=taxonomy/term/88\">Clones (Traditional)</a> \
<input type=\"checkbox\" name=\"C36\" /><a href=\"?q=taxonomy/term/89\">Clones (Modern)</a> \
<input type=\"checkbox\" name=\"C81\" /><a href=\"?q=taxonomy/term/81\">CSBwin</a> \
<input type=\"checkbox\" name=\"C80\" /><a href=\"?q=taxonomy/term/80\">Tools</a> \
<input type=\"checkbox\" name=\"C39\" /><a href=\"?q=taxonomy/term/39\">Technical&nbsp;Documentation</a> \
<input type=\"checkbox\" name=\"C54\" /><a href=\"?q=taxonomy/term/54\">News</a> \
<input type=\"checkbox\" name=\"C50\" /><a href=\"?q=taxonomy/term/50\">PDF</a> \
<input type=\"checkbox\" name=\"C52\" /><a href=\"?q=taxonomy/term/52\">Web&nbsp;site</a> \
</td></tr>\
</table></form>");
  $("a:contains(Content Categories)").hoverIntent(function() {
    $("#quickmenu").show();
  }, function() {
    return false;
  });
  $("body").click(function(event) {
    clickOutsideOfContentCategories = true;
    // Internet Explorer
    if (event.srcElement)
    {
      $(event.srcElement).parents("form#ContentCategories").each(function(i) {
        clickOutsideOfContentCategories = false;
      });
    }
    // Netscape and Firefox
    else if (event.target)
    {
      $(event.target).parents("form#ContentCategories").each(function(i) {
        clickOutsideOfContentCategories = false;
      });
    }
    if (clickOutsideOfContentCategories) {
      $("#quickmenu").hide();
    }
  });
  $("input#ContentCategoriesSubmit").click(function(event) {
    arg = '';
    $("form#ContentCategories").find("input[@type$='checkbox']").each(function() {
      if (this.checked) {
        if (arg == '') {
          arg = this.name;
        } else {
          arg = arg + ',' + this.name
        }
      }
    });
    // Remove all 'C'
    arg = arg.replace(/C/g, '');
    arg = 'http://dmweb.free.fr/?q=taxonomy/term/' + arg;
    window.location = arg;
  });
  // Build Table of Content
  $(document).ready(function () {
    $('div.node > div.content > *').each(function(i) {
      if ($(this).is("h3")) {
        $(this).attr('id', $(this).attr('id') || "toc" + i);
        $('#toc-content').append('<div class="toc-h3"><a href="#' + $(this).attr('id') + '">' + $(this).html() + '</a></div>');
      }
      if ($(this).is("h4")) {
        $(this).attr('id', $(this).attr('id') || "toc" + i);
        $('#toc-content').append('<div class="toc-h4"><a href="#' + $(this).attr('id') + '">' + $(this).html() + '</a></div>');
      }
    });
    if ($('#toc-content > *').length > 1) {
      $('#toc').show();
    }
  });
  // Enable sound playback for links to audio files
  $("a").filter(".file-audio").click(function(){
      if (typeof(soundManager.getSoundById(this.href)) == 'object') {
        if (soundManager.getSoundById(this.href).playState == 1) {
          soundManager.stop(this.href);
        } else {
          soundManager.stopAll();
          soundManager.play(this.href, this.href);
        }
      } else {
        soundManager.stopAll();
        soundManager.play(this.href, this.href);
      }
    return false;
  });
  // Enable hover and image viewer for some images
  $("img").filter(".viewer").click(function(){
    window.open('sites/all/modules/dmwebmodule/ImageViewer.htm?url=' + this.src, '', 'toolbar=no,status=no,scrollbars=no,location=no,menubar=no,directories=no,resizable=yes,width=700,height=530')
  }).hover(function() {
    $(this).addClass('viewer-hover');
    }, function() {
    $(this).removeClass('viewer-hover');
  });
  // Enable hover for thumbnails
  $("img.thumbnail").hover(function() {
    $(this).addClass('thumbnail-hover');
    }, function() {
    $(this).removeClass('thumbnail-hover');
  });
  // Insert translation link
  $("h3#translationlink").after("<p><a href=\"http://www.windowslivetranslator.com/BV.aspx?MKT=en&lp=ja_en&a=" + window.location + "\">Translate this page to English with Windows Live Translator</a></p>");
  // New books with lightbox2
  //$("dl.lbbook").hide();
  $("dd").each(function(i) {
    $("a[rel=lightbox]", $(this)).each(function() {
      $(this).attr('rel', "lightbox[group" + i + "]");
    });
  });
  // Books
  $("dl.book").hide().each(function(i) {
    bookID = "book" + i;
    $(this).before("<p><img class=\"previous\" src=\"sites/all/modules/dmwebmodule/ArrowLeft.png\" alt=\"Previous\" title=\"Previous\" width=\"30\" height=\"23\"><select class=\"book\" id=\"" + bookID + "\"></select><img class=\"next\" src=\"sites/all/modules/dmwebmodule/ArrowRight.png\" alt=\"Next\" title=\"Next\" width=\"30\" height=\"23\"></p>");
    $("a.file-image", $(this)).each(function(i) {
      $("select#" + bookID).append("<option value=\"" + this.href + "\">" + this.title + "</option>");
    });
    $("img.screenshot", $(this)).each(function(i) {
      $("select#" + bookID).append("<option value=\"" + this.src + "\">" + this.title + "</option>");
    });
    firstpage = $("select#" + bookID + " option:first").val();
    $("select#" + bookID).val(firstpage);
    $(this).before("<p><img class=\"" + bookID + "\" src=\"" + firstpage + "\" alt=\"Book page\"></p>");
  });
  $("select.book").change(function(){
    $("img." + $(this).attr('id')).attr('src', $(this).val());
  });
  $("img.previous").click(function() {
    if ($(this).next()[0].selectedIndex > 0) {
      $(this).next()[0].selectedIndex--;
      $(this).next().change();
    };
  });
  $("img.next").click(function() {
    if ($(this).prev()[0].selectedIndex < $(this).prev()[0].length - 1) {
      $(this).prev()[0].selectedIndex++;
      $(this).prev().change();
    };
  });
  // Hint Oracle
  if ($("dl.hintoracle").length > 0) {
    hoHideAllHints();
  // Insert Hint Oracle UI
    $("dl.hintoracle").before("<p>To query the Hint Oracle:</p>\
<ol><li>Select the coordinates of your champions in the dungeon:<br />\
<strong>Level <select size=\"1\" id=\"selectLvl\" onchange=\"javascript:hoUpdateLists();\">\
<option selected=\"selected\" value=\"17,48,14,45\">0</option>\
<option value=\"7,38,7,38\">1</option>\
<option value=\"7,37,5,34\">2</option>\
<option value=\"5,36,6,33\">3</option>\
<option value=\"9,37,8,30\">4</option>\
<option value=\"0,30,0,29\">5</option>\
<option value=\"3,31,4,31\">6</option>\
<option value=\"5,30,5,34\">7</option>\
<option value=\"5,31,11,38\">8</option>\
<option value=\"6,31,11,37\">9</option></select> X <select size=\"1\" id=\"selectX\">\
<option selected=\"selected\">0</option></select> Y <select size=\"1\" id=\"selectY\">\
<option selected=\"selected\">0</option></select></strong></li>\
<li>Click <a href=\"javascript:hoConsult(false);\">Consult the Hint Oracle</a>.</li>\
<li>Click on each hint title and sentence to reveal the hints one step at a time.</li></ol>\
<p>You can also <a href=\"javascript:hoConsult(true);\">Show all the hints for the selected level</a> (X and Y ignored), <a href=\"javascript:hoShowAllHints();\">Show all the hints</a> or <a href=\"javascript:hoHideAllHints();\">Hide all the hints</a>.</p>");
    hoUpdateLists();
    hoHintClickBehavior();
  }
});

function hoHintClickBehavior() {
  // Enable behavior to reveal hints one at a time
  $("dl.hintoracle > *").click(function(){
    $(this).removeAttr("title").unbind("click").unbind("mouseover").unbind("mouseout").removeClass('hint-hover');
    element = $(this).next();
    if ($(element).is("dd")) {
      $(element).show();
      if ($(element).next().is("dd")) {
        $(element).attr("title", "Click to reveal next hint").hover(function() {
          $(this).addClass('hint-hover');
          }, function() {
          $(this).removeClass('hint-hover');
        });
      }
    }
  });
}

function hoUpdateLists() {
  limits = $("#selectLvl").val().split(",");
  for (i = 0; i < limits.length; i++) limits[i] = parseInt(limits[i], 10);
  $("#selectX").empty();
  $("#selectY").empty();
  for (i = limits[0]; i < (1 + limits[1]); i++) $("#selectX").append("<option value=\"" + (i - limits[0]) + "\">" + i + "</option>");
  for (i = limits[2]; i < (1 + limits[3]); i++) $("#selectY").append("<option value=\"" + (i - limits[2]) + "\">" + i + "</option>");
  $("#selectX").val(0);
  $("#selectY").val(0);
}

function hoHideAllHints() {
  $("dl.hintoracle > *").hide();
}

function hoShowAllHints() {
  $("dl.hintoracle > *").show();
}

function hoConsult(levelonly) {
  hoHideAllHints();
  hoHintClickBehavior();
  lvl = parseInt($("#selectLvl > option:selected").text(), 10);
  $("dt").filter(".J" + lvl).show().attr("title", "Click to reveal first hint").hover(function() {
    $(this).addClass('hint-hover');
    }, function() {
    $(this).removeClass('hint-hover');
  });
  if (levelonly) {
    $("dt").filter(".L" + lvl).show().attr("title", "Click to reveal first hint").hover(function() {
      $(this).addClass('hint-hover');
      }, function() {
      $(this).removeClass('hint-hover');
    });
  } else {
    $("dt").filter("." + ((1024 * lvl) + (32 * parseInt($("#selectX").val(), 10)) + parseInt($("#selectY").val(), 10) + 49152).toString(16).toUpperCase()).show().attr("title", "Click to reveal first hint").hover(function() {
      $(this).addClass('hint-hover');
      }, function() {
      $(this).removeClass('hint-hover');
    });
  }
}