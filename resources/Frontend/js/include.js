document.addEventListener("DOMContentLoaded", () => {
  const basePath = "../"; // Anasayfa -> Frontend

  fetch(basePath + "header/header.html")
    .then(res => res.text())
    .then(data => {
      document.getElementById("header").innerHTML = data;
      const toggleBtn = document.querySelector('.menu-toggle');
      const nav = document.querySelector('#primary-navigation');
      if (toggleBtn && nav) {
        toggleBtn.addEventListener('click', () => {
          const isActive = nav.classList.toggle('active');
          toggleBtn.setAttribute('aria-expanded', isActive ? 'true' : 'false');
        });
        // Menü açıkken linke tıklanınca kapat
        nav.querySelectorAll('a').forEach(link => {
          link.addEventListener('click', () => {
            if (window.matchMedia('(max-width: 768px)').matches) {
              nav.classList.remove('active');
              toggleBtn.setAttribute('aria-expanded', 'false');
            }
          });
        });
      }
    })
    .catch(err => console.error("Header yüklenemedi:", err));

  fetch(basePath + "footer/footer.html")
    .then(res => res.text())
    .then(data => document.getElementById("footer").innerHTML = data)
    .catch(err => console.error("Footer yüklenemedi:", err));
});