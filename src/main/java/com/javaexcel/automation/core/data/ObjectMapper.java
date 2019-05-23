package com.javaexcel.automation.core.data;
//package com.wellsfargo.automation.core.data;
//
//import org.openqa.selenium.By;
//
//import com.wellsfargo.automation.core.Locator;
//
//public class ObjectMapper {
//	protected static Locator constructObject(String byType, String byContent, String name, String description) {
//		Locator constructed = null;
//		
//		if (description == null){
//			description = name;
//		}
//		
//		switch(byType.trim().toLowerCase()) {
//		case "id":
//			constructed = new Locator(By.id(byContent), description);
//			break;
//		case "cssselector":
//			constructed = new Locator(By.cssSelector(byContent), description);
//			break;
//		case "tagname":
//			constructed = new Locator(By.tagName(byContent), description);
//			break;
//		case "classname":
//			constructed = new Locator(By.className(byContent), description);
//			break;
//		case "xpath":
//			constructed = new Locator(By.xpath(byContent), description);
//			break;
//		case "name":
//			constructed = new Locator(By.name(byContent), description);
//			break;
//		case "linktext":
//			constructed = new Locator(By.linkText(byContent), description);
//			break;
//		case "partiallinktext":
//			constructed = new Locator(By.partialLinkText(byContent), description);
//			break;
//		default:
//			//throw new byNotFoundException();
//		}
//		return constructed;
//	}	
//}
