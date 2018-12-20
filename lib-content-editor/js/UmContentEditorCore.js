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


umContentEditor.generateContentForPreview =  (content)  => {
    const previewContent = UmQuestionWidget.saveContentEditor(content);
    console.log("Selection",previewContent);
    return {action:'onSaveContent',content:btoa(previewContent)};
};

umContentEditor.getQuestionBreak = () => {
    let questionBreak = "";
    for(let counter = 0 ; counter < 2;counter++){
        questionBreak = questionBreak + '<p style="page-break-before: always" class="pg-break">';
    }
    return questionBreak;
};

checkActivatedControls = () => {
    const commandStatus = [];
    for(let command in UmContentEditorCore.formattingCommandList){
        if(!UmContentEditorCore.formattingCommandList.hasOwnProperty(command))
          continue;
          const commandString = UmContentEditorCore.formattingCommandList[command];
          const commandState = {};
          commandState.command = commandString;
          commandState.status = this.isToolBarButtonActive(commandString) === "true";
          commandStatus.push(commandState);
    }
    try{
        UmContentEditor.onControlActivatedCheck(JSON.stringify({action:'onActiveControlCheck',content:btoa(JSON.stringify(commandStatus))}));
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
            tinyMCE.activeEditor.execCommand('mceInsertContent', false, questionNode,{format: 'raw'});
            this.setCursorPositionAtRootElementEnd();
        }});
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
    mediaContent = "<p class='text-center'>"+mediaContent+"</p><p style=\"page-break-before: always\" class=\"pg-break\">";
    tinyMCE.activeEditor.execCommand('mceInsertContent', false, mediaContent,{format: 'raw'});
};



handleCursorPositionFocus = element => {
    const labelText = $(element).text();
    const questionRoot = $(element).closest("div .question");
    const questionElement = $(questionRoot).children();
    const questionType = $(questionRoot).attr("data-um-widget");
    let elementToFocus = null;
    switch (labelText) {
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionBodyText:
            elementToFocus = questionElement.get(3);
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksPromptInput:
            elementToFocus = $(questionElement.get(5)).children().get(2);
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksAnswerBodyText:
            elementToFocus = $(questionElement.get(5)).children().get(2);
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRightFeedbackText:
            elementToFocus = $(questionElement.get(5)).children().get(5);
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionWrongFeedbackText:
            elementToFocus = $(questionElement.get(5)).children().get(8);
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRetryOption:
            if(questionType === UmQuestionWidget.WIDGET_NAME_MULTICHOICE){
                elementToFocus = findClosestElement(element,questionRoot).get(0)
            }else{
                elementToFocus = $(questionElement.get(5)).children().get(8);
            }
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForCheckAnswerInputPromptBtn:
            elementToFocus = $(questionElement.get(5)).children().get(2);
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForChoiceBodyText:
            elementToFocus = findClosestElement(element,questionRoot).get(2);
            break;
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFeedbackBodyText:
            elementToFocus = findClosestElement(element,questionRoot).get(6);
            console.log("forcing",elementToFocus);
        case UmQuestionWidget.PLACEHOLDERS_LABELS.labelForRightAnswerOption:
            elementToFocus = findClosestElement(element,questionRoot).get(4);
            break;
    }

    if($(element).hasClass("question") || $(element).hasClass("input-group")){
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
};

findClosestElement = (element,rootElement) =>{
    const choiceId = $(element).closest("div.question-choice").attr("id");
    const closeElement = $(rootElement).find("#" + choiceId).children();
    return closeElement;
};

setCursorPositionAtRootElementEnd = (rootElement) =>{
    if(rootElement == null){
        rootElement = document.getElementById("umPreview");
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

setCursorPositionAtAnyGivenEditableElement = (element) => {
    const range = document.createRange();
    const sel = window.getSelection();
    range.setStart(element, 0);
    range.collapse(false);
    sel.removeAllRanges();
    sel.addRange(range);
    element.focus();
};


umContentEditor.initEditor = (showToolbar = false) => {
    const configs = {
        selector: '#umPreview',
        height: $(window).height(),
        menubar: showToolbar,
        statusbar: showToolbar,
        inline:true,
        force_br_newlines : false,
        force_p_newlines : false,
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
                        let questionWidget = UmQuestionWidget.handleQuestionNode(questionNode);
                        questionWidget = questionWidget.startEditing(ed);
                        questionWidget = $("<div>").append(questionWidget)
                            .append('<p style="page-break-before: always" class="pg-break"><br ');
                        const tempNode =  tinymce.html.DomParser().parse($(questionWidget).html());
                        nodes[node].replace(tempNode);
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
                    this.protectedSection = (selectedContent.includes("<div") || selectedContent.includes("<label"));
                }catch (e) {
                    console.log(e);
                }
            });


            /**
             * Listen for click event inside the editor
             * @type {[type]}
             */
            ed.on('click', e => {
                this.checkActivatedControls();
                const selection = ed.selection;
                const activeNode = selection.getNode();
                this.protectedSection = $(activeNode).hasClass("um-labels")
                    || $(activeNode).hasClass("close") ||  $(activeNode).is("span")
                    || $(activeNode).is("button") || $(activeNode).hasClass("pg-break")
                    || $(activeNode).hasClass("question-retry-option");
                if($(e.target).is("label") || $(e.target).is("button")
                    || $(e.target).hasClass("question") || $(e.target).hasClass("question-retry-option")
                    || $(e.target).hasClass("question-retry-holder") || $(e.target).hasClass("input-group")){
                    this.handleCursorPositionFocus(e.target);
                }
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

                const selection = tinymce.activeEditor.selection;
                const activeNode = selection.getNode();
                const isLabel = $(activeNode).hasClass("um-labels");
                const isPgBreak = $(activeNode).hasClass("pg-break");
                const isDeleteQuestionBtn = $(activeNode).hasClass("close");
                const isCloseSpan = $(activeNode).is("span");
                const isButtonLabels = $(activeNode).is("button");
                const isDiv = $(activeNode).is("div");
                const isChoiceSelector = $(activeNode).hasClass("question-retry-option");
                const isInputgroup = $(activeNode).hasClass("input-group");
                let innerDivEmpty = false;

                if(isDiv){
                    const divContent = $(activeNode).text();
                    innerDivEmpty = divContent.length <= 0 || this.getCursorPositionRelativeToTheEditableElementContent() <= 0;
                }

                const preventLabelsDeletion = deleteKeys && isLabel;
                const preventTagDeletion = deleteKeys && innerDivEmpty;

                if((this.protectedSection && this.selectedContentLength > 0) || (enterKey && isLabel)){
                    wasContentSelected = true;
                }

                const disableDeleteOrEnterKey = isPgBreak || isDeleteQuestionBtn || isCloseSpan || isButtonLabels || preventLabelsDeletion || isChoiceSelector;

                if(this.selectedContentLength === 0){
                    if(preventTagDeletion || disableDeleteOrEnterKey || isInputgroup){
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        return false
                    }
                }else{
                    if((disableDeleteOrEnterKey && this.protectedSection) || disableDeleteOrEnterKey || isProtectedElement){
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        return false
                    }
                }
            });
        }
    };

    if(showToolbar){
        configs.toolbar = ['undo redo | bold italic underline strikethrough superscript subscript | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | fontsizeselect'];
    }
    tinymce.init(configs).then(() => {
        umContentEditor.requestFocus();
        UmQuestionWidget.setEditingMode(true);
        if($(".question").length > 0){
            this.setCursorPositionAtRootElementEnd();
        }
        try{
            UmContentEditor.onInitEditor(JSON.stringify({action:'onInitEditor',content:"true"}));
        }catch (e) {
            console.log("onInitEditor: "+e);
        }

        //add observer to watch content changes
        let filters = {
            attributes: true, characterData: true, childList: true, subtree: true,
            attributeOldValue: true, characterDataOldValue: true
        };
        const contentChangeObserver = new MutationObserver(function() {
            const content = umContentEditor.generateContentForPreview(btoa(tinyMCE.activeEditor.getContent()));
            try{
                UmContentEditor.onContentChanged(JSON.stringify({action:'onContentChanged',content:content}));
            }catch (e) {
                console.log("onContentChanged:",e);
            }
        });
        contentChangeObserver.observe(document.querySelector('#umPreview'),filters);
    });
};



