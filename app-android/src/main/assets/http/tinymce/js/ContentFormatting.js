function ContentFormatting() {if (!(this instanceof ContentFormatting))
    return new ContentFormatting();}

const formatting = new ContentFormatting();
let activeEditor = null;

/**
 * Initialize ContentFormatting with active editor instance
 * @param activeEditor TinyMCE active editor instance
 */
formatting.init = function(activeEditor){
    this.activeEditor = activeEditor;
    console.log("ContentFormatting controls initialized");
};

/**
 * Check if a toolbar button is active or not
 * @param buttonIdentifier Command identifier as found in documentation
 * {@link https://www.tiny.cloud/docs/advanced/editor-command-identifiers/}
 * @returns {boolean} TRUE if is active otherwise FALSE
 */
formatting.isToolBarButtonActive = function(buttonIdentifier){
    return this.activeEditor.queryCommandState(buttonIdentifier) != null
};

/**
 * Get content of the active Editor
 * @returns {*|void} Current content on editor
 */
formatting.getActiveEditorContent = function(){
    return this.activeEditor.getContent();
};

/**
 * Change editor content font size
 * @param fontSize Size to change to
 * @returns {string | * | void}
 */
formatting.setFontSize = function(fontSize){
    this.activeEditor.execCommand("FontSize",false,""+fontSize+"pt");
    return this.activeEditor.queryCommandValue("FontSize");
};

/**
 * Undo previously performed action
 * @returns {boolean} TRUE if succeed FALSE otherwise
 */
formatting.editorActionUndo = function(){
    this.activeEditor.execCommand("Undo",false,null);
    return this.isToolBarButtonActive("Undo");
};

/**
 * Redo previously performed action
 * @returns {boolean} TRUE if succeed FALSE otherwise
 */
formatting.editorActionRedo = function(){
    this.activeEditor.execCommand("Redo",false,null);
    return this.isToolBarButtonActive("Redo");
};

/**
 * Set text direction from Left to Right
 * @returns {boolean} TRUE if direction changed otherwise FALSE
 */
formatting.textDirectionLeftToRight = function(){
    this.activeEditor.execCommand("mceDirectionLTR",false,null);
    return this.isToolBarButtonActive("mceDirectionLTR");
};

/**
 * Set text direction from Right to Left
 * @returns {boolean} TRUE if direction changed otherwise FALSE
 */
formatting.textDirectionRightToLeft = function(){
    this.activeEditor.execCommand("mceDirectionRTL",false,null);
    return this.isToolBarButtonActive("mceDirectionRTL");
};

/**
 * Remove or insert un-ordered list
 * @returns {boolean} TRUE inserted and FALSE otherwise
 */
formatting.paragraphUnOrderedListFormatting = function(){
    this.activeEditor.execCommand("InsertUnorderedList",false,null);
    return this.isToolBarButtonActive("InsertUnorderedList");
};

/**
 * Remove or insert ordered list
 * @returns {boolean} TRUE inserted and FALSE otherwise
 */
formatting.paragraphOrderedListFormatting = function(){
    this.activeEditor.execCommand("InsertOrderedList",false,null);
    return this.isToolBarButtonActive("InsertOrderedList");
};

/**
 * Justify left editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
formatting.paragraphLeftJustification = function(){
    this.activeEditor.execCommand("JustifyLeft",false,null);
    return this.isToolBarButtonActive("JustifyLeft");
};


/**
 * Justify left editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
formatting.paragraphRightJustification = function(){
    this.activeEditor.execCommand("JustifyRight",false,null);
    return this.isToolBarButtonActive("JustifyRight");
};

/**
 * Justify fully editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
formatting.paragraphFullJustification = function(){
    this.activeEditor.execCommand("JustifyFull",false,null);
    return this.isToolBarButtonActive("JustifyFull");
};

/**
 * Justify center editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
formatting.paragraphCenterJustification = function(){
    this.activeEditor.execCommand("JustifyCenter",false,null);
    return this.isToolBarButtonActive("JustifyCenter");
};


/**
 * Indent editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
formatting.paragraphOutDent = function(){
    this.activeEditor.execCommand("Outdent",false,null);
    return this.isToolBarButtonActive("Outdent");
};

/**
 * Indent editor content
 * @returns {boolean} TRUE if justified FALSE otherwise
 */
formatting.paragraphIndent = function(){
    this.activeEditor.execCommand("Indent",false,null);
    return this.isToolBarButtonActive("Indent");
};

/**
 * Apply bold formatting to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
formatting.textFormattingBold = function(){
    this.activeEditor.execCommand("Bold",false,null);
    return this.isToolBarButtonActive("Bold");
};

/**
 * Apply italic formatting to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
formatting.textFormattingItalic = function(){
    this.activeEditor.execCommand("Italic",false,null);
    return this.isToolBarButtonActive("Italic");
};

/**
 * Apply underline formatting to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
formatting.textFormattingUnderline = function(){
    this.activeEditor.execCommand("Underline",false,null);
    return this.isToolBarButtonActive("Underline");
};

/**
 * Apply strike-through formatting to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
formatting.textFormattingStrikeThrough = function(){
    this.activeEditor.execCommand("Strikethrough",false,null);
    return this.isToolBarButtonActive("Strikethrough");
};

/**
 * Apply superscript formatting to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
formatting.textFormattingSuperScript = function(){
    this.activeEditor.execCommand("Superscript",false,null);
    return this.isToolBarButtonActive("Superscript");
};

/**
 * Apply subscript formatting to a text
 * @returns {boolean} TRUE if applied otherwise FALSE
 */
formatting.textFormattingSubScript = function(){
    this.activeEditor.execCommand("Subscript",false,null);
    return this.isToolBarButtonActive("Subscript");
};

/**
 * Get content from the active content edtor
 * @returns {*|void}
 */
formatting.getContent = function(){
    return this.activeEditor.getContent().getHtml();
};

/**
 * Request formatting focus to the active editor
 * @returns {boolean}
 */
formatting.requestFocus = function () {
    this.activeEditor.execCommand("mceFocus",false,null);
    return this.isToolBarButtonActive("mceFocus")
};




