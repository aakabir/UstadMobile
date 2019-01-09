/**
 * Tes cases to make sure ustadmobile blankDocument templates works as expected
 * 1. Insert Multiple choice questions
 * 2. Insert in fill the blanks questions
 * 3. Insert multimedia blankDocument.
 */
chai.should();
describe('#Protected content', function() {

    describe('givenActiveEditor_whenProtectedElementIsSelectedAndDeleteKeyIsPressed_thenShouldNotBeDeleted', function() {
        it('Content deletion not allowed', function() {
            const content = "<label class='um-labels'>Sample label</label>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(content,content.length,true,false,{});
            isKeyAllowed.should.equal(false);
        });
    });

    describe('givenActiveEditor_whenProtectedElementIsSelectedAndAnyKeyIsPressed_thenShouldNotBeDeleted', function() {
        it('Content deletion not allowed', function() {
            const content = "<label class='um-labels'>Sample label</label>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(content,content.length,true,false,{});
            isKeyAllowed.should.equal(false);
        });
    });

    describe('givenActiveEditor_whenNotProtectedElementIsSelectedAndDeleteKeyIsPressed_thenShouldBeDeleted', function() {
        it('Content deletion allowed', function() {
            const content = "<p>Sample label</p>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(content,content.length,true,false,{});
            isKeyAllowed.should.equal(true);
        });
    });

    describe('givenActiveEditor_whenNoElementContentIsSelectedAndDeleteKeyIsPressed_thenShouldBeDeleted', function() {
        it('Content deletion allowed', function() {
            const content = "<p>Sample label</p>";
            const isKeyAllowed = UmContentEditorCore.checkProtectedElements(content,0,false,false,{});
            isKeyAllowed.should.equal(true);
        });
    });
});