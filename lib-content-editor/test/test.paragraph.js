/**
 * Test cases to make sure paragraph ustadEditor behaves as expected
 */
chai.should();
describe('Paragraph Formatting', function() {

    describe('#JustifyLeft', function() {
        it('Should apply left justification to the blankDocument', function() {
            const callback = ustadEditor.paragraphLeftJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyRight', function() {
        it('Should apply right justification to the blankDocument', function() {
            const callback = ustadEditor.paragraphRightJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyFull', function() {
        it('Should apply full justification to the blankDocument', function() {
            const callback = ustadEditor.paragraphFullJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyCenter', function() {
        it('Should apply center justification to the blankDocument', function() {
            const callback = ustadEditor.paragraphCenterJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Indent', function() {
        it('Should apply blankDocument Indent', function() {
            const callback = ustadEditor.paragraphIndent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Outdent', function() {
        it('Should apply blankDocument Outdent', function() {
            const callback = ustadEditor.paragraphOutDent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#OrderedList', function() {
        it('Should add ordered list to the blankDocument', function() {
            const callback = ustadEditor.paragraphOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#UnOrderedList', function() {
        it('Should add unordered list to the blankDocument', function() {
            const callback = ustadEditor.paragraphUnOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
