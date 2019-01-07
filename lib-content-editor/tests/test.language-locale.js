/**
 * Tes cases to make sure ustadmobile blankDocument templates works as expected
 * 1. Insert Multiple choice questions
 * 2. Insert in fill the blanks questions
 * 3. Insert multimedia blankDocument.
 */

chai.should();
describe('#Language locale', function() {

    describe('givenActiveEditor_whenDefaultLanguageLocaleIsSet_thenAllPlaceholdersShouldBeSetInThatLanguage', function() {
        UmContentEditorCore.initEditor({locale:'swa',test:true});
        it('Swahili placeholders was set', function() {
            "Ndio".should.equal(UmQuestionWidget.PLACEHOLDERS_LABELS.labelForTrueOptionText);
        });
    });

});