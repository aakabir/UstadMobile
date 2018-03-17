##UstadMobile GWT project

This branch : gwtdev2 is the GWT branch of UstadMobile application. Here resides all GWT components - code and assets for the project. 

####Why GWT?
GWT is Google's Web Toolkit. It allows us to use the same underlining java code used on android/j2me on the web. This project allows to get an app like experiecne on the web. eg: login, browse opds, view content, access statistical data (if your role allows it), etc. In addition functions that typically start on the web such as user and group management can be made available to all modern platforms of UstadMobile under one core code base. 
GWT would still need to have its own views, system implementation of some platform specific tasks such as xml parsing, HTTP request, replacement for files, etc. 
GWT would also need to be visually scalable within different device types.

We've choosen to use this structure for UstadMobile GWT:
GWTP uiBinders for Views and GWT-Material for style and consistency with Material Design layouts on Android.

####Screenshots of the application and progress:
	<Screnshots>

####Project folder:
All GWT related code and assets live in the app-gwt folder. This is also the folder for the eclipse project. Since GWTP uses Maven, app-gwt is also a maven project and can be built frm the command line. 

####Building:
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


