/**
 * Test cases to make sure editor actions (Undo & Redo) behaves as expected
 */
chai.should();
describe('Editor Actions', function() {

    describe('#Undo', function() {
        it('Should apply ordered list ustadEditor to the active editor', function() {
            tinymce.activeEditor.execCommand('mceInsertContent', false, "<span>Undo Redo</span>",{format: 'raw'});
            const callback = umContentEditor.editorActionUndo();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });

    describe('#Redo', function() {
        it('Should apply unordered list ustadEditor to the active editor', function() {
            const callback = umContentEditor.editorActionRedo();
            atob(callback.content).split("-")[formatStatusIndex].should.equal('true');
        });
    });
});
