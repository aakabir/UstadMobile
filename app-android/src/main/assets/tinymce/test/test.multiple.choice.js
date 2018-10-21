/**
 * Test cases to make sure multiple choice question logic behaves a sexpected
 */
chai.should();
describe('Inserting a question', function() {
    describe('#Bold', function() {
        it('Should insert new question template', function() {
            const totalQuestions = 1;
            const formatApplied = formatting.textFormattingBold();
            const questionList = document.querySelectorAll(".question");

            totalQuestions.should.equal(questionList.length);
        });
    });

    describe('#Italic', function() {
        it('Should apply italic to the active editor', function() {
            const formatApplied = formatting.textFormattingItalic();
            formatApplied.should.equal(true);
        });
    });

    describe('#Underline', function() {
        it('Should apply underline to the active editor', function() {
            const formatApplied = formatting.textFormattingUnderline();
            formatApplied.should.equal(true);
        });
    });

    describe('#StrikeThrough', function() {
        it('Should apply strike-through to the active editor', function() {
            const formatApplied = formatting.textFormattingStrikeThrough();
            formatApplied.should.equal(true);
        });
    });

    describe('#Superscript', function() {
        it('Should apply superscript to the active editor', function() {
            const formatApplied = formatting.textFormattingSuperScript();
            formatApplied.should.equal(true);
        });
    });

    describe('#Subscript', function() {
        it('Should apply subscript to the active editor', function() {
            const formatApplied = formatting.textFormattingSubScript();
            formatApplied.should.equal(true);
        });
    });
});
