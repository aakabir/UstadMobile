
let QuestionWidget = function(element) {
   this.element =  $.parseHTML(element);
};

QuestionWidget._widgets = {};
QuestionWidget._widgetListeners = {};
QuestionWidget.WIDGET_NAME_MULTICHOICE = "multichoice";
QuestionWidget.WIDGET_NAME_FILL_BLANKS = "fill-the-blanks";

QuestionWidget.prototype.init = function() {};

QuestionWidget.prototype.editOn = function(){
    $(this.element).find("label").remove();
    $(this.element).find(".question-retry-option")
        .before("<label class='um-labels col-sm-12 col-lg-10'>Can be retried?</label><br/>");
    $(this.element).find(".question-retry-option").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".question-choice-answer").removeClass("hide-element").addClass("show-element");
    $(this.element).find('[data-um-preview="main"]').removeClass("preview-main default-margin-top");
    $(this.element).find('[data-um-preview="alert"]').removeClass("preview-alert default-margin-top");
    $(this.element).find('[data-um-preview="support"]').removeClass("preview-support default-margin-top");
    $(this.element).find(".question-body").before("<label class='um-labels'>Question Text</label><br/>");
};

QuestionWidget.prototype.addListeners = function(){
    $(".question-retry-option select").on("change",this.setRetryOption.bind(this));
};


/**
 * Get node by ID from the Editor question DOM
 * @param id node - question element
 * @returns {*}
 */
QuestionWidget.getById = function(id) {
    if(!QuestionWidget._widgets[id]) {
        const widgetElement = document.getElementById(id);
        const widgetType = widgetElement.getAttribute("data-um-widget");
        switch(widgetType) {
            case MultiChoiceQuestionWidget.WIDGET_NAME_MULTICHOICE:
                QuestionWidget._widgets[id] = new MultiChoiceQuestionWidget(widgetElement);
                break;
            case MultiChoiceQuestionWidget.WIDGET_NAME_FILL_BLANKS:
                QuestionWidget._widgets[id] = new FillTheBlanksQuestionWidget(widgetElement);
                break;
        }
    }

    return QuestionWidget._widgets[id];
};

/**
 * Handle question node as element for editor controls
 * @param serializedNode Serialized node HTMl
 * @returns {*}
 */
QuestionWidget.handleQuestionNode = function (serializedNode) {
    const questionId =  $(serializedNode).attr("id");
    if(!QuestionWidget._widgets[questionId]) {
        const widgetType = $(serializedNode).attr("data-um-widget");
        switch(widgetType) {
            case QuestionWidget.WIDGET_NAME_MULTICHOICE:
                QuestionWidget._widgets[questionId] = new MultiChoiceQuestionWidget(serializedNode);
                break;
            case QuestionWidget.WIDGET_NAME_FILL_BLANKS:
                QuestionWidget._widgets[questionId] = new FillTheBlanksQuestionWidget(serializedNode);
                break;
        }
    }
    return QuestionWidget._widgets[questionId];
};

/**
 * Handle the editor controls when the editing mode is ON i.e attaching listeners
 */
QuestionWidget.handleListeners = function () {
    const questionList = window.document.querySelectorAll(".question");
    for(const question in questionList){
        if(!questionList.hasOwnProperty(question))
            continue;
        const questionElement = questionList[question].outerHTML;
        const questionId =  $(questionElement).attr("id");
        if(!QuestionWidget._widgetListeners[questionId]) {
            const widgetType = $(questionElement).attr("data-um-widget");
            switch(widgetType) {
                case QuestionWidget.WIDGET_NAME_MULTICHOICE:
                    const multiChoice = new MultiChoiceQuestionWidget(questionElement);
                    multiChoice.attachEditListeners();
                    QuestionWidget._widgetListeners[questionId] = multiChoice;
                    break;
                case QuestionWidget.WIDGET_NAME_FILL_BLANKS:
                    const fillTheBlanks = new FillTheBlanksQuestionWidget(questionElement);
                    fillTheBlanks.attachEditListeners();
                    QuestionWidget._widgetListeners[questionId] = fillTheBlanks;
                    break;
            }
        }
    }
};

/**
 * Handle the editor controls when editing mode is OFF ie. attaching listeners
 */
QuestionWidget.handleEditOff = function(){
    const questionList = window.document.querySelectorAll(".question");
    for(const question in questionList){
        if(!questionList.hasOwnProperty(question))
            continue;
        const questionElement = questionList[question].outerHTML;
        const widgetType = $(questionElement).attr("data-um-widget");
        switch(widgetType) {
            case QuestionWidget.WIDGET_NAME_MULTICHOICE:
                const multiChoice = new MultiChoiceQuestionWidget(questionElement);
                multiChoice.attachPreviewListeners();
                break;

            case QuestionWidget.WIDGET_NAME_FILL_BLANKS:
                const fillTheBlanks = new FillTheBlanksQuestionWidget(questionElement);
                fillTheBlanks.attachPreviewListeners();
                break;
        }
    }

};

/**
 * Generate next question ID
 * @param idLength length of the alpha numeric question ID
 * @returns {string} Generated question ID
 */
QuestionWidget.getNextQuestionId = function(idLength = 8){
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let questionId = '';
    for (let i = idLength; i > 0; --i) questionId += chars[Math.floor(Math.random() * chars.length)];
    return questionId;
};


QuestionWidget.prototype.editOff = function(){
    $(this.element).find("label").remove();
    $(this.element).find("br").remove();
    $(this.element).find("button").remove();
    $(this.element).find(".question-choice-answer").remove();
    $(this.element).find(".question-choice-feedback").addClass("hide-element");
    $(this.element).find(".question-feedback-container").addClass("hide-element");
    $(this.element).find(".question-retry").remove();
    return this.element;
};


let FillTheBlanksQuestionWidget = function(element){
    QuestionWidget.apply(this, arguments);
};

let MultiChoiceQuestionWidget = function(element){
    QuestionWidget.apply(this, arguments);
};

/**
 * Make FillTheBlanksQuestionWidget Extends QuestionWidget
 * @type {QuestionWidget}
 */
FillTheBlanksQuestionWidget.prototype = Object.create(QuestionWidget.prototype);

/**
 * Make MultiChoiceQuestionWidget Extends QuestionWidget
 * @type {QuestionWidget}
 */
MultiChoiceQuestionWidget.prototype = Object.create(QuestionWidget.prototype);


/**
 * Attach listeners when editing mode is ON on Multiple choice questions
 */
MultiChoiceQuestionWidget.prototype.attachEditListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
    $(".question-add-choice-btn button.add-choice").on('click', this.addChoice.bind(this));
    $("button.add-choice").on('click', this.addChoice.bind(this));
    $("select").on("change",this.setCorrectChoice.bind(this));
};

/**
 * Attach listeners when editing mode is ON on fill the blanks questions
 */
FillTheBlanksQuestionWidget.prototype.attachEditListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
};


/**
 * Attach listeners when editing mode is OFF on multiple choice questions
 */
MultiChoiceQuestionWidget.prototype.attachPreviewListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
    $(".question-choice-body").on('click', this.handleClickAnswer.bind(this));
    $(".qn-retry").on('click', this.handleClickQuestionRetry.bind(this));
};

/**
 * Attach listeners when editing mode is OFF on fill the blanks questions
 */
FillTheBlanksQuestionWidget.prototype.attachPreviewListeners = function() {
    QuestionWidget.prototype.addListeners.apply(this, arguments);
    $(".fill-the-blanks-check").on('click', this.handleProvidedAnswer.bind(this));
    $(".qn-retry").on('click', this.handleClickQuestionRetry.bind(this));
};


/**
 * Enable editing mode on multiple fill the multiple choice questions
 */
MultiChoiceQuestionWidget.prototype.editOn = function() {
    QuestionWidget.prototype.editOn.apply(this, arguments);
    $(this.element).find("button.btn-default.add-choice").remove();
    $("<button class='btn btn-primary float-right default-margin-top add-choice show-element'>Add Choice</button>").appendTo(this.element);
    $(this.element).find(".question-choice-body").before("<label class='um-labels'>Choice Text</label><br/>");
    $(this.element).find(".question-choice-feedback").before("<label class='um-labels'>Feedback Text</label><br/>");
    $(this.element).find(".question-choice-answer").before("<label class='um-labels'>is right answer?</label><br/>");
    return this.element;
};


/**
 * Enable editing mode on multiple fill the banks questions
 * @returns {Array|*}
 */
FillTheBlanksQuestionWidget.prototype.editOn = function() {
    QuestionWidget.prototype.editOn.apply(this, arguments);
    $(this.element).find(".fill-blanks").removeClass("hide-element").addClass("show-element");
    $(this.element).find(".question-choice-body").before("<label class='um-labels'>Question Answer</label><br/>");
    $(this.element).find(".input-group").before("<label class='um-labels '>Input Answer</label><br/>");
    $(this.element).find(".question-choice-feedback-correct").before("<label class='um-labels'>Right input feedback</label><br/>");
    $(this.element).find(".question-choice-feedback-wrong").before("<label class='um-labels'>Wrong input feedback</label><br/>");
    return this.element;
};


/**
 * Add choice to the choice list
 * @param event
 */
MultiChoiceQuestionWidget.prototype.addChoice = function(event) {
    const choiceUiHolder = "<div class=\"question-choice\" data-um-correct=\"false\">" +
        "<label class=\"um-labels\">Choice Text</label><br>" +
        "<div class=\"question-choice-body\" data-um-preview=\"support\">B. Dar es salaam</div>" +
        "<label class=\"um-labels\">Feedback Text</label><br>" +
        "<div class=\"question-choice-feedback\" data-um-edit-only=\"true\">" +
        "What a choice, you need to read more about this country.</div>" +
        "<label class=\"um-labels\">is right answer?</label><br>" +
        "<div class=\"question-choice-answer select-option col-sm-3 show-element col-lg-3\">" +
        "<select><option value=\"true\">Yes</option>" +
        "<option selected=\"selected\" value=\"false\">No</option>" +
        "</select></div></div>";
    this.editOn();
    $(event.target).prev().prev().before(choiceUiHolder);
};

/**
 * Check if the selected question answer is correct
 * @param event
 */
MultiChoiceQuestionWidget.prototype.handleClickAnswer = function(event) {
    const choiceElement = $(event.target).closest("div .question-choice");
    const questionElement = $(event.target).closest("div div.question");
    const isCorrectChoice = choiceElement.attr("data-um-correct")==='true';
    const feedbackText = $(choiceElement).find(".question-choice-feedback").text();
    const feedbackContainer = $(questionElement).find(".question-feedback-container");
    $(feedbackContainer).find(".question-feedback-container-text").html(feedbackText);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning");
    $(feedbackContainer).addClass((isCorrectChoice ? "alert-success":"alert-danger")+ " show-element");
    const canBeRetried = questionElement.attr("data-um-retry")==='true';
    if(!isCorrectChoice && canBeRetried){
        $(questionElement).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }
};


/**
 * Handle when user fill the answer on the input filed
 * @param event
 */
FillTheBlanksQuestionWidget.prototype.handleProvidedAnswer = function(event){
    const questionElement = $(event.target).closest("div div.question");
    const choiceElement = $(questionElement).find(".fill-blanks");
    const wrongChoiceText = $(choiceElement).find(".question-choice-feedback-wrong").text();
    const correctChoiceText = $(choiceElement).find(".question-choice-feedback-correct").text();
    const defaultAnswerText = $(choiceElement).find(".question-choice-body").text().toLowerCase();
    const userAnswerText = $(questionElement).find(".fill-the-blanks-input").val().toLowerCase();
    const feedbackContainer = $(questionElement).find(".question-feedback-container");
    const isCorrectChoice = defaultAnswerText === userAnswerText;
    const message = userAnswerText.length > 0 ?
        (isCorrectChoice ? correctChoiceText: wrongChoiceText):"<b>Sorry!</b> please type your answer before submitting it";
    $(feedbackContainer).find(".question-feedback-container-text").html(message);
    $(feedbackContainer).removeClass("hide-element show-element alert-success alert-danger alert-warning");
    $(feedbackContainer).addClass(userAnswerText.length > 0 ? (isCorrectChoice ? "alert-success":"alert-danger")
        :"alert-info"+ " show-element");
    const canBeRetried = questionElement.attr("data-um-retry")==='true';
    if((!isCorrectChoice  || userAnswerText.length <= 0) && canBeRetried){
        $(questionElement).find(".question-retry-btn").removeClass("hide-element").addClass("show-element");
    }
};

/**
 * Set correct question choice
 * @param event
 */
MultiChoiceQuestionWidget.prototype.setCorrectChoice = function(event){
    $(event.target).closest("div .question-choice").attr("data-um-correct",$(event.target).val());
};


/**
 * Add retry button on question which was set to be retried
 * @param event onChange event from selector
 */
QuestionWidget.prototype.setRetryOption = function(event){
    const questionElement = $(event.target).closest("div div.question");
    const canBeRetried = $(event.target).val() === 'true';
    $(questionElement).attr("data-um-retry",canBeRetried);
    $(this.element).find("br").remove();
};

/**
 * Handle when retry button is clicked (Hide feedback box and retry button)
 * @param event onClick event from button
 */
QuestionWidget.prototype.handleClickQuestionRetry = function(event){
    const questionElement = $(event.target).closest("div.question");
    $(questionElement).find(".question-feedback-container").removeClass("show-element").addClass("hide-element");
    $(questionElement).find(".question-retry-btn").removeClass("show-element").addClass("hide-element");
};



$(function() {
    console.log("look for all DOM element")
});


