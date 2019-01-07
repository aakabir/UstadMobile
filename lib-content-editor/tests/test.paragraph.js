/**
 * Test cases to make sure paragraph ustadEditor behaves as expected
 */
chai.should();
describe('#Paragraph Formatting', function() {

    describe('givenActiveEditor_whenParagraphIsLeftJustified_thenShouldJustify', function() {
        it('Left justification applied', function() {
            const callback = UmContentEditorCore.paragraphLeftJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenActiveEditor_whenParagraphIsRightJustified_thenShouldJustify', function() {
        it('Right justification applied', function() {
            const callback = UmContentEditorCore.paragraphRightJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenActiveEditor_whenParagraphIsFullyFully_thenShouldJustify', function() {
        it('Full justification applied', function() {
            const callback = UmContentEditorCore.paragraphFullJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenActiveEditor_whenParagraphIsCenterJustified_thenShouldJustify', function() {
        it('Center justification', function() {
            const callback = UmContentEditorCore.paragraphCenterJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenActiveEditor_whenParagraphIsIndented_thenIndentShouldBeApplied', function() {
        it('Indent applied', function() {
            const callback = UmContentEditorCore.paragraphIndent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });


    describe('givenActiveEditor_whenParagraphIsOutdented_thenOutdentShouldBeApplied', function() {
        it('Outdent applied', function() {
            const callback = UmContentEditorCore.paragraphOutDent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenActiveEditor_whenOrderedListIsInserted_thenItemsShouldBeNumbered', function() {
        it('Numbers applied to a list item', function() {
            const callback = UmContentEditorCore.paragraphOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenActiveEditor_whenUnOrderedListIsInserted_thenItemsShouldBulletized', function() {
        it('Bullets applied to list items', function() {
            const callback = UmContentEditorCore.paragraphUnOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
