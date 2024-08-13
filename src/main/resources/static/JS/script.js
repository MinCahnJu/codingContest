document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM fully loaded and parsed');

    function renderMathInElement(element) {
        if (typeof MathJax !== 'undefined' && MathJax.typesetPromise) {
            MathJax.typesetPromise([element]).then(() => {
                console.log('MathJax: Typesetting complete for element.');
            }).catch((err) => console.log('MathJax: Typesetting failed for element:', err));
        } else {
            console.log('MathJax is not defined or typesetPromise is not available');
        }
    }

    function renderAllMath() {
        const elementsToRender = document.querySelectorAll('.problem, .example');
        elementsToRender.forEach(renderMathInElement);
    }

    // Observer to detect changes in the elements
    const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.type === 'childList') {
                console.log('ChildList mutation detected');
                renderAllMath();
            }
        });
    });

    const elementsToObserve = document.querySelectorAll('.problem, .example');
    elementsToObserve.forEach(element => {
        if (element) {
            observer.observe(element, { childList: true });
            console.log(`Observing: ${element.id}`);
        }
    });

    // Check if MathJax is ready
    if (typeof MathJax !== 'undefined' && MathJax.startup) {
        MathJax.startup.promise.then(() => {
            renderAllMath();
        }).catch((err) => console.log('MathJax: Initialization failed:', err));
    } else {
        console.log('MathJax startup not found, waiting for MathJax to load...');
        // Polling to check if MathJax is ready
        const interval = setInterval(() => {
            if (typeof MathJax !== 'undefined' && MathJax.startup) {
                clearInterval(interval);
                MathJax.startup.promise.then(() => {
                    renderAllMath();
                }).catch((err) => console.log('MathJax: Initialization failed:', err));
            }
        }, 100);
    }

});