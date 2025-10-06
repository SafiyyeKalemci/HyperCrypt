const module = document.querySelector('.cyber-security-module');
const video = module.querySelector('.hover-video');

module.addEventListener('mouseenter', () => {
    video.style.display = 'block';
    module.classList.add('hovering');
    video.play();
});

module.addEventListener('mouseleave', () => {
    video.pause();
    video.currentTime = 0;
    video.style.display = 'none';
    module.classList.remove('hovering');
});