/**
 * Test cases to make sure paragraph ustadEditor behaves as expected
 */
chai.should();
describe('Paragraph Formatting', function() {

    describe('#JustifyLeft', function() {
        it('Should apply left justification to the blankDocument', function() {
            const callback = umContentEditor.paragraphLeftJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyRight', function() {
        it('Should apply right justification to the blankDocument', function() {
            const callback = umContentEditor.paragraphRightJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyFull', function() {
        it('Should apply full justification to the blankDocument', function() {
            const callback = umContentEditor.paragraphFullJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#JustifyCenter', function() {
        it('Should apply center justification to the blankDocument', function() {
            const callback = umContentEditor.paragraphCenterJustification();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Indent', function() {
        it('Should apply blankDocument Indent', function() {
            const callback = umContentEditor.paragraphIndent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Outdent', function() {
        it('Should apply blankDocument Outdent', function() {
            const callback = umContentEditor.paragraphOutDent();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#OrderedList', function() {
        it('Should add ordered list to the blankDocument', function() {
            const callback = umContentEditor.paragraphOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#UnOrderedList', function() {
        it('Should add unordered list to the blankDocument', function() {
            const callback = umContentEditor.paragraphUnOrderedListFormatting();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
