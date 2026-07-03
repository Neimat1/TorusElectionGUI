const docsMenuToggle = document.querySelector('.docs-menu-toggle');
const docsNav = document.querySelector('#docs-nav');
const docsBackdrop = document.querySelector('.docs-menu-backdrop');

function setDocsMenuOpen(open) {
    document.body.classList.toggle('docs-menu-open', open);
    if (docsMenuToggle) {
        docsMenuToggle.setAttribute('aria-expanded', String(open));
        docsMenuToggle.setAttribute('aria-label', open ? 'Close menu' : 'Open menu');
    }
}

if (docsMenuToggle) {
    docsMenuToggle.addEventListener('click', () => setDocsMenuOpen(!document.body.classList.contains('docs-menu-open')));
}
if (docsBackdrop) {
    docsBackdrop.addEventListener('click', () => setDocsMenuOpen(false));
}
if (docsNav) {
    docsNav.addEventListener('click', event => {
        if (event.target.closest('a')) setDocsMenuOpen(false);
    });
}
window.addEventListener('keydown', event => {
    if (event.key === 'Escape') setDocsMenuOpen(false);
});
