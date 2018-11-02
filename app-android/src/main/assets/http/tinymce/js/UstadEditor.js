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
 * Initialize tinymce on a document
 */
ustadEditor.initTinyMceEditor = function(){
    const inlineConfig = {
        selector: '.container-fluid',
        menubar: false,
        inline: true,
        plugins: ['ustadmobile','directionality','lists','noneditable','visualblocks'],
        toolbar: ['ustadmobile'],
        valid_styles: {
            '*': 'font-size,font-family,color,text-decoration,text-align'
        },
        content_css: [
            '//fonts.googleapis.com/css?family=Lato:300,300i,400,400i',
        ],
        extended_valid_elements : 'label[class],div[onclick|class|data-um-correct|data-um-widget|id],option[selected|value]',
        setup:function(ed) {
            /*ed.on('change', function() {
                ustadEditor.hideToolbarMenu();
                QuestionWidget.handleListeners();
                ustadEditor.handleContentChange();
            });

            ed.on('keyup', function() {
                ustadEditor.handleContentChange();
                setTimeout(ustadEditor.hideToolbarMenu(), 22);
            });

            ed.on('undo', function() {
                ustadEditor.handleContentChange();
                setTimeout(ustadEditor.hideToolbarMenu(), 22);
            });

            ed.on('redo', function() {
                ustadEditor.handleContentChange();
                setTimeout(ustadEditor.hideToolbarMenu(), 22);
            });*/

        },
        style_formats: [{ title: 'Containers', items: [
                { title: 'section', block: 'section', wrapper: true, merge_siblings: false },
                { title: 'article', block: 'article', wrapper: true, merge_siblings: false },
                { title: 'blockquote', block: 'blockquote', wrapper: true },
                { title: 'hgroup', block: 'hgroup', wrapper: true },
                { title: 'aside', block: 'aside', wrapper: true },
                { title: 'figure', block: 'figure', wrapper: true }
            ]
        }]
    };
    try{
        tinymce.init(inlineConfig).then(function () {
            ustadEditor.init(tinymce.activeEditor);
           /* QuestionWidget.handleEditOn();
            QuestionWidget.handleListeners();
            setTimeout(ustadEditor.requestFocus(), 20);
            setTimeout(ustadEditor.hideToolbarMenu(), 22);
            setTimeout(ustadEditor.switchOnEditorController());
            console.log(JSON.stringify({action:'onInitEditor',content:"true"}));*/

        });
    }catch (e) {
        console.log(JSON.stringify({action:'exception',content:e}))
    }
};

/**
 * Add labels and texts on the document when question templates is used
 */
ustadEditor.switchOnEditorController = function(){
    try{
        document.getElementById("editor-on").click();
    }catch (e) {
        console.log("switchOnEditorController:",e);
    }
};


/**
 * Get content from the active content editor
 * @returns {*|void}
 */
ustadEditor.getContent = function(){
    return {action:'getContent',content:btoa(this.activeEditor.getContent())};
};

/**
 * Request focus to the active editor
 * @returns {boolean}
 */
ustadEditor.requestFocus = function () {
    this.activeEditor.execCommand("mceFocus",false,null);
    return this.isToolBarButtonActive("mceFocus");
};

/**
 * Hide toolbar menu after successfully initializing the editor
 */
ustadEditor.hideToolbarMenu = function () {
    try{
        $("#ustadmobile-menu").click();
        $("#mceu_0").hide();
        $("#mceu_4").hide();
        $("#mce-editor").on('click',function () {
            $("#mceu_0").hide();
            $("#mceu_4").hide();
        });
    }catch (e) {
        console.log("hideToolbarMenu:",e)
    }
};

/**
 * Insert multiple choice question template to the editor
 */
ustadEditor.insertMultipleChoiceQuestionTemplate = function () {
    try{
        document.getElementById("multiple-choice").click();
        return "inserted multiple choice question";
    }catch (e) {
        console.log(e);
        return null;
    }
};


/**
 * Insert fill in the blanks question template to the editor
 */
ustadEditor.insertFillInTheBlanksQuestionTemplate = function () {
    try{
        document.getElementById("fill-the-blanks").click();
        return "inserted fill the blanks question";
    }catch (e) {
        console.log(e);
        return null;
    }
};


/**
 * Insert multimedia content to the editor
 * @param source media absolute path
 * @param mimeType media mime type
 */
ustadEditor.insertMedia = function(source,mimeType){
    const width = $(window).width();
    let mediaContent = null;
    if(mimeType.includes("image")){
        mediaContent = "<img src=\""+source+"\" class=\"um-media img-fluid\" width=\""+width+"\"/>";
    }else if(mimeType.includes("audio")){
        mediaContent =
            "<video controls class='um-media img-fluid media-audio'>" +
            "    <source src=\""+source+"\" type=\""+mimeType+"\">" +
            " Your browser does not support the "+
            "</video>";
    }else{
        mediaContent =
            "<video controls class='um-media img-fluid' width='"+width+"'>" +
            "    <source src=\""+source+"\" type=\""+mimeType+"\">" +
            "</video>";
    }
    this.executeRawContent("<p>"+mediaContent+"</p>");
};


/**
 * Insert raw content to the active editor
 * @param content content to be inserted
 */
ustadEditor.executeRawContent= function(content){
    this.activeEditor.execCommand('mceInsertContent', false, content,{format: 'raw'});
};


/**
 * Callback to listen for any changes on the active editor
 */
ustadEditor.handleContentChange = function(){
    console.log(JSON.stringify({action:'onContentChanged',content:btoa(this.activeEditor.getContent())}));
};


/**
 * Load content into a preview
 * @param fileContent content to be manipulated for preview
 */
ustadEditor.loadContentForPreview = function (fileContent) {
    const editorContent = $("<div/>").html($.parseHTML(atob(fileContent)));
    $(editorContent).find("br").remove();
    $(editorContent).find("label").remove();
    $(editorContent).find("button.add-choice").remove();
    $(editorContent).find('div.question-choice').addClass("question-choice-pointer").removeClass("default-margin-top");
    $(editorContent).find('div.multichoice').addClass("default-margin-bottom").removeClass("default-margin-top");
    $(editorContent).find('div.select-option').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.fill-blanks').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice-answer').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('.question-retry-btn').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice-feedback').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-answer').addClass("hide-element").removeClass("show-element");

    $(editorContent).find('div.question').addClass('card col-sm-12 col-lg-12 default-padding-bottom default-margin-bottom default-padding-top');
    $(editorContent).find('div.question-choice').addClass('alert alert-secondary');
    $(editorContent).find('[data-um-preview="main"]').addClass('preview-main default-margin-top');
    $(editorContent).find('[data-um-preview="alert"]').addClass('preview-alert default-margin-top');
    $(editorContent).find('[data-um-preview="support"]').addClass('preview-support default-margin-top');
    return {action:'onSaveContent',content:btoa($('<div/>').html(editorContent).contents().html())};
};




/**
 * Changing editor editing mode
 */
ustadEditor.changeEditorMode = function(mode){
    if(mode === 'off'){
        this.activeEditor.getBody().setAttribute('contenteditable', false);
    }else{
        this.activeEditor.getBody().setAttribute('contenteditable', true);
    }
    return mode;
};


/**
 * Create new file with our basic document template.
 * @param content content for preview
 * @returns {Promise<void>} File generation promise
 */
ustadEditor.generateNewFile = async function(content){
    let promise = new Promise((resolve) => {
        $.ajax({url: "templates/stand-alone-file.html", success: function(fileContent){
                const fileContentParts = fileContent.split("<template/>");
                const standAloneFileContent = fileContentParts[0]+atob(content)+fileContentParts[1];
                resolve(standAloneFileContent);
            }});
    });

    const result = await promise;
    console.log(JSON.stringify({action:'newFile',content:btoa(result),extraFlag:null}));
};





/**
 * Start content live preview on the editor
 */
ustadEditor.startLivePreview = function () {
    try{
        document.getElementById("editor-off").click();
        return {action: 'savePreview', content: btoa(this.activeEditor.getContent()),extraFlag:null};
    }catch (e) {
        console.log("startLivePreview",e)
    }
};

/**
 * Start content preview away from the editor
 * @param content content to be previewed
 */
ustadEditor.startLocalPreview = function(content){
    document.getElementById("ustad-preview").innerHTML = atob(content);
    QuestionWidget.handleEditOff();
};









