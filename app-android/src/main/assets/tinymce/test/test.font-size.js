/**
 * Test cases to make sure font-size changing task behaves as expected
 */
chai.should();
describe('Font Size', function() {
    describe('#Font Size', function() {
        it('Text font size changed', function() {
            const fontSize = "40pt";
            const currentFontSize = formatting.setFontSize(fontSize);
            currentFontSize.should.equal(fontSize);
        });
    });

});
