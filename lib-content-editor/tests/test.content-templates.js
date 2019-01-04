/**
 * Tes cases to make sure ustadmobile blankDocument templates works as expected
 * 1. Insert Multiple choice questions
 * 2. Insert in fill the blanks questions
 * 3. Insert multimedia blankDocument.
 */
const TEST_CASE_TIMEOUT = 1000;
chai.should();
describe('Ustadmobile Content', function() {

    describe("Content type questions",function () {

        describe('#multiple questions', function() {
            it('Should insert multi-choice question template when multi-choice menu clicked', function() {
                UmContentEditorCore.insertMultipleChoiceQuestionTemplate();
                setTimeout(() => {
                    const questionList = $('body').find('div[data-um-widget="multi-choice"]');
                    questionList.length.should.equal(1);
                },TEST_CASE_TIMEOUT);
            });
        });

        describe('#Fill in the blanks questions', function() {
            it('Should insert fill in the blanks question template when fill in the blanks menu clicked', function() {
                UmContentEditorCore.insertFillInTheBlanksQuestionTemplate();
                setTimeout(() =>{
                    const questionList = $('body').find('div[data-um-widget="fill-the-blanks"]');
                    questionList.length.should.equal(1);
                },TEST_CASE_TIMEOUT)
            });
        });
    });

    describe("Content type multimedia",function () {

        describe('#Multimedia - Image', function() {
            it('Should insert image to the editor when image selected as file', function() {
                let bannySource = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg";
                UmContentEditorCore.insertMediaContent(bannySource,"image/jpg");
                setTimeout(() => {
                    const content = $('body').find('img');
                    const imageSrc = $(content).attr('src');
                    const isSourceTheSame = imageSrc === bannySource;
                    isSourceTheSame.should.equal('true');
                },TEST_CASE_TIMEOUT);
            });
        });

        describe('#Multimedia - Audio', function() {
            it('Should insert audio to the editor when audio selected as file', function() {
                let bannySource = "http://www.noiseaddicts.com/samples_1w72b820/280.mp3";
                UmContentEditorCore.insertMediaContent(bannySource,"audio/mp3");
                setTimeout(() => {
                    const content = $('body').find('video.media-audio');
                    const audioUrl = $(content).find('Source:first').attr('src');
                    const isSourceTheSame = audioUrl === bannySource;
                    isSourceTheSame.should.equal('true');
                },TEST_CASE_TIMEOUT);
            });
        });

        describe('#Multimedia - Video', function() {
            it('Should insert video to the editor when video selected as file', function() {

                let bannySource = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                UmContentEditorCore.insertMediaContent(bannySource,"video/mp4");
                setTimeout(() => {
                    const content = $('body').find('video.um-media');
                    const videoUrl = $(content).find('Source:first').attr('src');
                    const isSourceTheSame = videoUrl === bannySource;
                    isSourceTheSame.should.equal('true');
                },TEST_CASE_TIMEOUT);
            });
        });
    });


});