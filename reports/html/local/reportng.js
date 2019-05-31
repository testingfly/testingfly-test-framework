function toggleElement(elementId, displayStyle)
{
    var element = document.getElementById(elementId);
    var current = element.currentStyle
                ? element.currentStyle['display']
                : document.defaultView.getComputedStyle(element, null).getPropertyValue('display');
    element.style.display = (current == 'none' ? displayStyle : 'none');
}

function toggle(toggleId)
{
    var toggle = document.getElementById ? document.getElementById(toggleId) : document.all[toggleId];
    toggle.textContent = toggle.innerHTML == '\u25b6' ? '\u25bc' : '\u25b6';
}

function toggleTableRow(element){
	var displayStyle = "table-row";
	var current = element.currentStyle
                ? element.currentStyle['display']
                : document.defaultView.getComputedStyle(element, null).getPropertyValue('display');
    element.style.display = (current == 'none' ? displayStyle : 'none');
}

document.addEventListener("DOMContentLoaded", function(){
	var collapsableHeaders = document.querySelectorAll(".collapsable-header");
	
	for (var i = 0; i < collapsableHeaders.length; i++){
		collapsableHeaders[i].addEventListener("click", function() {
			var nextElements = [];
			var nextElement = this;
			while(true){
				nextElement = nextElement.nextElementSibling;
				if (nextElement === null || nextElement.className.indexOf("collapsable-header") != -1){
					break;
				}
				nextElements.push(nextElement);
			}
			nextElements.forEach(function (element){
				toggleTableRow(element);
			});
		});
	}
});
 
