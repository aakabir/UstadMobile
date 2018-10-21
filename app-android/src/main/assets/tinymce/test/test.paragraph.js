/**
 * Test cases to make sure paragraph formatting behaves as expected
 */
chai.should();
describe('Paragraph Formatting', function() {

    describe('#JustifyLeft', function() {
        it('Should apply left justification to the content', function() {
            const formatApplied = formatting.paragraphLeftJustification();
            formatApplied.should.equal(true);
        });
    });

    describe('#JustifyRight', function() {
        it('Should apply right justification to the content', function() {
            const formatApplied = formatting.paragraphRightJustification();
            formatApplied.should.equal(true);
        });
    });

    describe('#JustifyFull', function() {
        it('Should apply full justification to the content', function() {
            const formatApplied = formatting.paragraphFullJustification();
            formatApplied.should.equal(true);
        });
    });

    describe('#JustifyCenter', function() {
        it('Should apply center justification to the content', function() {
            const formatApplied = formatting.paragraphCenterJustification();
            formatApplied.should.equal(true);
        });
    });

    describe('#Indent', function() {
        it('Should apply content Indent', function() {
            const formatApplied = formatting.paragraphIndent();
            formatApplied.should.equal(true);
        });
    });

    describe('#Outdent', function() {
        it('Should apply content Outdent', function() {
            const formatApplied = formatting.paragraphOutDent();
            formatApplied.should.equal(true);
        });
    });

    describe('#OrderedList', function() {
        it('Should add ordered list to the content', function() {
            const formatApplied = formatting.paragraphOrderedListFormatting();
            formatApplied.should.equal(true);
        });
    });

    describe('#UnOrderedList', function() {
        it('Should add unordered list to the content', function() {
            const formatApplied = formatting.paragraphUnOrderedListFormatting();
            formatApplied.should.equal(true);
        });
    });
});

