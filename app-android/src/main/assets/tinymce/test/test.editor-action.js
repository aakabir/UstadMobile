/**
 * Test cases to make sure editor actions (Undo & Redo) behaves as expected
 */
chai.should();
describe('Editor Actions', function() {

    describe('#Undo', function() {
        it('Should apply ordered list formatting to the active editor', function() {
            tinymce.activeEditor.execCommand('mceInsertContent', false, "<span>Undo Redo</span>",{format: 'raw'});
            const formatApplied = formatting.editorActionUndo();
            formatApplied.should.equal(true);
        });
    });

    describe('#Redo', function() {
        it('Should apply unordered list formatting to the active editor', function() {
            const formatApplied = formatting.editorActionRedo();
            formatApplied.should.equal(true);
        });
    });
});


