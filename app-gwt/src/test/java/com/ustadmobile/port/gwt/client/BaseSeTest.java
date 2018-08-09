package com.ustadmobile.port.gwt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gwt.core.client.GWT;

public class BaseSeTest {

	private WebDriver driver;
	private String geckoDriverLocation = "c:\\opt\\geckodriver\\geckodriver.exe";
	private String chromeDriverLocation = "c:\\opt\\chromedriver\\chromedriver.exe";
	private String appURL = "http://127.0.0.1:8888";
	private ChromeOptions chromeOptions;
	WebDriverWait wait;
	
    @BeforeClass
    public static void setupClass() {
    	//ChromeDriverManager.getInstance().setup();
    }
    
    @Before
    public void setupTest() {
    	
    	System.setProperty("webdriver.gecko.driver", geckoDriverLocation);
    	System.setProperty("webdriver.firefox.driver", geckoDriverLocation);
    	System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
    	
        //driver = new FirefoxDriver();
        //driver = new ChromeDriver();
    	//driver = new RemoteWebDriver(caps);
    	System.out.println("UMSELOG: Creating Driver..");
    	chromeOptions = new ChromeOptions();
    	chromeOptions.setCapability("hello", "world"); //extend like so..
    	//full list here: https://peter.sh/experiments/chromium-command-line-switches/
    	chromeOptions.addArguments("--window-size=500,500");
    	driver = new ChromeDriver(chromeOptions);
    	
    	wait = new WebDriverWait(driver, 20);
    	
        
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    public String getCurrentTitle() {
    	String title = driver.getTitle();
    	System.out.println("Title is: " + title);
    	return title;
    }
    
	
    @Test
    public void testGivenBasePageOpend_whenFeedClicked_thenShoudShowAboutPage() {
    	driver.get(appURL);
    	String currentTitle = getCurrentTitle();
    	
    	//Wait till button is clickable
		By sideNav = By.id("base_footer_button_feed");
		wait.until(ExpectedConditions.elementToBeClickable(sideNav));

		//Click it
		WebElement sideNavElement = driver.findElement(sideNav);
		assertNotNull(sideNavElement);
		sideNavElement.click();
		
		//Assert That we have moved.
		currentTitle = getCurrentTitle();
		
		By aboutText = By.id("about_textbox");
		WebElement aboutTextElement = driver.findElement(aboutText);
		assertNotNull(aboutTextElement);
    }
    
}
