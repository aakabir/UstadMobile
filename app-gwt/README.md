## UstadMobile GWT project

This branch : gwtdev2 is the GWT branch of UstadMobile application. Here resides all GWT components - code and assets for the project. 

#### Why GWT?
GWT is Google's Web Toolkit. It allows us to use the same underlining java code used on android/j2me on the web. This project allows to get an app like experience on the web. eg: login, browse opds, view content, access statistical data (if your role allows it), etc. In addition functions that typically start on the web such as user and group management can be made available to all modern platforms of UstadMobile under one core code base. 
GWT would still need to have its own views, system implementation of some platform specific tasks such as xml parsing, HTTP request, replacement for files, etc. 
GWT would also need to be visually scalable within different device types.

We've choosen to use this structure for UstadMobile GWT:
GWTP uiBinders for Views and GWT-Material for style and consistency with Material Design layouts on Android.

#### Screenshots of the application and progress:
![OPDS Catalog](images/UstadMobileGWT01.PNG "Home")
![Settings Menu](images/UstadMobileGWT02.PNG "Settings")

#### Project folder:
All GWT related code and assets live in the app-gwt folder. This is also the folder for the eclipse project. Since GWTP uses Maven, app-gwt is also a maven project and can be built frm the command line. 

#### Building:
Git clone and checkout gwtdev2 branch. 
```console
git checkout gwtdev2
```
Then go to app-gwt folder
```console
cd app-gwt
```
Run buildcore.sh <- This script copies core files to the project. It also copies lib db, annotations and utils. It also removes any classes that cannot be replaced by the GWTp build procedure. 
```console
./buildcore.sh
```

For an IDE independent build procedure:
On a console where maven is present, first clean (just in case) and then install :
```console
maven clean
maven install
```
In order to run the project we need to launch eclipse and set it up. 
Make sure you have GWT plugin for Ecipse installed. 
Import existing project to eclipse and choose the folder app-gwt
Add a new run configuration in eclipse - this will be the config that runs the gwt application.

	Goals: gwt:run
	Base directory : Choose the project imported when you click Workspace..
	Give it a recognizable name. eg: app-gwt GWT Run

If you didnt do the command line tools, you can right click the project and select : Run As > Maven clean.. and then Run As > Maven install..
Then click the Run icon dropdown and select app-gwt GWT Run to run the application. This will open a Jetty instance after successful build and compilation. You can tell launch the application and see where it is at now in development. 


#### Project Structure ####
For clarity and sanity, here is how the project is structured right now.

```console
repo/app-gwt/	<- This is where all the GWT bit lives. 
├─ core-src/	<- Contains core source/assets/res copied via build .sh
│  ├─ main/ 
│  │  ├─ ...
├─ images/	<- Contains images for this MD & any other doc/wiki
│
├─ lib-database-annotation-src/
├─ lib-database-src/
├─ lib-util-src/
│
├─ src/		<- The main GWT specific source folder 
│  ├─ main/
│  │  ├─ assets/
│  │  │	 ├─ com/ustadmobile/core 
│  │  │		├─ <Files>	<- Contains feeds, html, prop  files for core
│
│  │  ├─ java/
│  │  │	 ├─ com/ustadmobile/
│  │  │	 │  ├─ core/
│  │  │	 │  │  ├─ db/		<- DB Manager Factory returns GWT impl
│  │  │	 │  │  ├─ impl/		<- SystemImpl Factory return GWT impl
│  │  │	 │  │  ├─ opds/		<- OPDS Async Helper 
│  │  │	 │  │  ├─ util/		<- Utilities eg UMCalendar impl
│  │  │	 │  │  ├─ about.html <- About file here for now.
│  │  │	 │  ├─ port/gwt/
│  │  │	 │  │  ├─ client/ 	<- This package has GWT/GWTP MVP specific code
│  │  │	 │  │  │  ├─ application/ <- Consists of the application and every single Views, Modules, Presenters, Gatekeepers.
│  │  │	 │  │  │  ├─ db/	<- DAO's, GWT impl of DB Manager, DAOs, OpdsEntryRepository, etc
│  │  │	 │  │  │  ├─ gin/	<- Contains MAIN module. It is from this main module where all child modules. (used by GWTP)
│  │  │	 │  │  │  ├─ impl/	<- GWT impl of SystemImpl, Logging, HttpResponse.
│  │  │	 │  │  │  ├─ place/	<- Consists NamesTokens that identifies all Places available as String urls.
│  │  │	 │  │  │  ├─ resources/	<- Contains AppResources class
│  │  │	 │  │  │  ├─ test/		<- Test files go here
│  │  │	 │  │  │  ├─ util/		<- GWT specific util files go here
│  │  │	 │  │  ├─ xmlpull/	<- XmlPullParser GWT implementation
│  │  │	 │  │  │  ├─ XmlPullParserGWT.jav	<- GWT implementation of XPP.
│
│  │  ├─ res/		<- Any resources used by GWT are copied here
│
│  │  ├─ resources	<- Any resources used by GWT packages
│  │  │	 ├─ com/ustadmobile/port/
│  │  │		├─ gwt/
│  │  │		│  ├─ client/
│  │  │		│     ├─ gwtapp.gwt.xml <- Main GWT App's XML
│  │  │		├─ Core.gwt.xml	<- Core module's gwt xml
│  │  │		├─ Lib.gwt.xml 	<- Lib modules's gwt xml
│
│  │  ├─ webapp/	<- Contains Website assets(feeds, images, etc) copied from source package
│  │  │	 ├─ com/ustadmobile/core/
│  │  │	 │  ├─ <Files> <- Copied from src/main/assets/com/ustadmobile/core to be accessible.
│  │  │	 ├─ WEB-INF/
│  │  │	 │  ├─ com/ustadmobile/core/
│  │  │	 │  │  ├─ <Files> <-Copied from src/main/assets/com/ustadmobile/core to be accessible.    
│  │  │	 ├─ index.html	<- If you want this to load; other static html files. 
│  
├─ src-jre/	<- Class override -To implement JRE classes that are not in GWT 
│  ├─ main/java/java/util/concurrent/atomic/AtomicInteger.java 	<- Implemented as normal int. 
│ 
├─ src-json/ <- Class overide to implement JSON class's methods not in GWT
│  ├─ main/java/org/json/ 
│  │  ├─ <Classes>
│ 
├─ src-xpp/ <- Classes override to implement XPP classes and its methods not in GWT
│  ├─ main/java/org/xmlpull/v1/
│  │  ├─ <Classes>
│  
├─ target/	<- Build directory for maven
│  ├─ <Files>
│ 
├─ buildcore.sh	<- The build script. This copies files and renames, etc for GWT to work. Check README for the right setup steps.
├─ pom.xml	<- The project's maven pom xml. 
├─ README.md 	<- This md README
```

#### GWT application explanation ####
All GWT specific source files are in ```src``` folder. All classes are in the ```java``` folder. 
The ```com.ustadmobile.core``` package has factory classes that return GWT implementation bits. 
The ```com.ustadmobile.port.gwt``` package has all GWT related source code. 
The ```com.ustadmobile.port.client``` package has all GWT client classes to make the application run.
The packages and classes outside ```client``` are related to GWT but are not part of running GWT and its components. 

#### Application components ####
Note: This project is using GWTP and GWT-Material (for UI). For more understanding, refer to their turorial: https://dev.arcbees.com/gwtp/tutorials/index.html

All the application components lie in the package ```com.ustadmobile.port.gwt.client.application```. Every page/view has a module associated with it that is declared and linked by the class: ApplicationModule.java. This "installs" the module to the application. 

```
configure(){
	...
	install(new AboutModule());
	...
}
```

Every page/view is in its own package. eg: ```com.ustadmobile.port.gwt.client.application.about```. The package contains of the GWT Presenter, UI Handler, GWT View and GWT Module. 

```
├─ com.ustadmobile.port.gwt.client.application
│  ├─ ...
│  ├─ about/
│  │  ├─ AboutModule.java
│  │  ├─ AboutPresenter.java
│  │  ├─ AboutUiHandlers.java
│  │  ├─ AboutView.java
│  ├─ ApplicationModule.java
│  ├─ ApplicationPresenter.java
│  ├─ ApplicationView.java
```

##### Module #####
All modules declared in the ApplicationModule.java class' configure() are sub-modules to it. A Module here is a GIN module. A GIN module, made by Google, is used by GWTP for dependency injection. A GIN Module should extend ```AbstractPresenterModule``` in any GWTP application. 

In all modules and sub-modules you bind the presenter, view to the module using bindPresenter(). For eg: in AboutModule we bind the AboutPresenter and in the main ApplicationModule we bind the ApplicationPresenter class. 

The starting point of this GWT application is from ApplicationModule. Like its sub modules and view, it has a presenter and a view: ApplicationView. 

##### Presenter #####
But we are only declaring the panel in the parent and no content yet. We aren't telling GWT to go to any particular page because we are internally handling that in UstadMobileSystemImpl.startUI(). This is similar across all ports of UstadMobile. So we are declaring an instance of UstadMobileSystemImpl in the main ApplicationPresenter and calling startUI(). 

The main ApplicationPresenter class has the logic for everything to do with GWT application start. For now it has some methods to go to different pages of the application and most importantly initiates UstadMobileSystemImpl. 

The presenters for the individual views in their packages are child Presenters here. For eg: AboutPresenter is the child presenter of Application Presenter. It uses its parent presenter (ApplicationPresenter)'s slot to reveal itself. 
 
 ###### What does a GWT Presenter do ? ######
 
 The way to link a Core Presenter to this GWT Presenter is to create a new CorePresenterHandler class (inside the GWT Presenter) that extends the Core Presenter and implements any UiHandlers. In this nested class's constructor we call super with the context and view passed to it. 
 
``` 
 public class CoreAboutPresenterHandler 
 	extends com.ustadmobile.core.controller.AboutController	
		implements UiHandlers {
			public CoreAboutPresenterHandler(Object context, AboutView view) {
				super(context, view);
				// TODO Auto-generated constructor stub
				GWT.log("CoreAboutPresenter constructor. TODO.");
		}
 } 
```
 
 This CorePresenterHandler is used to create a ```mController``` object within the GWT Presenter. 
 
 ```mController = new CorePresenterHandler(placeManager, (AboutView)view); ```
 
 This Core presenter (or mController) can be used elsewhere throughout the presenter. We choose to use the placeManager as the context that will get passed around as state. Place managers work as an intermediary between the GWT History API and ProxyPlaceAbstract. It sets up event listener relationships to synchronize them.

The view object is an interface created in the Application Presenter. Every Presenter has two interfaces - MyView and MyProxy. MyView extends GWTP View,  extend any other View and in our case extends our core View.

```
    //About Presenter's View Interface
    interface MyView extends 
    	View, 
		HasUiHandlers<CoreAboutPresenterHandler>, 
		com.ustadmobile.core.view.AboutView {
    }
```

So with that laid out, we have a way to create a new page, load it to the application. Bind its presenter and view. Link Core Presenter with the GWT Presenter and link Core View to GWT View as well. 

Any update to the Core View will update the GWT View. 

##### View #####
A View in GWT is bound to a corresponding view_name.ui.xml file in the same package. A UI XML contains the widgets and we use GWT-Material panels and normal HTML Panels. Below is a simple UI XML.

```
<ui:UiBinder xmlns:ui='urn:ui:com.google.gwt.uibinder'
             xmlns:g='urn:import:com.google.gwt.user.client.ui'>    
    <g:HTMLPanel>
    	<p>Hello AboutView!</p>
        <g:TextBox ui:field="textBox"/><br/>
        <g:TextArea ui:field="version"/><br/>
        <g:Button ui:field="updateText" text="Update Text"/>
    </g:HTMLPanel>
    
</ui:UiBinder>
```

This is bound by this line of code in AboutView.java:

```
//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
interface Binder extends UiBinder<Widget, AboutView> {
}
```

A GWT-Material panel can be used like so (in ApplicationView.ui.xml):

```
<m:MaterialPanel ui:field="content">
  <m:MaterialLabel>Content Material Label</m:MaterialLabel>
</m:MaterialPanel>
```

Since we want to separate  out classes and ui xmls, they are located in the resources package of src folder. 

```
├─ src/main/resources/com/ustadmobile/port/gwt/client/application
│  ├─ ...
│  ├─ about/
│  │  ├─ AboutView.ui.xml
│  ├─ ApplicationView.ui.xml
```

Since the starting point of this GWT application is ApplicationModule, we can see ApplicationView has a panel content declared (in this case it is a material panel since we are using GWT-Material for UI).

We are also injecting in the view's constructor the content panel in the ApplicationPresenter's slot.

The concept of slots is how UIs are generated. We can have side slot, content slot, etc. Our current design is separating out content in its own panel and the side menu in a different slot. 

Our GWT Views extend the MyView interfaces (that in turn extend from CorePresenterHandler) so every methods of Core View's are overridden. If a Core presenter is updating a core view's text field for example, it will update GWT Views and so on. 

##### Other parts of GWTP #####
There are other integral part of this project that are so that this GWTP application can run okay.

```
├─ com.ustadmobile.port.gwt.client
│  ├─ ...
│  ├─ gin/
│  │  ├─ ClientModule.java
│  ├─ place/
│  │  ├─ NameTokens.java
│  ├─ resources/
│  │  ├─ AppResources.java
│  │  ├─ ResourceLoader.java
```

###### NameTokens.java ######
This is a simple class that mapps literal String names to places. 

```
public class NameTokens {
	...
    public static final String ABOUT = AboutView.VIEW_NAME;
    ...
}
```

###### ClientModule.java ######
This is your main GIN module from which all of the child modules are loaded. It is also where the DefaultPlaceManager is setup. In GWTP, this is the very start of the application. 

##### Database & Repository #####

All the GWT DB components lie in the sub package ```db```. 

##### GWT Implementation of Core #####
All the implementations specific to GWT lie in the ```impl``` sub package, etc.

##### Overall Flow of the Application #####
![OPDS Catalog](images/UstadMobileGWT03.PNG "Application Flow")


#### Testing on GWT ####
Since the presenters are being unit tested (which are part of core) seperate to GWT, we are focusing on GWT's implementation / GWT specific unit tests. The major chunk of testing will be functional testing the web server. 

The most recommended way forward is by the use of Selenium and WebDriver via page objects. 

