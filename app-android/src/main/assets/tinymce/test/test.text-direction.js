/**
 * Test cases to make sure text direction change behaves as expected
 */
chai.should();
describe('Text Direction', function() {
    describe('#LeftToRight', function() {
        it('Text direction changed from LTR', function() {
            const applied = formatting.textDirectionLeftToRight();
            applied.should.equal(true);
        });
    });

    describe('#RightToLeft', function() {
        it('Text direction changed from RTL', function() {
            const applied = formatting.textDirectionRightToLeft();
            applied.should.equal(true);
        });
    });
});
