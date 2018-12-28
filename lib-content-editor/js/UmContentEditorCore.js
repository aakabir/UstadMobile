function UmContentEditorCore() {if (!(this instanceof UmContentEditorCore))
    return new UmContentEditorCore();}

const umContentEditor = new UmContentEditorCore();

const questionTemplatesDir = "templates/";

const indexMultipleChoiceQuestionType = 0;

const indexFillTheBlanksQuestionType = 1;

let wasContentSelected = false;

let isProtectedElement = false;



UmContentEditorCore.formattingCommandList = [
    'Bold','Underline','Italic','Strikethrough','Superscript','Subscript','JustifyCenter','JustifyLeft',
    'JustifyRight','JustifyFull', 'Indent','Outdent','JustifyLeft','JustifyCenter', 'JustifyRight',
    'JustifyFull','InsertUnorderedList','InsertOrderedList','mceDirectionLTR','mceDirectionRTL'
];

UmContentEditorCore.sectionType = {
  sectionBody:1,
  sectionFeedback:2
};

/**
 * Check if a toolbar button is active or not
 * @param buttonIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
isToolBarButtonActive = (buttonIdentifier) => {
    return tinyMCE.activeEditor.queryCommandState(buttonIdentifier);
};

/**
 * Check if the control was executed at least once.
 * @param controlCommand
 */
isControlActivated = (controlCommand) => {
    return tinyMCE.activeEditor.queryCommandValue(controlCommand) != null;
};

/**
 * Change editor blankDocument font size
 * @param fontSize Size to change to
 * @returns {string | * | void}
 */
umContentEditor.setFontSize = (fontSize) => {
    this.executeCommand("FontSize",""+fontSize+"pt");
    const activeFont = tinyMCE.activeEditor.queryCommandValue("FontSize");
    const isActive = this.isControlActivated("FontSize");
    return {action:'activeControl',content:btoa("FontSize-"+isActive+"-"+activeFont)};
};

/**
 * Undo previously performed action
 * @returns {Object} TRUE if succeed FALSE otherwise
 */
umContentEditor.editorActionUndo = () => {
    this.executeCommand("Undo",null);
    const isActive = this.isControlActivated("Undo");
    return {action:'activeControl',content:btoa("Undo-"+isActive)};
};

/**
 * Redo previously performed action
 * @returns {Object} TRUE if succeed FALSE otherwise
 */
umContentEditor.editorActionRedo = () => {
    this.executeCommand("Redo",null);
    const isActive = this.isControlActivated("Redo");
    return {action:'activeControl',content:btoa("Redo-"+isActive)};
};

/**
 * Set text direction from Left to Right
 * @returns {Object} TRUE if direction changed otherwise FALSE
 */
umContentEditor.textDirectionLeftToRight = () => {
    this.executeCommand('mceDirectionLTR');
    const isActive = this.isControlActivated("mceDirectionLTR");
    return {action:'activeControl',content:btoa("mceDirectionLTR-"+isActive)};
};

/**
 * Set text direction from Right to Left
 * @returns {Object} TRUE if direction changed otherwise FALSE
 */
umContentEditor.textDirectionRightToLeft = () => {
    this.executeCommand("mceDirectionRTL",null);
    const isActive = this.isControlActivated("mceDirectionRTL");
    return {action:'activeControl',content:btoa("mceDirectionRTL-"+isActive)};
};

/**
 * Remove or insert un-ordered list
 * @returns {Object} TRUE inserted and FALSE otherwise
 */
umContentEditor.paragraphUnOrderedListFormatting = () => {
    this.executeCommand("InsertUnorderedList",null);
    const isActive = this.isToolBarButtonActive("InsertUnorderedList");
    return {action:'activeControl',content:btoa("InsertUnorderedList-"+isActive)};
};

/**
 * Remove or insert ordered list
 * @returns {Object} TRUE inserted and FALSE otherwise
 */
umContentEditor.paragraphOrderedListFormatting = () => {
    this.executeCommand("InsertOrderedList",null);
    const isActive = this.isToolBarButtonActive("InsertOrderedList");
    return {action:'activeControl',content:btoa("InsertOrderedList-"+isActive)};
};

/**
 * Justify left editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
umContentEditor.paragraphLeftJustification = () => {
    this.executeCommand("JustifyLeft",null);
    const isActive = this.isToolBarButtonActive("JustifyLeft");
    return {action:'activeControl',content:btoa("JustifyLeft-"+isActive)};
};

/**
 * Justify left editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
umContentEditor.paragraphRightJustification = () => {
    this.executeCommand("JustifyRight",null);
    const isActive = this.isToolBarButtonActive("JustifyRight");
    return {action:'activeControl',content:btoa("JustifyRight-"+isActive)};
};

/**
 * Justify fully editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
umContentEditor.paragraphFullJustification = () => {
    this.executeCommand("JustifyFull",null);
    const isActive = this.isToolBarButtonActive("JustifyFull");
    return {action:'activeControl',content:btoa("JustifyFull-"+isActive)};
};

/**
 * Justify center editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
umContentEditor.paragraphCenterJustification = () => {
    this.executeCommand("JustifyCenter",null);
    const isActive = this.isToolBarButtonActive("JustifyCenter");
    return {action:'activeControl',content:btoa("JustifyCenter-"+isActive)};
};

/**
 * Indent editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
umContentEditor.paragraphOutDent = () => {
    this.executeCommand("Outdent",null);
    const isActive = this.isControlActivated("Outdent");
    return {action:'activeControl',content:btoa("Outdent-"+isActive)};
};

/**
 * Indent editor blankDocument
 * @returns {Object} TRUE if justified FALSE otherwise
 */
umContentEditor.paragraphIndent = () => {
    this.executeCommand("Indent",null);
    const isActive = this.isControlActivated("Indent");
    return {action:'activeControl',content:btoa("Indent-"+isActive)};
};

/**
 * Apply bold ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
umContentEditor.textFormattingBold = () => {
    this.executeCommand("Bold",null);
    const isActive = this.isToolBarButtonActive("Bold");
    return {action:'activeControl',content:btoa("Bold-"+isActive)};
};

/**
 * Apply italic ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
umContentEditor.textFormattingItalic = () => {
    this.executeCommand("Italic",null);
    const isActive = this.isToolBarButtonActive("Italic");
    return {action:'activeControl',content:btoa("Italic-"+isActive)};
};

/**
 * Apply underline ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
umContentEditor.textFormattingUnderline = () => {
    this.executeCommand("Underline",null);
    const isActive = this.isToolBarButtonActive("Underline");
    return {action:'activeControl',content:btoa("Underline-"+isActive)};
};

/**
 * Apply strike-through ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
umContentEditor.textFormattingStrikeThrough = () => {
    this.executeCommand("Strikethrough",null);
    const isActive = this.isToolBarButtonActive("Strikethrough");
    return {action:'activeControl',content:btoa("Strikethrough-"+isActive)};
};

/**
 * Apply superscript ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
umContentEditor.textFormattingSuperScript = () => {
    this.executeCommand("Superscript",null);
    const isActive = this.isToolBarButtonActive("Superscript");
    return {action:'activeControl',content:btoa("Superscript-"+isActive)};
};

/**
 * Apply subscript ustadEditor to a text
 * @returns {Object} TRUE if applied otherwise FALSE
 */
umContentEditor.textFormattingSubScript = () => {
    this.executeCommand("Subscript",null);
    const isActive = this.isToolBarButtonActive("Subscript");
    return {action:'activeControl',content:btoa("Subscript-"+isActive)};
};

/**
 * Check if the current selected editor node has controls activated to it
 * @param commandValue control to check from
 * @returns {{action: string, content: string}}
 */
umContentEditor.checkCurrentActiveControls = (commandValue) => {
    const isActive = this.isToolBarButtonActive(commandValue);
    return {action:'activeControl',content:btoa(commandValue+"-"+isActive)};
};

/**
 * Start checking for active controls  and reactivate
 * @returns {{action: string, content: string}}
 */
umContentEditor.startCheckingActivatedControls = () => {
    return {action:'onActiveControlCheck',content:btoa("yes")};
};

/**
 * Insert multiple choice question template to the editor
 */
umContentEditor.insertMultipleChoiceQuestionTemplate =  () =>  {
    this.insertQuestionTemplate(indexMultipleChoiceQuestionType);
};

/**
 * Insert fill in the blanks question template to the editor
 */
umContentEditor.insertFillInTheBlanksQuestionTemplate =  () =>  {
    this.insertQuestionTemplate(indexFillTheBlanksQuestionType);
};

umContentEditor.requestFocus =  () =>  {
    this.executeCommand("mceFocus",null);
    return this.isControlActivated("mceFocus");
};

umContentEditor.selectAll =  () => {
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
executeCommand = (command, args) => {
    try{
        tinyMCE.activeEditor.execCommand(command, false,args);
    }catch(e){
        console.log("executeCommand: "+e);
    }
};

getCursorPositionRelativeToTheEditableElementContent = ()  =>  {
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
    return -1;
};


umContentEditor.checkActivatedControls = () => {
    const commandStatus = [];
    for(let command in UmContentEditorCore.formattingCommandList){
        if(!UmContentEditorCore.formattingCommandList.hasOwnProperty(command))
          continue;
          const commandString = UmContentEditorCore.formattingCommandList[command];
          const commandState = {};
          const status = this.isToolBarButtonActive(commandString);
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


insertQuestionTemplate = (questionTypeIndex) => {
    const questionTemplateList = ['question-multiple-choice.html','question-fill-the-blanks.html'];
    const nextQuestionId = UmQuestionWidget.QUESTION_ID_TAG+UmQuestionWidget.getNextQuestionId();
    const nextChoiceId = UmQuestionWidget.CHOICE_ID_TAG+UmQuestionWidget.getNextQuestionId();
    $.ajax({url: questionTemplatesDir+questionTemplateList[questionTypeIndex], success: (templateHtmlContent) => {
            let questionNode = $(templateHtmlContent).attr("id",nextQuestionId);
            $(questionNode).find(".question-choice").attr("id",nextChoiceId);
            questionNode = $(questionNode).prop('outerHTML');
            UmQuestionWidget.setQuestionStatus(true);
            tinymce.activeEditor.dom.add(tinymce.activeEditor.getBody(), 'p', {class: 'pg-break',style:'page-break-before: always'}, '');
            this.setCursorPositionAtAnyGivenEditableElement(this.getLastEditableElement());
            umContentEditor.insertContentRaw(questionNode);
            this.scrollToElement(this.getLastEditableElement());
            this.setCursorPositionAtAnyGivenEditableElement(this.getLastExtraContentWidget());
            tinymce.activeEditor.dom.remove(tinymce.activeEditor.dom.select('p.pg-break'));
        }});
};

getLastExtraContentWidget = () => {
    const extraContentWidgets = $("#umEditor").find('.extra-content');
    return $(extraContentWidgets[extraContentWidgets.length - 1]).get(0);
};

getLastEditableElement = () =>{
    const editor = $("#umEditor");
    const pageBreaks = editor.find('.pg-break');
    if(pageBreaks.length === 0){
        editor.append(UmQuestionWidget.PAGE_BREAK);
        getLastEditableElement();
    }
    return $(pageBreaks[pageBreaks.length - 1]).get(0);
};



umContentEditor.insertMediaContent = (source, mimeType) => {
    const width = $(window).width();
    let mediaContent = null;
    if(mimeType.includes("image")){
        mediaContent = "<img src=\""+source+"\" class=\"um-media img-fluid\" width=\""+width+"\"/>";
    }else if(mimeType.includes("audio")){
        mediaContent =
            "<video controls class='media-audio'>" +
            "    <source src=\""+source+"\" type=\""+mimeType+"\">" +
            "</video>";
    }else{
        mediaContent =
            "<video controls class='um-media img-fluid' width='"+width+"'>" +
            "    <source src=\""+source+"\" type=\""+mimeType+"\">" +
            "</video>";
    }
    mediaContent = "<p class='text-center'>"+mediaContent+"</p><br/>";
    umContentEditor.insertContentRaw(mediaContent);
};

umContentEditor.insertContentRaw = (content) =>{
    tinyMCE.activeEditor.execCommand('mceInsertContent', false, content,{format: 'raw'});
};


handleCursorFocusPosition = element => {
   try{
       if($(element).is("label") || $(element).is("button") || $(element).hasClass("close")
           || $(element).hasClass("question") || $(element).hasClass("question-retry-option")
           || $(element).hasClass("question-retry-holder") || $(element).hasClass("input-group")){

           const labelText = $(element).text();
           const questionRoot = $(element).closest("div .question");
           const questionElement = $(questionRoot).children();
           const questionType = $(questionRoot).attr("data-um-widget");
           let elementToFocus = null;
           if(labelText === UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionBodyText){
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
           }else if($(element).hasClass("add-choice")){
               setTimeout(function () {
                   const questionChoices = $(questionRoot).find(".question-choice");
                   elementToFocus = $(questionChoices.get(questionChoices.length -1)).children().get(2);
                   setCursorPositionAtAnyGivenEditableElement(elementToFocus);
                   scrollToElement(elementToFocus);
               },200);
           } else if($(element).hasClass("question") || $(element).hasClass("input-group")){
               elementToFocus = questionElement.get(3);
           }else if($(element).hasClass("question-retry-option") || $(element).hasClass("question-retry-holder")){
               if(questionType === UmQuestionWidget.WIDGET_NAME_MULTICHOICE){
                   elementToFocus = $(questionElement).children().get(6);
               }else{
                   elementToFocus = $(questionElement.get(5)).children().get(8);
               }
           }

           if(elementToFocus && !$(elementToFocus).hasClass("question")){
               this.setCursorPositionAtAnyGivenEditableElement(elementToFocus);
           }
       }
   }catch (e) {
       console.log("handleCursorFocusPosition: "+e);
   }
};



setCursorPositionAtRootElementEnd = (rootElement) =>{
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

scrollToElement = (element) => {
    if (!!element && element.scrollIntoView) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center'});
    }
};

setCursorPositionAtAnyGivenEditableElement = (element) => {
   try{
       const range = document.createRange();
       const sel = window.getSelection();
       range.setStart(element, 0);
       range.collapse(false);
       sel.removeAllRanges();
       sel.addRange(range);
       element.focus();
   }catch (e) {
       console.log("setCursorPositionAtAnyGivenEditableElement",e)
   }
};


umContentEditor.initEditor = (showToolbar = false) => {
    const configs = {
        selector: '#umEditor',
        height: $(window).height(),
        menubar: showToolbar,
        statusbar: showToolbar,
        inline:true,
        force_br_newlines : false,
        force_p_newlines : true,
        forced_root_block : '',
        plugins: ['directionality','lists','noneditable','visualblocks'],
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
                            let questionWidget = UmQuestionWidget.handleQuestionNode(questionNode);
                            questionWidget = questionWidget.startEditing();
                            questionWidget = $("<div>").append(questionWidget)
                                .append(UmQuestionWidget.EXTRA_CONTENT_WIDGET);
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


            /**
             * Listen for click event inside the editor
             * @type {[type]}
             */
            ed.on('click', e => {
                umContentEditor.checkActivatedControls();
                const selection = ed.selection;
                const activeNode = selection.getNode();
                this.protectedSection = $(activeNode).hasClass("um-labels")
                    || $(activeNode).hasClass("close") ||  $(activeNode).is("span")
                    || $(activeNode).is("button") || $(activeNode).hasClass("pg-break")
                    || $(activeNode).hasClass("question-retry-option");
                this.handleCursorFocusPosition(e.target);
            });

            /**
             * Listen for the key up event
             * @type {[type]}
             */
            ed.on('keyup', () => {
                if(wasContentSelected){
                    wasContentSelected = false;
                    umContentEditor.editorActionUndo();
                }
            });

            /**
             * Listen for the keyboard keys and prevent important label and divs from being deleted from the editor
             * @type {[type]}
             */
            ed.on('keydown', e => {
                const deleteKeys = e.key === "Backspace" || e.key === "Delete";
                const enterKey = e.key === "Enter";

                const currentCursorPositionElement = tinymce.activeEditor.selection;
                const activeNode = currentCursorPositionElement.getNode();
                const isLabel = $(activeNode).hasClass("um-labels");
                const isDeleteQuestionBtn = $(activeNode).hasClass("close");
                const isCloseSpan = $(activeNode).is("span");
                const isButtonLabels = $(activeNode).is("button");
                const isParagraph = $(activeNode).is("p");
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
                        this.protectedSection = divParagraphs.length === 1 && deleteKeys && ((innerHtmlContentLength > 0 || innerTextContentLength > 0)
                            && this.getCursorPositionRelativeToTheEditableElementContent() === 0)
                    }else{
                        this.protectedSection = false;
                    }
                }

                if((this.protectedSection && this.selectedContentLength > 0) || (enterKey && isLabel)){
                    wasContentSelected = true;
                }

                let disableKeys = this.selectedContentLength === 0 && (isDeleteQuestionBtn || isCloseSpan || isButtonLabels || preventLabelsDeletion ||
                    isChoiceSelector || isInputGroup || (preventEditorDiv && deleteKeys))||
                    this.protectedSection || isProtectedElement;
                this.handleCursorFocusPosition(activeNode);
                if(disableKeys || isLabel){
                    e.preventDefault();
                    e.stopImmediatePropagation();
                    return false
                }
            });
        }
    };

    if(showToolbar){
        configs.toolbar = ['undo redo | bold italic underline strikethrough superscript subscript | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | fontsizeselect'];
    }
    tinymce.init(configs).then(() => {
        rangy.init();
        umContentEditor.requestFocus();
        UmQuestionWidget.setEditingMode(true);
        if($(".question").length > 0){
            this.setCursorPositionAtRootElementEnd();
        }else{
            const editorWrapper = $("#umEditor");
            if(UmQuestionWidget.removeSpaces($(editorWrapper.children().get(0)).text()).length === 0){
                $(editorWrapper.children().get(0)).remove();
            }
            editorWrapper.append(UmQuestionWidget.EXTRA_CONTENT_WIDGET);
            setCursorPositionAtAnyGivenEditableElement(editorWrapper.children().get(0))
        }
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
                const previewContent = JSON.stringify({action:'onSaveContent',
                    content:UmQuestionWidget.saveContentEditor(btoa(tinyMCE.activeEditor.getContent()))});
                try{
                    UmContentEditor.onSaveContent(previewContent);
                }catch (e) {
                    console.log("onContentChanged:",e);
                }
            });
            contentChangeObserver.observe(document.querySelector('#umEditor'),contentWatcherFilters);

            let menuWatcherFilter = {
                attributes : true,
                attributeFilter : ['style']
            };
            const menuStateChangeObserver = new MutationObserver(function() {
                umContentEditor.checkActivatedControls();
            });
            menuStateChangeObserver.observe(document.querySelector('.mce-panel'),menuWatcherFilter);
        }catch (e) {
            console.log("Observers ",e);
        }
    });
};



