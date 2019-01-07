/**
 * Test cases to make sure text ustadEditor behaves as expected
 */
chai.should();
describe('#Text Formatting', function() {
    describe('givenTextNode_whenBoldFormatIsActivated_thenShouldApplyBoldnessToSelectedText', function() {
        it('Boldness applied to selected text', function() {
            const callback = UmContentEditorCore.textFormattingBold();
          atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenTextNode_whenItalicFormatIsActivated_thenShouldItalicizeSelectedText', function() {
        it('Italicize selected text', function() {
            const callback = UmContentEditorCore.textFormattingItalic();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenTextNode_whenUnderlineFormatIsActivated_thenShouldUnderlineSelectedText', function() {
        it('Underlined selected text', function() {
            const callback = UmContentEditorCore.textFormattingUnderline();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenTextNode_whenStrikeThroughFormatIsActivated_thenShouldBeAppliedOnSelectedText', function() {
        it('Strike-through selected text', function() {
            const callback = UmContentEditorCore.textFormattingStrikeThrough();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenTextNode_whenSuperScriptFormatIsActivated_thenShouldMakeSelectedTextSuperScript', function() {
        it('Selected text made Superscript', function() {
            const callback = UmContentEditorCore.textFormattingSuperScript();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenTextNode_whenSubScriptFormatIsActivated_thenShouldMakeSelectedTextSubScript', function() {
        it('Selected text made subscript', function() {
            const callback = UmContentEditorCore.textFormattingSubScript();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
