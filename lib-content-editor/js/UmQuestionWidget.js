
let UmQuestionWidget = function(element) {
    this.element =  $.parseHTML(element);
};


/**
 * English constant placeholders
 * */
UmQuestionWidget.PLACEHOLDERS_LABELS = {
    labelForQuestionBodyText: 'Question text',
    labelForAddChoiceBtn: 'Add choice',
    labelForChoiceBodyText: 'Choice text',
    labelForFeedbackBodyText: 'Feedback text',
    labelForQuestionRetryOption: 'Can this be retried?',
    labelForRightAnswerOption: 'Is this the right answer?',
    labelForFillTheBlanksAnswerBodyText: 'Question answer',
    labelForFillTheBlanksPromptInput: 'Input answer',
    labelForQuestionRightFeedbackText: 'Right input feedback',
    labelForQuestionWrongFeedbackText: 'Wrong input feedback',
    labelForTrueOptionText:'Yes',
    labelFalseOptionText: 'No',
    labelForCheckAnswerInputPromptBtn: 'Check answer',
    labelForTryAgainOptionBtn:'Try again',
    placeholderForTheQuestionText: 'Placeholder text for the question body',
    placeholderForTheChoiceText: 'Placeholder text for the choice body',
    placeholderForTheBlanksInput: 'Type answer here',
    placeholderForTheChoiceFeedback: 'Feedback placeholder text',
    placeholderForTheRightChoiceFeedback: 'Feedback placeholder text for the right choice',
    placeholderForTheWrongChoiceFeedback: 'Feedback placeholder text for the wrong choice',
    warningOnSubmitEmptyField: '<b>Whoops!</b> Please type your answer before you press submit',
};

UmQuestionWidget.QUESTION_ID_TAG = "question-";

UmQuestionWidget.CHOICE_ID_TAG = "choice-";

UmQuestionWidget._widgets = {};

UmQuestionWidget._widgetListeners = {};

UmQuestionWidget.isNewQuestion = false;

UmQuestionWidget.isEditingMode = false;

UmQuestionWidget.WIDGET_NAME_MULTICHOICE = "multi-choice";

UmQuestionWidget.WIDGET_NAME_FILL_BLANKS = "fill-the-blanks";

let UmFillTheBlanksQuestionWidget = function(element){UmQuestionWidget.apply(this, arguments);};

let UmMultiChoiceQuestionWidget = function(element){UmQuestionWidget.apply(this, arguments);};

UmMultiChoiceQuestionWidget.prototype = Object.create(UmQuestionWidget.prototype);

UmFillTheBlanksQuestionWidget.prototype = Object.create(UmQuestionWidget.prototype);


UmQuestionWidget.setQuestionStatus = (isNewQuestion) =>{
    UmQuestionWidget.isNewQuestion = isNewQuestion;
};

UmQuestionWidget.setEditingMode = (isEditingMode) => {
    UmQuestionWidget.isEditingMode = isEditingMode;
};

UmQuestionWidget.saveContentEditor = (content) => {
    let editorContent = $("<div/>").html($.parseHTML(atob(content)));
    $(editorContent).find("br").remove();
    $(editorContent).find("label").remove();
    $(editorContent).find("p.pg-break").remove();
    $(editorContent).find("button.add-choice").remove();
    $(editorContent).find('div.question-choice').addClass("question-choice-pointer").removeClass("default-margin-top");
    $(editorContent).find('div.multi-choice').addClass("default-margin-bottom").removeClass("default-margin-top");
    $(editorContent).find('div.select-option').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.fill-blanks').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice-answer').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('.question-retry-btn').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice-feedback').addClass("hide-element").removeClass("show-element");
    $(editorContent).find('div.question-choice').addClass('alert alert-secondary');
    $(editorContent).find('p.pg-break').addClass('hide-element');
    $(editorContent).find('button.btn-delete').removeClass("show-element").addClass('hide-element');
    $(editorContent).find('.default-theme').removeClass('no-padding');
    $(editorContent).find('.question-body').addClass("default-margin-bottom no-left-padding");
    $(editorContent).find('.question-answer').addClass("no-padding no-left-padding default-margin-bottom");
    $(editorContent).find('.fill-answer-inputs').addClass("no-padding");
    $(editorContent).find('[data-um-preview="main"]').addClass('preview-main default-margin-top');
    $(editorContent).find('[data-um-preview="alert"]').addClass('preview-alert default-margin-top');
    $(editorContent).find('[data-um-preview="support"]').addClass('preview-support default-margin-top');
    $(editorContent).find('div.question').addClass('card default-padding-top default-padding-bottom');
    editorContent = $('<div/>').html(editorContent).contents().html();
    return editorContent;
};


UmQuestionWidget.getNextQuestionId = (idLength = 8) =>{
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let questionId = '';
    for (let i = idLength; i > 0; --i) questionId += chars[Math.floor(Math.random() * chars.length)];
    return questionId;
};


UmQuestionWidget.handleQuestionNode =  (questionNode) => {
    const questionId =  $(questionNode).attr("id");
    if(!UmQuestionWidget._widgets[questionId]) {
        const widgetType = $(questionNode).attr("data-um-widget");
        switch(widgetType) {
            case UmQuestionWidget.WIDGET_NAME_MULTICHOICE:
                UmQuestionWidget._widgets[questionId] = new UmMultiChoiceQuestionWidget(questionNode);
                break;
            case UmQuestionWidget.WIDGET_NAME_FILL_BLANKS:
                UmQuestionWidget._widgets[questionId] = new UmFillTheBlanksQuestionWidget(questionNode);
                break;
        }
    }
    return UmQuestionWidget._widgets[questionId];
};


/*Widget start editing starts*/
UmQuestionWidget.prototype.startEditing = function() {
    $(this.element).find("label").remove();
    $(this.element).find(".btn-delete").removeClass("hide-element").addClass("show-element").html(' <span aria-hidden="true">&times;</span>');
    $(this.element).find(".question-body").removeClass("default-margin-bottom").before("<label class='um-labels'>"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionBodyText+"</label><br/>");
    $(this.element).find(".question-retry-btn").html("<button class='btn btn-dark black float-right qn-retry' data-um-preview='support'>"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForTryAgainOptionBtn+"</button>");

    if(UmQuestionWidget.isNewQuestion){
        $(this.element).find(".question-body").html(UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheQuestionText);
        $(this.element).find(".question-choice-body").html(UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceText);
        $(this.element).find(".question-choice-feedback").html(UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceFeedback);
        $(this.element).find(".question-choice-feedback-correct").html(UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheRightChoiceFeedback);
        $(this.element).find(".question-choice-feedback-wrong").html(UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheWrongChoiceFeedback);
        $(this.element).find(".fill-the-blanks-check").text(UmQuestionWidget.PLACEHOLDERS_LABELS.labelForCheckAnswerInputPromptBtn);
        $(this.element).find(".fill-the-blanks-input").attr("placeholder",UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheBlanksInput);
    }else{
        $(this.element).find(".question-choice").removeClass("question-choice-pointer selected-choice alert alert-secondary").addClass("default-margin-top");
        $(this.element).find('[data-um-preview="main"]').removeClass("preview-main default-margin-top");
        $(this.element).find('[data-um-preview="alert"]').removeClass("preview-alert default-margin-top");
        $(this.element).find('[data-um-preview="support"]').removeClass("preview-support default-margin-top");
        $(this.element).find(".default-theme").addClass("no-padding");
        $(this.element).find(".question-feedback-container").addClass("hide-element").removeClass("show-element");
        $(this.element).removeClass("card default-margin-bottom default-padding-top");
        $(this.element).find(".multi-choice").removeClass("default-margin-top").addClass("default-margin-bottom");
        $(this.element).find(".question-answer").removeClass("default-margin-bottom");
        $(this.element).find("p.pg-break").addClass("show-element").removeClass("hide-element");
        $(this.element).find("button.btn-delete").addClass("show-element").removeClass("hide-element");
        $(this.element).find(".select-option").addClass("show-element").removeClass("hide-element");
        $(this.element).find(".fill-blanks").addClass("show-element").removeClass("hide-element");
        $(this.element).find(".question-choice-answer").addClass("show-element").removeClass("hide-element");
        $(this.element).find(".question-choice-feedback").addClass("show-element").removeClass("hide-element");
    }

    //$(this.element).find("button.qn-retry").css("color","white").css("margin-right","-30px").addClass(".default-margin-top");

    $(this.element).find(".question-retry-option").html("" +
        "<select class='question-retry-option-select col-sm-12'>" +
        "  <option value=\"true\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForTrueOptionText+"</option>" +
        "  <option value=\"false\" selected=\"selected\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelFalseOptionText+"</option>" +
        "</select>");
    $(this.element).find(".question-choice-answer").html("" +
        "<select class='question-choice-answer-select col-sm-12'>" +
        "  <option value=\"true\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForTrueOptionText+"</option>" +
        "  <option value=\"false\" selected=\"selected\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelFalseOptionText+"</option>" +
        "</select>");

    $(this.element).find(".question-retry-option")
        .before("<label class='um-labels no-left-padding'>"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRetryOption+"</label><br/>");


    return this.element;
};


UmMultiChoiceQuestionWidget.prototype.startEditing = function(activeEditor){
    UmQuestionWidget.prototype.startEditing.apply(this, arguments);
    $(this.element).find("button.btn-default.add-choice").remove();
    $("<button class='btn btn-primary float-right default-margin-top add-choice show-element'>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForAddChoiceBtn+"</button>").appendTo(this.element);
    $(this.element).find(".question-choice-body").before("<label class='um-labels'>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForChoiceBodyText+"</label><br/>");
    $(this.element).find(".question-choice-feedback").before("<label class='um-labels'>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFeedbackBodyText+"</label><br/>");
    $(this.element).find(".question-choice-answer").before("<label class='um-labels'>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForRightAnswerOption+"</label><br/>");
    const choices = $(this.element).find(".question-choice");
    for(let choice in choices){
        if(!choices.hasOwnProperty(choice))
            continue;
        if($(choices[choice]).hasClass("question-choice")){
            $(choices[choice]).attr("id",UmQuestionWidget.CHOICE_ID_TAG+ UmQuestionWidget.getNextQuestionId());
        }

    }

    return this.element;
};


UmFillTheBlanksQuestionWidget.prototype.startEditing = function(activeEditor){
    UmQuestionWidget.prototype.startEditing.apply(this, arguments);
    $(this.element).find(".fill-blanks").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".question-choice-body").before("<label class='um-labels'>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksAnswerBodyText+"</label><br/>");
    $(this.element).find(".input-group").before("<label class='um-labels '>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFillTheBlanksPromptInput+"</label><br/>");
    $(this.element).find(".question-choice-feedback-correct").before("<label class='um-labels'>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionRightFeedbackText+"</label><br/>");
    $(this.element).find(".question-choice-feedback-wrong").before("<label class='um-labels'>"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.labelForQuestionWrongFeedbackText+"</label><br/>");
    return this.element;
};
/*Widget start editing ends*/


/*Widget events listeners starts*/
UmQuestionWidget.handleWidgetListeners = () => {
    const bodySelector = $('body');
    bodySelector.off('click').off('change');
    //Click events
    bodySelector.on('click', event => {
        if($(event.target).hasClass("qn-retry")){
            UmQuestionWidget.prototype.onQuestionRetryButtonClicked(event);
        }else if($(event.target).hasClass("fill-the-blanks-check")){
            if(!UmQuestionWidget.isEditingMode){
                UmFillTheBlanksQuestionWidget.prototype.onQuestionAnswerChecked(event);
            }
        }else if($(event.target).hasClass("add-choice")){
            UmMultiChoiceQuestionWidget.prototype.addChoice(event);
        }else if($(event.target).hasClass("btn-delete") || $(event.target).is("span")){
            UmQuestionWidget.prototype.onQuestionDeletion(event);
        }else if($(event.target).hasClass("question-choice")){
           if(!UmQuestionWidget.isEditingMode){
               UmMultiChoiceQuestionWidget.prototype.onQuestionAnswerChecked(event);
           }
        }
    });

    //Option change events
    bodySelector.on("change", event => {
        if($(event.target).hasClass("question-choice-answer-select")){
           UmMultiChoiceQuestionWidget.prototype.onChoiceStateChange(event);
        }else if($(event.target).hasClass("question-retry-option-select")){
            UmQuestionWidget.prototype.onQuestionRetrySelectionChange(event);
        }

    });
};
/*Widget events listeners ends*/


/*Widget event handling starts*/
UmMultiChoiceQuestionWidget.prototype.addChoice = function(event){
    const choiceUiHolder = "<div class=\"question-choice\" data-um-correct=\"false\" data-um-preview=\"support\" id='"
        +UmQuestionWidget.CHOICE_ID_TAG+UmQuestionWidget.getNextQuestionId()+"'>" +
        "<label class=\"um-labels\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForChoiceBodyText+"</label><br>" +
        "<div class=\"question-choice-body\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceText+"</div>" +
        "<label class=\"um-labels\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForFeedbackBodyText+"</label><br>" +
        "<div class=\"question-choice-feedback\" data-um-edit-only=\"true\">"
        +UmQuestionWidget.PLACEHOLDERS_LABELS.placeholderForTheChoiceFeedback+"</div>" +
        "<label class=\"um-labels\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForRightAnswerOption+"</label><br>" +
        "<div class=\"question-choice-answer select-option col-sm-3 show-element col-lg-3\">" +
        "<select><option value=\"true\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelForTrueOptionText+"</option>" +
        "<option selected=\"selected\" value=\"false\">"+UmQuestionWidget.PLACEHOLDERS_LABELS.labelFalseOptionText+"</option>" +
        "</select></div></div>";
    $(event.target).prev().prev().before(choiceUiHolder);
};


UmMultiChoiceQuestionWidget.prototype.onQuestionAnswerChecked = function(event){
    const choiceElement = $(event.target).closest("div .question-choice");
    const questionElement = $(event.target).closest("div div.question");

    const allChoices = $(questionElement).find("[data-um-correct]");
    for(let choice in allChoices){
        if(!allChoices.hasOwnProperty(choice))
            continue;
        const choiceNode = allChoices[choice];
        if($(choiceNode).hasClass("question-choice-pointer")){
            const isClicked = $(choiceNode).attr("id") === $(choiceElement).attr("id");
            if(isClicked){
                $(choiceNode).addClass("selected-choice");
            }else{
                $(choiceNode).removeClass("selected-choice");
            }
        }
    }
    const isCorrectChoice = choiceElement.attr("data-um-correct")==='true';
    const feedbackText = $(choiceElement).find(".question-choice-feedback").html();
    const feedbackContainer = $(questionElement).find(".question-feedback-container");
    $(feedbackContainer).find(".question-feedback-container-text").html(feedbackText);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning");
    $(feedbackContainer).addClass((isCorrectChoice ? "alert-success":"alert-danger")+ " show-element");
    const canBeRetried = questionElement.attr("data-um-retry")==='true';
    if(!isCorrectChoice && canBeRetried){
        $(questionElement).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }

    if(isCorrectChoice){
        $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
    }
};

UmFillTheBlanksQuestionWidget.prototype.onQuestionAnswerChecked = (event) =>{
    const questionElement = $(event.target).closest("div div.question");
    const choiceElement = $(questionElement).find(".fill-blanks");
    const wrongChoiceText = $(choiceElement).find(".question-choice-feedback-wrong").html();
    const correctChoiceText = $(choiceElement).find(".question-choice-feedback-correct").html();
    let defaultAnswerText = $(choiceElement).find(".question-choice-body").text().toLowerCase();
    let userAnswerText = $(questionElement).find(".fill-the-blanks-input").val().toLowerCase();
    const feedbackContainer = $(questionElement).find(".question-feedback-container");
    userAnswerText = userAnswerText.replace(/(\r\n|\n|\r)/gm,"").replace(/\s/g, "");
    defaultAnswerText = defaultAnswerText.replace(/(\r\n|\n|\r)/gm,"").replace(/\s/g, "");

    const isCorrectChoice = defaultAnswerText === userAnswerText;
    const message = userAnswerText.length > 0 ?
        (isCorrectChoice ? correctChoiceText: wrongChoiceText):UmQuestionWidget.PLACEHOLDERS_LABELS.warningOnSubmitEmptyField;
    $(feedbackContainer).find(".question-feedback-container-text").html(message);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning alert-info");
    $(feedbackContainer).addClass(userAnswerText.length > 0 ? (isCorrectChoice ? "alert-success":"alert-danger")
        :"alert-info"+ " show-element");
    const canBeRetried = questionElement.attr("data-um-retry")==='true';
    if((!isCorrectChoice && canBeRetried) || userAnswerText.length <= 0){
        $(questionElement).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }

    if(isCorrectChoice){
        $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
    }
};


UmMultiChoiceQuestionWidget.prototype.onChoiceStateChange = (event) => {
    $(event.target).closest("div .question-choice").attr("data-um-correct",$(event.target).val());
};


UmQuestionWidget.prototype.onQuestionRetrySelectionChange = (event) => {
    const questionElement = $(event.target).closest("div div.question");
    const canBeRetried = $(event.target).val() === 'true';
    $(questionElement).attr("data-um-retry",canBeRetried);
    $(this.element).find("br").remove();
};

UmQuestionWidget.prototype.onQuestionRetryButtonClicked = (event) => {
    const questionElement = $(event.target).closest("div.question");
    $(questionElement).find(".question-choice-pointer").removeClass("selected-choice");
    $(questionElement).find(".question-feedback-container").removeClass("show-element").addClass("hide-element");
    $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
};


UmQuestionWidget.prototype.onQuestionDeletion = (event) => {
    const questionElement = $(event.target).closest("div div.question");
    $(questionElement).nextUntil(".question").remove();
    $(questionElement).remove();

    if($.find(".question").length === 0){
        $("p.pg-break").nextUntil(".question").remove();
    }
};

/*Widget event handling ends*/
