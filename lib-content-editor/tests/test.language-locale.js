/**
 * Tes case to make sure language locale behaves as expected
 */

chai.should();
describe('#Language locale', function() {
    describe('givenActiveEditor_whenDefaultLanguageLocaleIsSet_thenAllPlaceholdersShouldBeSetInThatLanguage', function() {
        UmEditorCore.onInit("en", "ltr", false, true);
        UmEditorCore.enableEditingMode();
        setTimeout(() => {
            it('English placeholders was set', function() {
                "Yes".should.equal(UmWidgetManager._locale.placeholders.labelForTrueOptionText);
            });
        }, TEST_CASE_TIMEOUT);
    
    });
    

});