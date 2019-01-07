/**
 * Class which handle all content editor operation
 * @returns {*|UmContentEditorCore}
 * @constructor
 *
 * @author kileha3
 */
let UmContentEditorCore = function() {};

/**
 * Template location path
 * @type {string} path
 */
const questionTemplatesDir = "templates/";

/**
 * Language locate path
 */
const languageLocaleDir = "locale/";

/**
 * Index of the multiple choice question document template in the template list
 * @type {number} index
 */
const indexMultipleChoiceQuestionType = 0;

/**
 * Index of the fill in the blank document template in the template list
 * @type {number} index
 */
const indexFillTheBlanksQuestionType = 1;

/**
 * Flag which shows if by any chance content was selected
 * @type {boolean} True when selection was performed otherwise false.
 */
let wasContentSelected = false;

/**
 * Flag which hold a value when content get selected and it includes protected content
 * @type {boolean} True if
 */
let isProtectedElement = false;


/**
 * List of all tinymce formatting commands which will be used by native side.
 * @type {string[]} list of commands
 */
UmContentEditorCore.formattingCommandList = [
    'Bold','Underline','Italic','Strikethrough','Superscript','Subscript','JustifyCenter','JustifyLeft',
    'JustifyRight','JustifyFull', 'Indent','Outdent','JustifyLeft','JustifyCenter', 'JustifyRight',
    'JustifyFull','InsertUnorderedList','InsertOrderedList','mceDirectionLTR','mceDirectionRTL','FontSize'
];

UmContentEditorCore.sectionType = {
  sectionBody:1,
  sectionFeedback:2
};

/**
 * Check if a toolbar button is active or not
 * @param commandIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
UmContentEditorCore.prototype.checkCommandState = (commandIdentifier) => {
    return tinyMCE.activeEditor.queryCommandState(commandIdentifier);
};


/**
 * Check if the control was executed at least once.
 * @param commandIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
UmContentEditorCore.prototype.checkCommandValue = (commandIdentifier) => {
    return tinyMCE.activeEditor.queryCommandValue(commandIdentifier) != null;
};


/**
 * Change editor font size
 * @param fontSize font size to change to
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.setFontSize = (fontSize) => {
    UmContentEditorCore.executeCommand("FontSize",""+fontSize+"pt");
    const activeFont = tinyMCE.activeEditor.queryCommandValue("FontSize");
    const isActive = UmContentEditorCore.prototype.checkCommandState("FontSize");
    return {action:'activeControl',content:btoa("FontSize-"+isActive+"-"+activeFont)};
};

/**
 * Undo previously performed action
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.editorActionUndo = () => {
    UmContentEditorCore.executeCommand("Undo",null);
    UmContentEditorCore.prototype.checkCommandState("Undo");
};


/**
 * Redo previously performed action
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.editorActionRedo = () => {
    UmContentEditorCore.executeCommand("Redo",null);
    UmContentEditorCore.prototype.checkCommandState("Redo");
};

/**
 * Set text direction from Left to Right
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textDirectionLeftToRight = () => {
    UmContentEditorCore.executeCommand('mceDirectionLTR');
    const isActive =UmContentEditorCore.prototype.getNodeDirectionality() === "ltr";
    return {action:'activeControl',content:btoa("mceDirectionLTR-"+isActive)};
};

/**
 * Set text direction from Right to Left
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textDirectionRightToLeft = () => {
    UmContentEditorCore.executeCommand('mceDirectionRTL');
    const isActive = UmContentEditorCore.prototype.getNodeDirectionality() === "rtl";
    return {action:'activeControl',content:btoa("mceDirectionRTL-"+isActive)};
};

/**
 * Remove or insert un-ordered list
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphUnOrderedListFormatting = () => {
    UmContentEditorCore.executeCommand("InsertUnorderedList",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("InsertUnorderedList");
    return {action:'activeControl',content:btoa("InsertUnorderedList-"+isActive)};
};

/**
 * Remove or insert ordered list
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphOrderedListFormatting = () => {
    UmContentEditorCore.executeCommand("InsertOrderedList",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("InsertOrderedList");
    return {action:'activeControl',content:btoa("InsertOrderedList-"+isActive)};
};

/**
 * Justify editor content to the left
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphLeftJustification = () => {
    UmContentEditorCore.executeCommand("JustifyLeft",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyLeft");
    return {action:'activeControl',content:btoa("JustifyLeft-"+isActive)};
};

/**
 * Justify editor content to the right.
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphRightJustification = () => {
    UmContentEditorCore.executeCommand("JustifyRight",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyRight");
    return {action:'activeControl',content:btoa("JustifyRight-"+isActive)};
};

/**
 * Justify content editor fully
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphFullJustification = () => {
    UmContentEditorCore.executeCommand("JustifyFull",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyFull");
    return {action:'activeControl',content:btoa("JustifyFull-"+isActive)};
};

/**
 * Justify editor content at the center
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphCenterJustification = () => {
    UmContentEditorCore.executeCommand("JustifyCenter",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("JustifyCenter");
    return {action:'activeControl',content:btoa("JustifyCenter-"+isActive)};
};

/**
 * Indent editor content
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphOutDent = () => {
    UmContentEditorCore.executeCommand("Outdent",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("Outdent");
    return {action:'activeControl',content:btoa("Outdent-"+isActive)};
};

/**
 * Indent editor content
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.paragraphIndent = () => {
    UmContentEditorCore.executeCommand("Indent",null);
    const isActive = UmContentEditorCore.prototype.checkCommandValue("Indent");
    return {action:'activeControl',content:btoa("Indent-"+isActive)};
};

/**
 * Apply bold format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingBold = () => {
    UmContentEditorCore.executeCommand("Bold",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Bold");
    return {action:'activeControl',content:btoa("Bold-"+isActive)};
};

/**
 * Apply italic format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingItalic = () => {
    UmContentEditorCore.executeCommand("Italic",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Italic");
    return {action:'activeControl',content:btoa("Italic-"+isActive)};
};

/**
 * Apply underline format to text on the editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingUnderline = () => {
    UmContentEditorCore.executeCommand("Underline",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Underline");
    return {action:'activeControl',content:btoa("Underline-"+isActive)};
};

/**
 * Apply strike-through format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingStrikeThrough = () => {
    UmContentEditorCore.executeCommand("Strikethrough",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Strikethrough");
    return {action:'activeControl',content:btoa("Strikethrough-"+isActive)};
};

/**
 * Apply superscript format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingSuperScript = () => {
    UmContentEditorCore.executeCommand("Superscript",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Superscript");
    return {action:'activeControl',content:btoa("Superscript-"+isActive)};
};

/**
 * Apply subscript format to text on editor
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.textFormattingSubScript = () => {
    UmContentEditorCore.executeCommand("Subscript",null);
    const isActive = UmContentEditorCore.prototype.checkCommandState("Subscript");
    return {action:'activeControl',content:btoa("Subscript-"+isActive)};
};

/**
 * Check if the current selected editor node has controls activated to it
 * @param commandValue control to check from
 * @returns {{action: string, content: string}} callback object
 */
UmContentEditorCore.checkCurrentActiveControls = (commandValue) => {
    const isActive = UmContentEditorCore.prototype.checkCommandState(commandValue);
    return {action:'activeControl',content:btoa(commandValue+"-"+isActive)};
};


/**
 * Insert multiple choice question template to the editor
 */
UmContentEditorCore.insertMultipleChoiceQuestionTemplate =  () =>  {
    UmContentEditorCore.prototype.insertQuestionTemplate(indexMultipleChoiceQuestionType);
};

/**
 * Insert fill in the blanks question template to the editor
 */
UmContentEditorCore.insertFillInTheBlanksQuestionTemplate =  () =>  {
    UmContentEditorCore.prototype.insertQuestionTemplate(indexFillTheBlanksQuestionType);
};

/**
 * Request focus to the tinymce
 * @returns focus object
 */
UmContentEditorCore.prototype.requestFocus =  () =>  {
    UmContentEditorCore.executeCommand("mceFocus",null);
    return UmContentEditorCore.prototype.checkCommandState("mceFocus");
};

/**
 * Select all content in document body
 */
UmContentEditorCore.selectAll =  () => {
    const body = $('body');
    body.on("click",function () {
        tinymce.activeEditor.selection.select(tinymce.activeEditor.getBody(), true);
    });
    body.click();
};

/**
 * Execute normal formatting commands
 * @param command command to be executed
 * @param args extra value to be passed on eg. font size
 */
UmContentEditorCore.executeCommand = (command, args) => {
    try{
        tinyMCE.activeEditor.execCommand(command, false,args);
    }catch(e){
        console.log("executeCommand: "+e);
    }
};


/**
 * Get current node directionality, if it was not set then it should inherit parent's directionality
 * @returns {string} directionality tag.
 */
UmContentEditorCore.prototype.getNodeDirectionality = () => {
    const currentNode = $(tinymce.activeEditor.selection.getNode());
    const parentDirectionality = getComputedStyle(currentNode.parent().get(0)).direction;
    const currentNodeDirectionality = getComputedStyle(currentNode.get(0)).direction;

    if(currentNodeDirectionality !== parentDirectionality){
        return currentNodeDirectionality;
    }
    return parentDirectionality;
};


/**
 * Get current position of the cursor placed on editable content.
 * @returns {number} position at character based index
 */
UmContentEditorCore.prototype.getCursorPositionRelativeToTheEditableElementContent = ()  =>  {
    try{
        if (window.getSelection && window.getSelection().getRangeAt) {
            const range = window.getSelection().getRangeAt(0);
            const selectedObj = window.getSelection();
            let rangeCount = 0;
            let childNodes = selectedObj.anchorNode.parentNode.childNodes;
            for (let i = 0; i < childNodes.length; i++) {
                if (childNodes[i] === selectedObj.anchorNode) {
                    break;
                }
                if (childNodes[i].outerHTML)
                    rangeCount += childNodes[i].outerHTML.length;
                else if (childNodes[i].nodeType === 3) {
                    rangeCount += childNodes[i].textContent.length;
                }
            }
            return range.startOffset + rangeCount;
        }
    }catch (e) {
        console.log("getCursorPositionRelativeToTheEditableElementContent:",e);
    }
    return -1;
};

/**
 * Check tinymce controls state.
 */
UmContentEditorCore.prototype.checkActivatedControls = () => {
    const commandStatus = [];
    for(let command in UmContentEditorCore.formattingCommandList){
        if(!UmContentEditorCore.formattingCommandList.hasOwnProperty(command))
          continue;
          const commandString = UmContentEditorCore.formattingCommandList[command];
          const commandState = {};
          let status = null;
          if(commandString === "FontSize"){
              status = tinyMCE.activeEditor.queryCommandValue(commandString).replace("px","");
          }else if(commandString ==="mceDirectionLTR"){
              status = UmContentEditorCore.prototype.getNodeDirectionality() === "ltr";
          }else if(commandString ==="mceDirectionRTL"){
              status = UmContentEditorCore.prototype.getNodeDirectionality() === "rtl";
          }else{
              status = UmContentEditorCore.prototype.checkCommandState(commandString);
          }
          commandState.command = commandString;
          commandState.status = status === null ? false : status;
          commandStatus.push(commandState);
    }

    try{
        UmContentEditor.onControlsStateChanged(JSON.stringify({action:'onActiveControlCheck',content:btoa(JSON.stringify(commandStatus))}));
    }catch(e){
        console.log(e);
    }
};

/**
 * Prepare and Insert question template on the editor
 * @param questionTypeIndex index of the question type in the list.
 */
UmContentEditorCore.prototype.insertQuestionTemplate = (questionTypeIndex) => {
    const questionTemplateList = ['template-qn-multiple-choice.html','template-qn-fill-the-blanks.html'];
    const nextQuestionId = UmQuestionWidget.QUESTION_ID_TAG+UmQuestionWidget.getNextUniqueId();
    const nextChoiceId = UmQuestionWidget.CHOICE_ID_TAG+UmQuestionWidget.getNextUniqueId();
    $.ajax({url: questionTemplatesDir+questionTemplateList[questionTypeIndex], success: (templateHtmlContent) => {
            let questionNode = $(templateHtmlContent).attr("id",nextQuestionId);
            $(questionNode).find(".question-choice").attr("id",nextChoiceId);
            questionNode = $(questionNode).prop('outerHTML');
            questionNode = $("<div>").append(questionNode).append(UmQuestionWidget.EXTRA_CONTENT_WIDGET);
            questionNode = $(questionNode).html();
            UmContentEditorCore.prototype.insertQuestionNodeContent(questionNode);
        }});
};


/**
 * Get last extra content widget in the content editor
 * @returns content widget element
 */
UmContentEditorCore.prototype.getLastExtraContentWidget = () => {
    const extraContentWidgets = $("#umEditor").find('.extra-content');
    return $($(extraContentWidgets[extraContentWidgets.length - 1]).get(0)).children().get(0);
};

/**
 * Get last editable element in the editor
 * @returns content widget element
 */
UmContentEditorCore.prototype.getLastEditableElement = () =>{
    const editor = $("#umEditor");
    const pageBreaks = editor.find('.pg-break');
    if(pageBreaks.length === 0){
        editor.append(UmQuestionWidget.PAGE_BREAK);
        getLastEditableElement();
    }
    return $(pageBreaks[pageBreaks.length - 1]).get(0);
};


/**
 * Insert media content in the template.
 * @param source media source path.
 * @param mimeType media mimetype.
 */
UmContentEditorCore.insertMediaContent = (source, mimeType) => {
    let mediaContent = null;
    if(mimeType.includes("image")){
        mediaContent = '<img src="'+source+'" class="um-media img-fluid">';
    }else if(mimeType.includes("audio")){
        mediaContent = '<audio controls controlsList="nodownload" class="um-media"><source src="'+source+'" type="'+mimeType+'"></audio>';
    }else{
        mediaContent = '<video controls controlsList="nodownload" class="um-media"><source src="'+source+'" type="'+mimeType+'"></video>'
    }
    mediaContent = mediaContent + '<p></p>';
    const currentElement = $(tinymce.activeEditor.selection.getNode());
    if(currentElement.is("p")){
        $(currentElement).after(mediaContent);
    }else{
        UmContentEditorCore.insertContentRaw(mediaContent)
    }

    const parentChildren = $(currentElement.parent()).children();
    UmContentEditorCore.prototype.setCursor(parentChildren.get(parentChildren.length - 1),false);

};


/**
 * Tinymce command to insert content in the editor
 * @param content content to be inserted in the editor.
 */
UmContentEditorCore.insertContentRaw = (content) =>{
    tinymce.activeEditor.execCommand('mceInsertContent', false, content);
};


/**
 * Handle question node when inserted/modified to the editor (This will be called on editor preInit)
 * @param questionNode Question html content
 * @param isFromClipboard False when the node was inserted from template else will
 * be coming from clipboard.
 */
UmContentEditorCore.prototype.insertQuestionNodeContent = (questionNode,isFromClipboard = false) => {
    UmQuestionWidget.setQuestionStatus(true);
    if(!isFromClipboard){
        tinymce.activeEditor.dom.add(tinymce.activeEditor.getBody(), 'p', {class: 'pg-break',style:'page-break-before: always'}, '');
    }else{
        const activeNode = tinymce.activeEditor.selection.getNode();
        const extraContent = $(activeNode).closest("div div.extra-content");
        $(extraContent).after(UmQuestionWidget.PAGE_BREAK);
    }
    UmContentEditorCore.prototype.setCursorToANode(UmContentEditorCore.prototype.getLastEditableElement());
    UmContentEditorCore.insertContentRaw(questionNode);
    UmContentEditorCore.prototype.scrollToElement(UmContentEditorCore.prototype.getLastEditableElement());
    UmContentEditorCore.prototype.setCursorToANode(UmContentEditorCore.prototype.getLastExtraContentWidget());
    tinymce.activeEditor.dom.remove(tinymce.activeEditor.dom.select('p.pg-break'));
};


/**
 * Check next focusable area when someone click protected area.
 * @param element clicked target element.
 */
UmContentEditorCore.prototype.setFocusToNextFocusableArea = element => {
   try{
       if($(element).is("label") || $(element).is("button") || $(element).hasClass("close")
           || $(element).hasClass("question") || $(element).hasClass("question-retry-option")
           ||  $(element).is("img") || $(element).hasClass("question-retry-holder")
           || $(element).hasClass("input-group") || $(element).hasClass("question-action-holder")){

           const labelText = $(element).text();
           const questionRoot = $(element).closest("div .question");
           const questionElement = $(questionRoot).children();
           const questionType = $(questionRoot).attr("data-um-widget");
           let elementToFocus = null;
           if(labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionBodyText
               || $(element).hasClass("question-action-holder")){
               elementToFocus = questionElement.get(3);
           }else if(labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksPromptInput ||
               labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksAnswerBodyText){
               elementToFocus = $(questionElement.get(5)).children().get(1);
           }else if(labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRightFeedbackText){
               elementToFocus = $(questionElement.get(5)).children().get(3);
           }else if(labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionWrongFeedbackText){
               elementToFocus = $(questionElement.get(5)).children().get(5);
           } else if (labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRetryOption) {
               if (questionType === UmQuestionWidget.WIDGET_NAME_MULTICHOICE) {
                   elementToFocus = $(questionElement.get(questionElement.length - 4)).children().get(3);
               } else {
                   elementToFocus = $(questionElement.get(questionElement.length - 3)).children().get(5);
               }
           } else if (labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForCheckAnswerInputPromptBtn) {
               elementToFocus = $(questionElement.get(5)).children().get(2);
           } else if (labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForChoiceBodyText) {
               elementToFocus = $(element).next().get(0);
           } else if (labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFeedbackBodyText) {
               elementToFocus = $(element).next().get(0);
           } else if (labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForRightAnswerOption) {
               elementToFocus = $(element).prev().get(0);
           }else if($(element).hasClass("add-choice") || $(element).hasClass("img-delete-inner")){
               setTimeout(function () {
                   const questionChoices = $(questionRoot).find(".question-choice");
                   if(questionChoices && questionChoices.length > 0){
                       elementToFocus = $(questionChoices.get(questionChoices.length -1)).children().get(3);
                   }else{
                       elementToFocus = questionElement.get(3);
                   }
                   UmContentEditorCore.prototype.setCursorToANode(elementToFocus);
                   UmContentEditorCore.prototype.scrollToElement(elementToFocus);
               },200);
           } else if($(element).hasClass("img-delete") || $(element).hasClass("question") || $(element).hasClass("input-group")){
               elementToFocus = $(questionElement.get(3)).children().get(0);
           }else if($(element).hasClass("question-retry-option") || $(element).hasClass("question-retry-holder")){
               if(questionType === UmQuestionWidget.WIDGET_NAME_MULTICHOICE){
                   elementToFocus = $(questionElement).children().get(6);
               }else{
                   elementToFocus = $(questionElement.get(5)).children().get(8);
               }
           }
           if(elementToFocus && !$(elementToFocus).hasClass("question")){
               UmContentEditorCore.prototype.setCursorToANode(elementToFocus);
           }
       }
   }catch (e) {
       console.log("setFocusToNextFocusableArea: "+e);
   }
};

/**
 * Set cursor to the root editable element.
 * @param rootElement root target element.
 */
UmContentEditorCore.prototype.setCursorPositionAtRootElementEnd = (rootElement) =>{
    if(rootElement == null){
        rootElement = document.getElementById("umEditor");
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
 * Scroll to the targeted element in the editor.
 * @param element target element
 */
UmContentEditorCore.prototype.scrollToElement = (element) => {
    if (!!element && element.scrollIntoView) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center'});
    }
};

/**
 * Set cursor to a specific editor node
 * @param element target editor node.
 */
UmContentEditorCore.prototype.setCursorToANode = (element) => {
   try{
       element = element === null ? $("#umEditor").children().get(0):element;
       const range = document.createRange();
       const sel = window.getSelection();
       range.setStart(element, 0);
       range.collapse(false);
       sel.removeAllRanges();
       sel.addRange(range);
       element.focus();
   }catch (e) {
       console.log("setCursorToANode",e)
   }
};


UmContentEditorCore.prototype.setCursor = (element = null,isRoot) =>{
    if(isRoot){
        UmContentEditorCore.prototype.setCursorPositionAtRootElementEnd(element);
    }else{
        UmContentEditorCore.prototype.setCursorToANode(element);
    }
};


/**
 * Prevent keyboard key and stop propagation
 * @param e tinymce event
 * @returns {boolean} true if the task succeeded.
 */
UmContentEditorCore.prototype.allowKeyboardKey = (event) =>{
    try{
        event.preventDefault();
        event.stopImmediatePropagation();
    }catch (e) {
        console.log("allowKeyboardKey",e);
    }
    return false;
};

/**
 * Set default language locale on UmEditor
 */
UmContentEditorCore.prototype.setLanguageLocale = (locale,isTest) => {
    const localeFileUrl = (isTest ? "/umEditor/":"")+ languageLocaleDir+"locale."+locale+".json";
    $.ajax({url: localeFileUrl, success: (localeFileContent) => {
        UmQuestionWidget.PLACEHOLDERS_LABELS = JSON.parse(localeFileContent);
    }});
};

/**
 * Check if the current action is worth taking place
 * @param isProtected is current selected node protected
 * @param activeNode current selected node
 * @param selectedContentLength length of the content selection
 * @param deleteKeys is delete key
 * @param enterKey is enter key
 * @param e keydown event
 * @returns {boolean} True is the action should take place otherwise false.
 */
UmContentEditorCore.checkProtectedElements = (activeNode,selectedContentLength,isProtected,deleteKeys, enterKey, e) => {
    if(selectedContentLength > 0 || deleteKeys){
        const isLabel = $(activeNode).hasClass("um-labels");
        const isDeleteQuestionBtn = $(activeNode).hasClass("close");
        const isCloseSpan = $(activeNode).is("span");
        const isButtonLabels = $(activeNode).is("button");
        const isParagraph = $(activeNode).is("p");
        const isDiv = $(activeNode).is("div");
        const isChoiceSelector = $(activeNode).hasClass("question-retry-option");
        const isInputGroup = $(activeNode).hasClass("input-group");
        const preventEditorDiv = $(activeNode).attr("id") === "umEditor";
        const innerTextContentLength = UmQuestionWidget.removeSpaces($(activeNode).text()).length;
        const innerHtmlContentLength = UmQuestionWidget.removeSpaces($(activeNode).html()).length;

        const preventLabelsDeletion = deleteKeys && isLabel;

        if(isParagraph){
            const divWrapper = $(activeNode).closest("div");
            if(divWrapper){
                const divParagraphs = $(divWrapper).children();
                isProtected = divParagraphs.length === 1 && deleteKeys && ((innerHtmlContentLength > 0 || innerTextContentLength > 0)
                    && UmContentEditorCore.prototype.getCursorPositionRelativeToTheEditableElementContent() === 0)
            }else{
                isProtected = false;
            }
        }

        if((isProtected && this.selectedContentLength > 0) || (enterKey && isLabel)){
            wasContentSelected = true;
        }

        const disableKeys = this.selectedContentLength === 0 && (isDeleteQuestionBtn || isCloseSpan || isButtonLabels || preventLabelsDeletion ||
            isChoiceSelector || isInputGroup || (preventEditorDiv && deleteKeys))||
            isProtected || isProtectedElement || isLabel || isDiv;

        UmContentEditorCore.prototype.setFocusToNextFocusableArea(activeNode);
        const hasQuestions = $("#umEditor").find(".question").length > 0;
        const isBelowQuestion = $($($(activeNode).parent()).prev()).hasClass("question");
        const isNotBelowQuestion = $($($(activeNode).parent()).prev()).hasClass("extra-content");

        if(disableKeys &&  hasQuestions){
            return (isNotBelowQuestion || $(activeNode).hasClass("mce-content-body")
                || (isParagraph && isNotBelowQuestion) ? true : UmContentEditorCore.prototype.allowKeyboardKey(e));
        }else{
            return (isBelowQuestion && deleteKeys && innerTextContentLength === 0 || disableKeys ? UmContentEditorCore.prototype.allowKeyboardKey(e) : true);
        }
    }else{
        return true;
    }
};


/**
 * Initialize tinymce editor to the document element
 * @param umConfig editor configuration object
 * {locale:'en', path:'',toolbar:false}
 * locale => Default UMEditor language locale
 * path => Absolute path resolver (For test)
 * toolbar => Flag to show and hide default tinymce toolbar
 */
UmContentEditorCore.initEditor = (umConfig) => {
    UmContentEditorCore.prototype.setLanguageLocale(umConfig && umConfig.locale ? umConfig.locale:"en"
        ,umConfig && umConfig.test ? umConfig.test:false);
    let showToolbar = umConfig && umConfig.toolbar ? umConfig.toolbar:false;
    const configs = {
        selector: '#umEditor',
        height: $(window).height(),
        menubar: showToolbar,
        statusbar: showToolbar,
        inline:true,
        force_br_newlines : false,
        force_p_newlines : true,
        forced_root_block : '',
        plugins: ['directionality','lists'],
        toolbar: showToolbar,
        content_css: [
            '//fonts.googleapis.com/css?family=Lato:300,300i,400,400i',
            '//www.tinymce.com/css/codepen.min.css'],
        setup: (ed) => {
            ed.on('preInit', () => {
                ed.parser.addAttributeFilter("data-um-widget", (nodes) =>{
                    for(let node in nodes) {
                        if(!nodes.hasOwnProperty(node))
                            continue;
                        const questionNode = tinymce.html.Serializer().serialize(nodes[node]);
                        if($(questionNode).attr("id") != null){
                            let questionWidget = UmQuestionWidget.handleWidgetNode(questionNode);
                            questionWidget = questionWidget.startEditing();
                            questionWidget = $("<div>").append(questionWidget);
                            const tempNode =  tinymce.html.DomParser().parse($(questionWidget).html());
                            nodes[node].replace(tempNode);
                        }
                        UmQuestionWidget.handleWidgetListeners(true);
                    }
                });
            });
        },
        init_instance_callback: (ed) => {
            /**
             * Listen for text selection event
             * @type {[type]}
             */

            ed.on('SelectionChange', () => {
                try{
                    const sel = rangy.getSelection();
                    const range = sel.rangeCount ? sel.getRangeAt(0) : null;
                    let selectedContent = "";
                    if (range) {
                        selectedContent = range.toHtml();
                    }
                    this.selectedContentLength = selectedContent.length;
                    this.protectedSection = UmQuestionWidget.isLabelText(selectedContent) || (selectedContent.includes("<div")
                        || selectedContent.includes("<label"));
                }catch (e) {
                    console.log(e);
                }
            });

            ed.on('Paste', e => {
                e.stopPropagation();
                e.preventDefault();

                let clipboardData = e.clipboardData || window.clipboardData;
                const content = clipboardData.getData('Text');
                if($(content).hasClass("question")){
                    UmContentEditorCore.prototype.insertQuestionNodeContent(content,true);
                }else{
                    UmContentEditorCore.insertContentRaw(content)
                }
            });


            /**
             * Listen for click event inside the editor
             * @type {[type]}
             */
            ed.on('click', e => {
                UmContentEditorCore.prototype.checkActivatedControls();
                const selection = ed.selection;
                const activeNode = selection.getNode();
                this.protectedSection = $(activeNode).hasClass("um-labels") || $(activeNode).is("button")
                    || $(activeNode).hasClass("pg-break")
                    || $(activeNode).hasClass("question-retry-option");
                UmContentEditorCore.prototype.getNodeDirectionality();
                UmContentEditorCore.prototype.setFocusToNextFocusableArea(e.target);
            });

            /**
             * Listen for the key up event
             * @type {[type]}
             */
            ed.on('keyup', () => {
                if(wasContentSelected){
                    wasContentSelected = false;
                    UmContentEditorCore.editorActionUndo();
                }
            });


            /**
             * Listen for the keyboard keys and prevent important label and divs from being deleted from the editor
             * @type {[type]}
             */
            ed.on('keydown', (e) => {
                UmContentEditorCore.checkProtectedElements(tinymce.activeEditor.selection.getNode(),
                this.selectedContentLength,this.protectedSection,e.key === "Backspace" || e.key === "Delete",
                e.key === "Enter",e);

            });
        }
    };

    if(showToolbar){
        configs.toolbar = ['undo redo | bold italic underline strikethrough superscript subscript | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | fontsizeselect'];
    }

    tinymce.init(configs).then(() => {

        rangy.init();
        UmContentEditorCore.prototype.requestFocus();
        UmQuestionWidget.setEditingMode(true);

        if($(".question").length > 0){
            UmContentEditorCore.prototype.setCursorPositionAtRootElementEnd();
        }else{
            const editorWrapper = $("#umEditor");
            if(UmQuestionWidget.removeSpaces($(editorWrapper.children().get(0)).text()).length === 0){
                $(editorWrapper.children().get(0)).remove();
            }
            editorWrapper.append(UmQuestionWidget.EXTRA_CONTENT_WIDGET);
            UmContentEditorCore.prototype.setCursorToANode(editorWrapper.children().get(0))
        }
        tinymce.activeEditor.dom.remove(tinymce.activeEditor.dom.select('p.pg-break'));
        try{
            UmContentEditor.onInitEditor(JSON.stringify({action:'onInitEditor',content:"true"}));
        }catch (e) {
            console.log("onInitEditor: "+e);
        }

        try{
            //add observer to watch content changes
            let contentWatcherFilters = {
                attributes: true, characterData: true, childList: true, subtree: true,
                attributeOldValue: true, characterDataOldValue: true
            };

            const contentChangeObserver = new MutationObserver(function() {
                const editorContainer = $("#umEditor");
                const textHolder = editorContainer.find(".extra-content");
                if(textHolder.length === 0){
                    editorContainer.find("p").remove();
                    editorContainer.append(UmQuestionWidget.EXTRA_CONTENT_WIDGET);
                    UmContentEditorCore.prototype.setCursor(null,false);
                }
                setTimeout(() => {UmContentEditorCore.prototype.checkActivatedControls()},300);
                const previewContent = JSON.stringify({action:'onSaveContent',
                    content:UmQuestionWidget.saveContentEditor(btoa(tinyMCE.activeEditor.getContent()))});
                try{
                    UmContentEditor.onSaveContent(previewContent);
                }catch (e) {
                    console.log("onContentChanged:",e);
                }
            });
            contentChangeObserver.observe(document.querySelector('#umEditor'),contentWatcherFilters);


            //add observer to watch controls change
            const menuWatcherFilter = {
                attributes : true,
                attributeFilter : ['style']
            };
            const menuStateChangeObserver = new MutationObserver(function() {
                setTimeout(() => {UmContentEditorCore.prototype.checkActivatedControls()},300);
            });
            menuStateChangeObserver.observe(document.querySelector('.mce-panel'),menuWatcherFilter);

        }catch (e) {
            console.log("Observers ",e);
        }
    });

};

/**
 * Get content from tinymce editor
 * @returns string content
 */
UmContentEditorCore.getContent = ()=>{
    return tinymce.activeEditor.getContent();
};

UmContentEditorCore.destroy = () =>{
    UmContentEditorCore.executeCommand("mceRemoveControl",true,"#umeditor");
};




