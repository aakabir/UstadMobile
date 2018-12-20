/**
 * Test cases to make sure text ustadEditor behaves as expected
 */
chai.should();
describe('Text Formatting', function() {
    describe('#Bold', function() {
        it('Should apply boldness to the active editor', function() {
            const callback = umContentEditor.textFormattingBold();
          atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Italic', function() {
        it('Should apply italic to the active editor', function() {
            const callback = umContentEditor.textFormattingItalic();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Underline', function() {
        it('Should apply underline to the active editor', function() {
            const callback = umContentEditor.textFormattingUnderline();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#StrikeThrough', function() {
        it('Should apply strike-through to the active editor', function() {
            const callback = umContentEditor.textFormattingStrikeThrough();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Superscript', function() {
        it('Should apply superscript to the active editor', function() {
            const callback = umContentEditor.textFormattingSuperScript();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Subscript', function() {
        it('Should apply subscript to the active editor', function() {
            const callback = umContentEditor.textFormattingSubScript();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
