package com.ustadmobile.port.gwt.client;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class IndexSeTest {

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
    
    public void showTitle() {
    	System.out.println(driver.getTitle());
    }

	/*
	 * Test testIndex() assumes that the server was started. 
	 */
	
	@Test
    public void testThisSimpleSelennium() throws MalformedURLException {
		assertEquals("/", "/");        
        
        System.out.println("UMSELOG: Created Driver. Manipulating Window..");
        //driver.manage().window().maximize();
        System.out.println("UMSELOG: Window manipulated. Going to URL..");
        driver.get(appURL);
        
        assertEquals("gwtapp", driver.getTitle());
        
        
        System.out.println("UMSELOG: Quiting..");
        driver.quit();
        assertEquals("/", "/");
    }
	
	
	@Test
	public void testGivenIndexPageOpened_whenAboutClicked_thenShouldShowAboutPage() {
		driver.get(appURL);
		showTitle();
		//wait.until(ExpectedConditions.elementToBeClickable(hamburger));
		By hamburgerByClass = By.className("side-nav");
		By sideNav = By.className("button-collapse");
		
		
		wait.until(ExpectedConditions.elementToBeClickable(sideNav));
		WebElement sideNavElement = driver.findElement(sideNav);
		sideNavElement.click();
		
		By updateButton = By.id("updateButton");
		wait.until(ExpectedConditions.elementToBeClickable(updateButton));
		WebElement updateButtonElement = driver.findElement(updateButton);
		updateButtonElement.click();
		
		showTitle();
		assertEquals("/","/");
		

	}
	
}
