/**
 * Test cases to make sure paragraph ustadEditor behaves as expected
 */
chai.should();
describe('Paragraph Formatting', function() {

    describe('#JustifyLeft', function() {
        it('Should apply left justification to the blankDocument', function() {
            const callback = UmContentEditorCore.paragraphLeftJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyRight', function() {
        it('Should apply right justification to the blankDocument', function() {
            const callback = UmContentEditorCore.paragraphRightJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyFull', function() {
        it('Should apply full justification to the blankDocument', function() {
            const callback = UmContentEditorCore.paragraphFullJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyCenter', function() {
        it('Should apply center justification to the blankDocument', function() {
            const callback = UmContentEditorCore.paragraphCenterJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Indent', function() {
        it('Should apply blankDocument Indent', function() {
            const callback = UmContentEditorCore.paragraphIndent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Outdent', function() {
        it('Should apply blankDocument Outdent', function() {
            const callback = UmContentEditorCore.paragraphOutDent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#OrderedList', function() {
        it('Should add ordered list to the blankDocument', function() {
            const callback = UmContentEditorCore.paragraphOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#UnOrderedList', function() {
        it('Should add unordered list to the blankDocument', function() {
            const callback = UmContentEditorCore.paragraphUnOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
