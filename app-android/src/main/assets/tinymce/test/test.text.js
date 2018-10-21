/**
 * Test cases to make sure text formatting behaves as expected
 */
chai.should();
describe('Text Formatting', function() {
    describe('#Bold', function() {
        it('Should apply boldness to the active editor', function() {
            const formatApplied = formatting.textFormattingBold();
            formatApplied.should.equal(true);
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
