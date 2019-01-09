/**
 * Tes case to make sure language locale behaves as expected
 */

chai.should();
describe('#Language locale', function() {
    beforeEach(function(){
        this.timeout(2000);
    });
    describe('givenActiveEditor_whenDefaultLanguageLocaleIsSet_thenAllPlaceholdersShouldBeSetInThatLanguage', function() {
        UmContentEditorCore.initEditor({locale:'swa',test:true});
        it('Swahili placeholders was set', function() {
            "Ndio".should.equal(UmQuestionWidget.getPlaceholders().labelForTrueOptionText);
        });
    });

});