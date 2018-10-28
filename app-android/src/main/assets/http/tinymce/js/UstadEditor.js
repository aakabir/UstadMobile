function UstadEditor() {if (!(this instanceof UstadEditor))
    return new UstadEditor();}

const ustadEditor = new UstadEditor();
let activeEditor = null;

/**
 * Initialize UstadEditor with active editor instance
 * @param activeEditor TinyMCE active editor instance
 */
ustadEditor.init = function(activeEditor){
    this.activeEditor = activeEditor;
    console.log("UstadEditor controls initialized");
};

/**
 * Check if a toolbar button is active or not
 * @param buttonIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
ustadEditor.isToolBarButtonActive = function(buttonIdentifier){
    return this.activeEditor.queryCommandState(buttonIdentifier) != null
};

/**
 * Get content of the active Editor
 * @returns {*|void} Current content on editor
 */
ustadEditor.getActiveEditorContent = function(){
    return this.activeEditor.getContent();
};

/**
 * Change editor content font size
 * @param fontSize Size to change to
 * @returns {string | * | void}
 */
ustadEditor.setFontSize = function(fontSize){
    this.activeEditor.execCommand("FontSize",false,""+fontSize+"pt");
    return this.activeEditor.queryCommandValue("FontSize");
};

/**
 * Undo previously performed action
 * @returns {boolean} TRUE if succeed FALSE otherwise
 */
ustadEditor.editorActionUndo = function(){
    this.activeEditor.execCommand("Undo",false,null);
    return this.isToolBarButtonActive("Undo");
};

/**
 * Redo previously performed action
 * @returns {boolean} TRUE if succeed FALSE otherwise
 */
ustadEditor.editorActionRedo = function(){
    this.activeEditor.execCommand("Redo",false,null);
    return this.isToolBarButtonActive("Redo");
};

/**
 * Set text direction from Left to Right
 * @returns {boolean} TRUE if direction changed otherwise FALSE
 */
ustadEditor.textDirectionLeftToRight = function(){
    this.activeEditor.execCommand("mceDirectionLTR",false,null);
    return this.isToolBarButtonActive("mceDirectionLTR");
};

/**
 * Set text direction from Right to Left
 * @returns {boolean} TRUE if direction changed otherwise FALSE
 */
ustadEditor.textDirectionRightToLeft = function(){
    this.activeEditor.execCommand("mceDirectionRTL",false,null);
    return this.isToolBarButtonActive("mceDirectionRTL");
};

/**
 * Remove or insert un-ordered list
 * @returns {boolean} TRUE inserted and FALSE otherwise
 */
ustadEditor.paragraphUnOrderedListFormatting = function(){
    this.activeEditor.execCommand("InsertUnorderedList",false,null);
    return this.isToolBarButtonActive("InsertUnorderedList");
};

/**
 * Remove or insert ordered list
 * @returns {boolean} TRUE inserted and FALSE otherwise
 */
ustadEditor.paragraphOrderedListFormatting = function(){
    this.activeEditor.execCommand("InsertOrderedList",false,null);
    return this.isToolBarButtonActive("InsertOrderedList");
};

/**
 * Justify left editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphLeftJustification = function(){
    this.activeEditor.execCommand("JustifyLeft",false,null);
    return this.isToolBarButtonActive("JustifyLeft");
};


/**
 * Justify left editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphRightJustification = function(){
    this.activeEditor.execCommand("JustifyRight",false,null);
    return this.isToolBarButtonActive("JustifyRight");
};

/**
 * Justify fully editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphFullJustification = function(){
    this.activeEditor.execCommand("JustifyFull",false,null);
    return this.isToolBarButtonActive("JustifyFull");
};

/**
 * Justify center editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphCenterJustification = function(){
    this.activeEditor.execCommand("JustifyCenter",false,null);
    return this.isToolBarButtonActive("JustifyCenter");
};


/**
 * Indent editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphOutDent = function(){
    this.activeEditor.execCommand("Outdent",false,null);
    return this.isToolBarButtonActive("Outdent");
};

/**
 * Indent editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
ustadEditor.paragraphIndent = function(){
    this.activeEditor.execCommand("Indent",false,null);
    return this.isToolBarButtonActive("Indent");
};

/**
 * Apply bold ustadEditor to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingBold = function(){
    this.activeEditor.execCommand("Bold",false,null);
    return this.isToolBarButtonActive("Bold");
};

/**
 * Apply italic ustadEditor to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingItalic = function(){
    this.activeEditor.execCommand("Italic",false,null);
    return this.isToolBarButtonActive("Italic");
};

/**
 * Apply underline ustadEditor to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingUnderline = function(){
    this.activeEditor.execCommand("Underline",false,null);
    return this.isToolBarButtonActive("Underline");
};

/**
 * Apply strike-through ustadEditor to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingStrikeThrough = function(){
    this.activeEditor.execCommand("Strikethrough",false,null);
    return this.isToolBarButtonActive("Strikethrough");
};

/**
 * Apply superscript ustadEditor to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingSuperScript = function(){
    this.activeEditor.execCommand("Superscript",false,null);
    return this.isToolBarButtonActive("Superscript");
};

/**
 * Apply subscript ustadEditor to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
ustadEditor.textFormattingSubScript = function(){
    this.activeEditor.execCommand("Subscript",false,null);
    return this.isToolBarButtonActive("Subscript");
};

/**
 * Get content from the active content edtor
 * @returns {*|void}
 */
ustadEditor.getContent = function(){
    return this.activeEditor.getContent().getHtml();
};

/**
 * Request ustadEditor focus to the active editor
 * @returns {boolean}
 */
ustadEditor.requestFocus = function () {
    this.activeEditor.execCommand("mceFocus",false,null);
    return this.isToolBarButtonActive("mceFocus");
};

/**
 * Insert multiple choice question template to the editor
 */
ustadEditor.insertMultipleChoiceQuestionTemplate = function () {
    document.getElementById("multiple-choice").click();
    return "inserted";
};

/**
 * Insert fill in the blanks question template to the editor
 */
ustadEditor.insertFillInTheBlanksQuestionTemplate = function () {
    document.getElementById("fill-the-blanks").click();
    return "inserted";
};

/**
 * Start previewing the content
 */
ustadEditor.startContentPreviewing = function () {
    document.getElementById("editor-off").click();
    const returnVal = JSON.stringify({action: 'savePreview', content: btoa(this.activeEditor.getContent())});
    console.log(this.activeEditor.getContent());
    return returnVal;
};

/**
 * Switch ON editing mode
 */
ustadEditor.switchOnEditor = function () {
    document.getElementById("editor-on").click();
};

/**
 * Switch off editing mode
 */
ustadEditor.switchOffEditor = function(){
    ustadEditor.startContentPreviewing();
};

/**
 * Hide toolbar menu after successfully initializing the editor
 */
ustadEditor.hideToolbarMenu = function () {
    $("#ustadmobile-menu").click();
    $("#mceu_0").hide();
    $("#mceu_4").hide();
    $("#mce-editor").on('click',function () {
        $("#mceu_0").hide();
        $("#mceu_4").hide();
    });
};

/**
 * Load local file for editing / previewing
 * @param fileName name of the file to be edited/previewed
 * @param mode operation mode i.e Preview when TRUE otherwise FALSE
 */
ustadEditor.loadLocalFileToEditor = function (fileName, mode) {
    $.ajax({url: "content/"+fileName, success: function(fileContent){
        const container = document.createElement("div");
        container.innerHTML = fileContent;
        const questionList = container.querySelectorAll(".question");
        let questionContent = "";
        for(let question in questionList){
            if(!questionList.hasOwnProperty(question))
                continue;
            questionContent = questionContent + $(questionList[question]).prop('outerHTML');
        }

        tinymce.activeEditor.execCommand('mceInsertContent', false, questionContent,{format: 'raw'});
        ustadEditor.switchOnEditor();
        if(mode === "true"){
            ustadEditor.switchOffEditor();
            tinymce.activeEditor.getBody().setAttribute('contenteditable', false);
        }
    }});
};
/**
 * Load file to the preview container
 * @param fileName
 */
ustadEditor.loadFileForPreview = function (fileName) {
    $.ajax({url: "content/"+fileName, success: function(fileContent){
        document.getElementById("ustad-preview").innerHTML = fileContent;
        QuestionWidget.handleEditOff();
    }});
};




