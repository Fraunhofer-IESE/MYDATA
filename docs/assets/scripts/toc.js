$(document).ready(function(){
  var toc = "<ul id='contentList' class='collapsed'>";
  $("h2").slice(1).each(function(i, el){
    var current = $(el);
    current.attr("id", "heading" + i);
    toc += "<li><a href='#heading" + i + "'>" + current.text() + "</a></li>";
  });
  toc += "</ul>";
  $("#toc").html(toc);
});

document.getElementById("toggleButton").addEventListener("click", function() {
    var contentList = document.getElementById("contentList");
    if (contentList.classList.contains("collapsed")) {
        contentList.classList.remove("collapsed");
        contentList.classList.add("expanded");
        this.textContent = "Inhaltsverzeichnis ausblenden";
    } else {
        contentList.classList.remove("expanded");
        contentList.classList.add("collapsed");
        this.textContent = "Inhaltsverzeichnis anzeigen";
    }
});