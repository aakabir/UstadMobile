/**
 * Test cases to make sure text direction change behaves as expected
 */
chai.should();
describe('Text Direction', function() {
    describe('#LeftToRight', function() {
        it('Text direction changed from LTR', function() {
            const callback = umContentEditor.textDirectionLeftToRight();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#RightToLeft', function() {
        it('Text direction changed from RTL', function() {
            const callback = umContentEditor.textDirectionRightToLeft();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
