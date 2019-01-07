/**
 * Test cases to make sure focus change behaves as expected
 */
chai.should();
describe('#Fucus change events', function() {
    describe('givenActiveEditor_whenClickOrKeyDownEventTriggeredOnProtectedElement_thenCursorPositionShouldChangeToNextUnprotectedFocusableAElement', function() {
        it('Cursor moved to next unprotected focusable element.', function() {

            const previouslyFocusedElement = tinymce.activeEditor.selection.getNode();

            //element where click / keydown event will be triggered on (obtained from event.target)
            const currentElement = $(document).find(".um-labels").get(0);

            const expectedElementToBeFocused = $(document).find("p").get(1);

            const currentlyInFocusElement =  UmContentEditorCore.setFocusToNextUnprotectedFocusableElement(currentElement);
            const isFocusOnRightElement = expectedElementToBeFocused !== previouslyFocusedElement
                && currentlyInFocusElement === expectedElementToBeFocused;

            isFocusOnRightElement.should.equal(true);
        });
    });

});
