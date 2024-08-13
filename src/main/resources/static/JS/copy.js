document.addEventListener('DOMContentLoaded', function() {
    // Example adding functionality
    let exampleCount = 1;

    document.getElementById('add-example').addEventListener('click', () => {
        exampleCount++;

        const examplesDiv = document.getElementById('examples');

        // 새로운 입력 예제 필드 생성
        const newExampleInputDiv = document.createElement('span');
        newExampleInputDiv.classList.add('problem');
        newExampleInputDiv.style.width = '360px';
        newExampleInputDiv.style.border = 'none';

        const inputTextDiv = document.createElement('div');

        const inputTextarea = document.createElement('textarea');
        inputTextarea.classList.add('longtext');
        inputTextarea.style.width = '335px';
        inputTextarea.name = 'exampleinput[]';
        inputTextarea.rows = 3;
        inputTextarea.cols = 25;
        inputTextarea.placeholder = 'Enter your contest description here...';
        inputTextarea.oninput = function() { autoResize(this) };

        inputTextDiv.appendChild(inputTextarea)
        newExampleInputDiv.appendChild(inputTextDiv);


        // 새로운 출력 예제 필드 생성
        const newExampleOutputDiv = document.createElement('span');
        newExampleOutputDiv.classList.add('problem');
        newExampleOutputDiv.style.width = '360px';
        newExampleOutputDiv.style.border = 'none';

        const outputTextDiv = document.createElement('div');

        const outputTextarea = document.createElement('textarea');
        outputTextarea.classList.add('longtext');
        outputTextarea.style.width = '335px';
        outputTextarea.name = 'exampleoutput[]';
        outputTextarea.rows = 3;
        outputTextarea.cols = 25;
        outputTextarea.placeholder = 'Enter your contest description here...';
        outputTextarea.oninput = function() { autoResize(this) };

        outputTextDiv.appendChild(outputTextarea)
        newExampleOutputDiv.appendChild(outputTextDiv);


        // 새로운 예제 필드들을 examplesDiv에 추가
        examplesDiv.appendChild(newExampleInputDiv);
        examplesDiv.appendChild(newExampleOutputDiv);

        console.log('New example added');
    });
});

function autoResize(textarea) {
    if (textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = textarea.scrollHeight + 'px';
    } else {
        console.error('Textarea element is null');
    }
}