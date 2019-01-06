/**
 * Test cases to make sure text direction change behaves as expected
 */
chai.should();
describe('#Directionality', function() {
    describe('givenDirectionality_whenChangedToLTR_thenShouldAddDirectionalityToAnode', function() {
        it('Node directionality changed to LTR', function() {
            const callback = UmContentEditorCore.textDirectionLeftToRight();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('givenDirectionality_whenChangedToRTL_thenShouldAddDirectionalityToAnode', function() {
        it('Node directionality changed to RTL', function() {
            const callback = UmContentEditorCore.textDirectionRightToLeft();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
