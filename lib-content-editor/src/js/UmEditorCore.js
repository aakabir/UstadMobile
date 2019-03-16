/**
 * This is a class which handles all core functionality of the editor, like preventing immutable content and 
 * other protected element from being delete from th editor, handles where to focus next when event happens on protected
 * elements ,handles editor state like Initializing it and formatting the content inside it.
 * 
 * @returns {*|UmEditorCore}
 * @constructor
 *
 * @author kileha3
 */
let UmEditorCore = function() {};

/**Editor configurations */
UmEditorCore.editorConfig = null;

/** Flag to keep tract of the environment used when running the app */
let testEnvironment = false;

/** Current document directionality */
let directionality = "ltr";

/**  Language locate path */
const localeDir = "locale/";

/**  Template location path*/
const questionTemplatesDir = "templates/";

/**
 * Index of the multiple choice question document template in the template list
 * @type {number} index
 */
const indexMultipleChoiceWidget = 0;

/**
 * Index of the fill in the blank document template in the template list
 * @type {number} index
 */
const indexFillTheBlanksWidget = 1;

/**
 * Longer time used by editor to change values. i.e Longer operation like observer callbacks
 */
const longerEventTimeout = 500;

/**
 * Shorter time used by editor to change values. i.e Cursor movement.
 */
const shorterEventTimeout = 100;

/**
 * Average time used by editor to change values, i.e delete/cut events and cursor movement.
 */
const averageEventTimeout = 300;

/**
 * List of all tinymce formatting commands which will be used by native side.
 * @type {string[]} list of commands
 */
UmEditorCore.formattingCommandList = [
    'Bold','Underline','Italic','Strikethrough','Superscript','Subscript','JustifyCenter','JustifyLeft',
    'JustifyRight','JustifyFull', 'Indent','Outdent','JustifyLeft','JustifyCenter', 'JustifyRight',
    'JustifyFull','InsertUnorderedList','InsertOrderedList','mceDirectionLTR','mceDirectionRTL','FontSize'
];


/** 
 * Configure UmEditor ready for editing / preview actions 
 * @param locale current used locate 
 * @param dir current used language directionality
 * @param showToolbar flag to indicate tinymce inline toolbar visibility
 * @param testEnv flag to indicate environment i.e True Test Environment otherwise Live.
 * */
UmEditorCore.onCreate  = (locale = "en",dir = "ltr" ,showToolbar = false, testEnv = false) => {
     testEnvironment = testEnv;
     directionality = dir;

    //set default language directionality and its corresponding views
    $($.find(".um-editor")).attr("dir",dir);

    if(dir === "rtl"){
        $($.find(".float-right")).removeClass("float-right").addClass("float-left");
    }else{
        $($.find(".float-left")).removeClass("float-left").addClass("float-right");
    }

    /** 
     * Load placeholders based on locale */
    const langCode = UmEditorCore.prototype.getLanguageCodeFromLocale(locale);
    UmWidgetManager.loadPlaceHolderByLanguageCode(langCode, testEnv);

    //config tinymce
    UmEditorCore.editorConfig = {
        selector: '.um-editable',
        directionality: dir,
        height: $(window).height(),
        menubar: showToolbar,
        statusbar: showToolbar,
        inline:true,
        force_br_newlines : false,
        force_p_newlines : true,
        forced_root_block : '',
        plugins: ['directionality','lists'],
        toolbar: showToolbar,
        extended_valid_elements: "span[*],i[*]",
        init_instance_callback: (ed) => {
            //listen for paste event
            ed.on('Paste', e => {
                e.stopPropagation();
                e.preventDefault();
                let clipboardData = e.clipboardData || window.clipboardData;
                const widget = clipboardData.getData('Text');
                if($(widget).hasClass("question")){
                    UmEditorCore.prototype.insertWidgetNode(widget);
                }else{
                    UmEditorCore.insertContentRaw(widget);
                }
            });
        }
    };

    if(showToolbar){
        UmEditorCore.editorConfig.toolbar = ['undo redo | bold italic underline strikethrough superscript subscript | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | fontsizeselect'];
    }

    try{
        UmWidgetManager.handleWidgetListeners();
        UmEditor.onCreate(JSON.stringify({
            action:'onCreate',
            directionality: directionality,
            content:UmEditorCore.base64Encode(true)}
        ));
    }catch(e){
        UmEditorCore.prototype.logUtil("onCreate",e);
    }
};

/** Enable editing mode */
UmEditorCore.enableEditingMode = () => {

    $($.find("[data-um-widget]")).each((index , widget) => {
        UmEditorCore.prototype.processWidgetNode(widget , false);
    });
    UmEditorCore.requestFocusFromElementWithId($($.find(".um-editor")));
    
    setTimeout(() => {
        try{
            const isEditorInitialized = tinyMCE.activeEditor != null;
            UmWidgetManager.handleWidgetListeners(isEditorInitialized);
            
            if(isEditorInitialized){
                //register change observers
                UmEditorCore.prototype.registerObservers();
            }

            UmEditor.onEditingModeOn(JSON.stringify({
                action:'onEditingModeOn',
                directionality: directionality,
                content:UmEditorCore.base64Encode(isEditorInitialized)}
            ));
        }catch (e) {
            UmEditorCore.prototype.logUtil("onEditingModeOn: "+e);
        }
    }, averageEventTimeout);
};

/** Disable editing mode and enable preview mode */
UmEditorCore.disableEditingMode = () => {
    const editorWrapper = $(".um-editor");
    const previewContent = UmWidgetManager.preparingPreview(editorWrapper.html());
    editorWrapper.html("").html(previewContent);
    UmWidgetManager.handleWidgetListeners(false);
    
};

/**
 * Scroll page to the targeted element in the editor.
 * @param element target element to scroll to
 */
UmEditorCore.prototype.scrollToElement = (element) => {
    if (!!element && element.scrollIntoView) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center'});
    }
};


/** Request focus to the widget with its ID */
UmEditorCore.requestFocusFromElementWithId = (element) => {
    element = $(element).find(".um-editable:first");
    element.click();

    setTimeout(() => {
        tinyMCE.activeEditor.execCommand('mceFocus',false, element.attr("id"));
    }, averageEventTimeout);
};

/** Encode non latin text like arabic and language's text like that, bto itself fails to do that */
UmEditorCore.base64Encode = (content) => {
    return btoa(encodeURIComponent(content).replace(/%([0-9A-F]{2})/g, function(match, p1) {
        return String.fromCharCode(parseInt(p1, 16));
    }));
};

/** Decode non latin text like arabic and language's text like that, bto itself fails to do that */
UmEditorCore.base64Decode = (content) =>{
    return decodeURIComponent(Array.prototype.map.call(atob(content), function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
};


/** 
 * Get language locale code i.e if ar_EA then the code will be only ar for Arabic 
 * @returns active language code currently used.
 * */
UmEditorCore.prototype.getLanguageCodeFromLocale = (locale) => {
    let languageCode = locale.split("_");
    if(languageCode.length > 0){
        languageCode = languageCode[0];
    }
    return languageCode;
};

/** Handle observers that watches for any changes on currently editable page */
UmEditorCore.prototype.registerObservers = () => {
     try{
        //add content change observer
        let contentWatcherFilters = {
            attributes: true, characterData: true, childList: true, subtree: true,
            attributeOldValue: true, characterDataOldValue: true
        };

        const contentChangeObserver = new MutationObserver(() => {
            //Check editor controls state
            setTimeout(() => {UmEditorCore.prototype.checkActivatedControls()},averageEventTimeout);
        
            //generate preview content
            const previewContent = JSON.stringify({
                action:'onSaveContent',
                directionality: directionality,
                content: UmEditorCore.base64Encode(UmWidgetManager.preparingPreview($(".um-editor").html())) 
            });

            try{
                UmEditor.onSaveContent(previewContent);
            }catch (e) {
                UmEditorCore.prototype.logUtil("onContentChanged:",e);
            }


        });
        contentChangeObserver.observe(document.querySelector('.um-editor'),contentWatcherFilters);

     }catch(e){
         UmEditorCore.prototype.logUtil("registerObservers", e);
     }
};

/**
 * Tinymce command to insert content in the editor
 * @param content content to be inserted in the editor.
 */
UmEditorCore.insertContentRaw = (content) => {
    tinyMCE.activeEditor.execCommand('mceInsertContent', false, content);
};

/** 
 * Set cursor to specific element in the editor
 * @param element target element to focus
 * @param isRoot flag to indicate if target is root element.
  */
UmEditorCore.prototype.setCursor = (element = null,isRoot) =>{
   if(isRoot){
       UmEditorCore.prototype.setCursorPositionToLastEditableElement(element);
   }else{
       UmEditorCore.prototype.setCursorToAnyEditableElement(element);
   }
};

/**
 * Set cursor to the last non protected and focusable element in the editor.
 * @param rootElement editor root element.
 */
UmEditorCore.prototype.setCursorPositionToLastEditableElement = (rootElement) =>{
    if(rootElement == null){
        rootElement = document.getElementsByClassName("um-editor");
    }
    const range = document.createRange();
    const selection = window.getSelection();
    range.selectNodeContents(rootElement);
    range.collapse(false);
    selection.removeAllRanges();
    selection.addRange(range);
    rootElement.focus();
    range.detach();
    rootElement.scrollTop = rootElement.scrollHeight;
    if($(document).height() > $(window).height()){
        $("html, body").animate({ scrollTop: $(document).height()-$(window).height()});
    }
};

/**
 * Set cursor to a specific editor node
 * @param element target element cursor to be set on.
 */
UmEditorCore.prototype.setCursorToAnyEditableElement = (element) => {
    try{
        element = element === null ? $(".um-editor").children().get(0):element;
        const range = document.createRange();
        const sel = window.getSelection();
        range.setStart(element, 0);
        range.collapse(false);
        sel.removeAllRanges();
        sel.addRange(range);
        element.focus();
    }catch (e) {
        UmEditorCore.prototype.logUtil("setCursorToAnyEditableElement",e)
    }
 };

UmEditorCore.prototype.logUtil = (tag , message, debug = true) => {
   if(debug){
       console.log(tag, message);
   }
};

/** 
 * Insert link on selected text 
 * @param linkUrl url to be used as a link
 * @param linkText Text to be used as a link holder
 * @param selection flag to indicate if link is being inserted after text selection.
 * */
UmEditorCore.insertLink = (linkUrl, linkText = null, selection = true) =>{
    if(selection){
        const currentNode = tinyMCE.activeEditor.selection.getNode();
        if($(currentNode).is("a")){
            $(currentNode).text(linkText);
            $(currentNode).attr("href", linkUrl);
        }else{
            var highlight = window.getSelection(); 
            text = $(currentNode).text(),
            range = highlight.getRangeAt(0),
            startText = text.substring(0, range.startOffset), 
            endText = text.substring(range.endOffset, text.length);
            
            $(currentNode).html(startText + UmEditorCore.prototype.getStyledLink(linkUrl,linkText) + endText);
        }
    }else{
        UmEditorCore.insertContentRaw(UmEditorCore.prototype.getStyledLink(linkUrl, linkText));
    }
};

UmEditorCore.prototype.getStyledLink = (link, text) =>{
    return '<span class="um-link"><a href="' + link + '" target="_blank">' + text + '</a></span>&nbsp;';
};

/** Remove link associated with the current selection */
UmEditorCore.removeLink = () =>{
    const currentNode = tinyMCE.activeEditor.selection.getNode();  
    if($(currentNode).is("span") || $(currentNode).is("a")){
        text = $(currentNode).text();
        $(currentNode).replaceWith(text);
    }else{
        text = $(currentNode).find("span").text();
        $(currentNode).find("span").replaceWith(text);
    }
};


/**
 * Insert media content in the editor.
 * @param source file source path relative to the tinymce editor.
 * @param mimeType file mimetype.
 */
UmEditorCore.insertMediaContent = (source, mimeType) => {
    let mediaContent = null;
    if(mimeType.includes("image")){
        mediaContent = "<img src='" +source+"' class='.img-fluid'/>";
    }else if(mimeType.includes("audio")){
        mediaContent = '<audio controls controlsList="nodownload" class="media-audio"><source src="'+source+'" type="'+mimeType+'"></audio>';

    }else{
        mediaContent = '<video controls controlsList="nodownload" preload="meta" class="media-video"><source src="'+source+'" type="'+mimeType+'"></video>';
    }
    mediaContent = mediaContent + "<p><br/></p>";
    let currentElement = $(tinymce.activeEditor.selection.getNode());
    if(currentElement.is("p")){
        $(currentElement).after(mediaContent);
    }else{
        UmEditorCore.insertContentRaw(mediaContent)
    }

    const parentChildren = $($(currentElement.parent()).children()).last().get(0);
    UmEditorCore.prototype.setCursor(parentChildren,false);
    UmEditorCore.prototype.scrollToElement(parentChildren);

};

/** Insert multiple choice widget template into the document */
UmEditorCore.insertMultipleChoiceWidget = () => {
    UmEditorCore.prototype.insertWidgetTemplate(indexMultipleChoiceWidget, testEnvironment);
};

/** Insert fill in the blanks widget template into the document */
UmEditorCore.insertFillTheBlanksWidget = () => {
    UmEditorCore.prototype.insertWidgetTemplate(indexFillTheBlanksWidget, testEnvironment);
};

/**
 * Prepare and Insert question template on the editor
 * @param questionTypeIndex index of the question type in the list.
 * @param isTest Flag to indicate if executed from test environment
 */
UmEditorCore.prototype.insertWidgetTemplate = (questionTypeIndex,isTest = false) => {
    const questionTemplateList = ['template-qn-multiple-choice.html','template-qn-fill-the-blanks.html'];
    const nextQuestionId = UmWidgetManager.QUESTION_ID_TAG + UmWidgetManager.getNextUniqueId();
    const nextChoiceId = UmWidgetManager.CHOICE_ID_TAG + UmWidgetManager.getNextUniqueId();
    const templateUrl = (isTest ? "/":"") + questionTemplatesDir + questionTemplateList[questionTypeIndex];
    $.ajax({url: templateUrl, success: (templateHtmlContent) => {
            let widgetNode = $(templateHtmlContent).attr("id",nextQuestionId);
            $(widgetNode).find(".question-choice").attr("id",nextChoiceId);
            widgetNode = $(widgetNode).prop('outerHTML');
            UmEditorCore.prototype.insertWidgetNode($(widgetNode).get(0));
        }});
};

/** Process widget node from WidgetManager */
UmEditorCore.prototype.processWidgetNode = (widgetNode , newWidget) => {
    if(!$(widgetNode).attr("id")){
        $(widgetNode).attr("id", UmWidgetManager.EXTRA_CONTENT_ID_TAG + UmWidgetManager.getNextUniqueId())
    }
    
    const widgetManager = UmWidgetManager.handleWidgetNode(widgetNode, newWidget);
    widgetManager.switchEditingModeOn();
    return widgetManager.widget;
};


/**
 * Handle question node when inserted/modified to the editor (This will be called on editor preInit)
 * @param widgetNode Question html content
 * @param isFromClipboard False when the node was inserted from template else will
 * be coming from clipboard.
 */
UmEditorCore.prototype.insertWidgetNode = (widgetNode) => {
    const activeNode = tinyMCE.activeEditor.selection.getNode();
    const closestExtraContent = $(activeNode).closest("div div.extra-content");
    const closestQuestion = $(activeNode).closest("div div.question");
    const isInQuestion = $(closestQuestion).is("div");
    const isInExtraContent = $(closestExtraContent).is("div");

    let currentWidget = isInQuestion ? closestQuestion : (isInExtraContent ? closestExtraContent : null);

    if(isInExtraContent){
        currentWidget = closestExtraContent;
    }else{
        if(isInQuestion){
            let found = false;
            $($.find("[data-um-widget]")).each((index , widget) => {
                if(found){
                   currentWidget = widget;
                    return false;
                }
                found = $(closestQuestion).attr("id") == $(widget).attr("id");
            });
        }
    }
    //add template to the document
    widgetNode = UmEditorCore.prototype.processWidgetNode(widgetNode, true);
    $(currentWidget).after(widgetNode);

    //add extra content widget after the question
    const extraContent = $(UmWidgetManager.EXTRA_CONTENT_WIDGET);
    extraContent.attr("id",UmWidgetManager.EXTRA_CONTENT_ID_TAG + UmWidgetManager.getNextUniqueId());
    $(widgetNode).after(extraContent.get(0));
    $(".um-editor").find("p.pg-break").remove();

    UmWidgetManager.handleEditableContent(true);
    UmEditorCore.requestFocusFromElementWithId($(widgetNode).get(0));
    UmEditorCore.prototype.scrollToElement($($(UmWidgetManager.EXTRA_CONTENT_WIDGET).find("p")).get(0))
    
};

/**
 * Check tinymce toolbar menu controls state.
 */
UmEditorCore.prototype.checkActivatedControls = () => {
    const commandStatus = [];
    for(let command in UmEditorCore.formattingCommandList){
        if(!UmEditorCore.formattingCommandList.hasOwnProperty(command))
          continue;
          const commandString = UmEditorCore.formattingCommandList[command];
          const commandState = {};
          let status = null;
          if(commandString === "FontSize"){
              status = tinyMCE.activeEditor.queryCommandValue(commandString).replace("px","");
          }else if(commandString ==="mceDirectionLTR"){
              status = UmEditorCore.prototype.getNodeDirectionality() === "ltr";
          }else if(commandString ==="mceDirectionRTL"){
              status = UmEditorCore.prototype.getNodeDirectionality() === "rtl";
          }else{
              status = UmEditorCore.prototype.checkCommandState(commandString);
          }
          commandState.command = commandString;
          commandState.status = status === null ? false : status;
          commandStatus.push(commandState);
    }

    try{
        UmEditor.onControlsStateChanged(JSON.stringify({
            action:'onActiveControlCheck',
            directionality:'',
            content:btoa(JSON.stringify(commandStatus))}));
    }catch(e){
        UmEditorCore.prototype.logUtil("checkActivatedControls",e);
    }
};

/**
 * Check if a toolbar button is active or not
 * @param commandIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
UmEditorCore.prototype.checkCommandState = (commandIdentifier) => {
    return tinyMCE.activeEditor.queryCommandState(commandIdentifier);
};

/**
 * Check if the control was executed at least once.
 * @param commandIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
UmEditorCore.prototype.checkCommandValue = (commandIdentifier) => {
    return tinyMCE.activeEditor.queryCommandValue(commandIdentifier) != null;
};

/**
 * Change editor font size
 * @param fontSize font size to change to
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.setFontSize = (fontSize) => {
    UmEditorCore.executeCommand("FontSize",""+fontSize+"pt");
    const activeFont = tinyMCE.activeEditor.queryCommandValue("FontSize");
    const isActive = UmEditorCore.prototype.checkCommandState("FontSize");
    return {action:'activeControl',content:btoa("FontSize-" + isActive + "-" + activeFont)};
};

/**
 * Undo previously performed action
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.editorActionUndo = () => {
    UmEditorCore.executeCommand("Undo",null);
    UmEditorCore.prototype.checkCommandState("Undo");
};

/**
 * Redo previously performed action
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.editorActionRedo = () => {
    UmEditorCore.executeCommand("Redo",null);
    UmEditorCore.prototype.checkCommandState("Redo");
};

/**
 * Set text direction from Left to Right
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textDirectionLeftToRight = () => {
    UmEditorCore.executeCommand('mceDirectionLTR');
    const isActive = UmEditorCore.prototype.getNodeDirectionality() === "ltr";
    return {action:'activeControl',content:btoa("mceDirectionLTR-" + isActive)};
};

/**
 * Set text direction from Right to Left
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textDirectionRightToLeft = () => {
    UmEditorCore.executeCommand('mceDirectionRTL');
    const isActive = UmEditorCore.prototype.getNodeDirectionality() === "rtl";
    return {action:'activeControl',content:btoa("mceDirectionRTL-" + isActive)};
};

/**
 * Remove or insert un-ordered list
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphUnOrderedListFormatting = () => {
    UmEditorCore.executeCommand("InsertUnorderedList",null);
    const isActive = UmEditorCore.prototype.checkCommandState("InsertUnorderedList");
    return {action:'activeControl',content:btoa("InsertUnorderedList-" + isActive)};
};

/**
 * Remove or insert ordered list
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphOrderedListFormatting = () => {
    UmEditorCore.executeCommand("InsertOrderedList",null);
    const isActive = UmEditorCore.prototype.checkCommandState("InsertOrderedList");
    return {action:'activeControl',content:btoa("InsertOrderedList-" + isActive)};
};

/**
 * Justify editor content to the left
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphLeftJustification = () => {
    UmEditorCore.executeCommand("JustifyLeft",null);
    const isActive = UmEditorCore.prototype.checkCommandValue("JustifyLeft");
    return {action:'activeControl',content:btoa("JustifyLeft-" + isActive)};
};

/**
 * Justify editor content to the right.
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphRightJustification = () => {
    UmEditorCore.executeCommand("JustifyRight",null);
    const isActive = UmEditorCore.prototype.checkCommandValue("JustifyRight");
    return {action:'activeControl',content:btoa("JustifyRight-" + isActive)};
};

/**
 * Justify content editor fully
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphFullJustification = () => {
    UmEditorCore.executeCommand("JustifyFull",null);
    const isActive = UmEditorCore.prototype.checkCommandValue("JustifyFull");
    return {action:'activeControl',content:btoa("JustifyFull-" + isActive)};
};

/**
 * Justify editor content at the center
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphCenterJustification = () => {
    UmEditorCore.executeCommand("JustifyCenter",null);
    const isActive = UmEditorCore.prototype.checkCommandValue("JustifyCenter");
    return {action:'activeControl',content:btoa("JustifyCenter-" + isActive)};
};

/**
 * Indent editor content
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphOutDent = () => {
    UmEditorCore.executeCommand("Outdent",null);
    const isActive = UmEditorCore.prototype.checkCommandValue("Outdent");
    return {action:'activeControl',content:btoa("Outdent-" + isActive)};
};

/**
 * Indent editor content
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.paragraphIndent = () => {
    UmEditorCore.executeCommand("Indent",null);
    const isActive = UmEditorCore.prototype.checkCommandValue("Indent");
    return {action:'activeControl',content:btoa("Indent-" + isActive)};
};

/**
 * Apply bold format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textFormattingBold = () => {
    UmEditorCore.executeCommand("Bold",null);
    const isActive = UmEditorCore.prototype.checkCommandState("Bold");
    return {action:'activeControl',content:btoa("Bold-" + isActive)};
};

/**
 * Apply italic format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textFormattingItalic = () => {
    UmEditorCore.executeCommand("Italic",null);
    const isActive = UmEditorCore.prototype.checkCommandState("Italic");
    return {action:'activeControl',content:btoa("Italic-" + isActive)};
};

/**
 * Apply underline format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textFormattingUnderline = () => {
    UmEditorCore.executeCommand("Underline",null);
    const isActive = UmEditorCore.prototype.checkCommandState("Underline");
    return {action:'activeControl',content:btoa("Underline-" + isActive)};
};

/**
 * Apply strike-through format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textFormattingStrikeThrough = () => {
    UmEditorCore.executeCommand("Strikethrough",null);
    const isActive = UmEditorCore.prototype.checkCommandState("Strikethrough");
    return {action:'activeControl',content:btoa("Strikethrough-" + isActive)};
};

/**
 * Apply superscript format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textFormattingSuperScript = () => {
    UmEditorCore.executeCommand("Superscript",null);
    const isActive = UmEditorCore.prototype.checkCommandState("Superscript");
    return {action:'activeControl',content:btoa("Superscript-" + isActive)};
};

/**
 * Apply subscript format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.textFormattingSubScript = () => {
    UmEditorCore.executeCommand("Subscript",null);
    const isActive = UmEditorCore.prototype.checkCommandState("Subscript");
    return {action:'activeControl',content:btoa("Subscript-" + isActive)};
};

/**
 * Check if the current selected editor node has controls activated to it
 * @param commandValue control to check from
 * @returns {{action: string, content: string}} callback object
 */
UmEditorCore.checkCurrentActiveControls = (commandValue) => {
    const isActive = UmEditorCore.prototype.checkCommandState(commandValue);
    return {action:'activeControl',content:btoa(commandValue+"-" + isActive)};
};

/**
 * Execute normal formatting commands
 * @param command command to be executed
 * @param args extra value to be passed on eg. font size
 */
UmEditorCore.executeCommand = (command, args) => {
    try{
        tinyMCE.activeEditor.execCommand(command, false,args);
    }catch(e){
        UmEditorCore.prototype.logUtil("executeCommand: "+e);
    }
};


/**
 * Get current node directionality, if it was not set then it should inherit parent's directionality
 * @returns {string} directionality tag.
 */
UmEditorCore.prototype.getNodeDirectionality = () => {
    try{
        if(tinyMCE.activeEditor != null){
            const currentNode = $(tinyMCE.activeEditor.selection.getNode());
            return getComputedStyle(currentNode.get(0)).direction;
        }
    }catch(e){
        UmEditorCore.prototype.logUtil("getNodeDirectionality ", e);
    }
    return $($.find(".um-editor")).attr("dir");
};

/**
 * Get content editor content (For testing)
 */
UmEditorCore.getContent = () => {
    return tinyMCE.activeEditor.getContent();
};

/**Get highlighted text */
UmEditorCore.getLinkProperties = ()=>{
    try{
        const currentNode = tinyMCE.activeEditor.selection.getNode();
        let selectedText = "";
        let selectedLink = "";
        const highlight = window.getSelection();
        if((highlight + "").length > 0){
            selectedText = highlight + "";

        }else{
            if($(currentNode).is("a")){
                const aTag = $(currentNode);
                selectedText = aTag.text();
                selectedLink = aTag.attr("href");
            }
        }
        const linkObj = {linkText:selectedText,linkUrl:selectedLink};
        console.log("kileha",linkObj);
        UmEditor.onLinkPropRequested(JSON.stringify({
            action:'onLinkPropRequested',
            directionality: directionality,
            content:UmEditorCore.base64Encode(JSON.stringify(linkObj))}
        ));
    }catch(e){
        UmEditorCore.prototype.logUtil("SelectionChange",e);
    }
}