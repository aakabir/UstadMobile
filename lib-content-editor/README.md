  
# Ustadmobile Content Editor (UmContentEditor)    
 TinyMCE based content editor with the ability to use custom html content templates.  Right now we support two question based template which are multiple choice and fill in the blanks.    
## Getting Started 
You can use this repo as stand-alone project out of Ustadmobile app (use on web apps),     
with Ustadmobile app this repo will be used as support lib since editor controls are implemented on native android.     
    
### Setup  
All plugins that will be used for testing requires Node v8.0.0 or higher. Node installation guide can be found on their official website.  
#### mocha-chrome <br/>  
Plugin which will be used to run all our tests  
```    
 npm install -g mocha-chrome --save-dev  
```

### Prerequisites 
Make sure you install chrome before running any tests using mocha-chrome.    
    
### Using the repo 
#### As stand-alone lib (on web apps) * Clone this repo to your local machine, locate umeditor directory under libraries.     
* Create html file i.e index.html and import all necessary libraries.    
    
``` html 
<link rel="stylesheet" href="lib/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="lib/umeditor/src/css/UmEditorCore.css"> 
<script src="lib/umeditor/src/js/UmQuestionWidget.js" type="text/javascript"></script>
<script src="lib/jquery/jquery3.3.1.min.js" type="text/javascript"></script> 
<script src="lib/rangy/js/rangy-core.js" type="text/javascript"></script>
<script src="lib/tinymce/js/tinymce.min.js" type="text/javascript"></script> 
<script src="lib/umeditor/src/js/UmContentEditorCore.js" type="text/javascript"></script> 
```

* Set listeners when the page is loaded    
```javascript    
 window.onload = function() {    
    UmQuestionWidget.handleWidgetListeners();    
 };  
``` 
* Initialize the editor by calling this method and pass TRUE if you need default tinymce toolbar to be shown..    

```javascript    
UmContentEditorCore.initEditor(true); 
```
    
 #### As android support lib (android) 
 See how we used it to implement our editor on dev-content-editor branch, use ContentEditorActivity.java to understand the logical flow    
    
## Running the tests    
 You can quickly run tests on web browser or using mocha-chrome.<br/><br/>  
<b> Run on web browser</b> <br/>  
Run the following files located under tests directory on your browser  
  
```  
1. content-formatting-tests.html
  
2. content-template-tests.html  
  
3. content-key-down.html
  
4. content-language-locale-tests.html
  
```   
  <b> Run with mocha-chrome - Terminal</b><br/>  
Navigate to test directory and run the following command on your terminal<br/><br/>  
  
``` mocha-chrome content-formatting-tests.html --timeout 6000  
  
mocha-chrome content-template-tests.html --timeout 6000  
  
mocha-chrome content-key-down.html --timeout 6000  
``` 
<br/>  
  
<b> Run with gradle</b>  
  
```   
 ./gradlew :lib-content-editor:test  
```    
 ## Adding custom template
  You may easily create your own html content template and add it to the template directory,   with its functionality implemented on both  UmContentEditorCore.js and  UmQuestionWidget.js located on src directory.
