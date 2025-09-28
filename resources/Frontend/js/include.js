// include.js
document.addEventListener("DOMContentLoaded", () => {
  // Mevcut sayfanın URL'sini al
  const currentPath = window.location.pathname; // Örn: /resources/Frontend/Anasayfa/anasayfa.html

  // Base path: header/footer klasörünün kökü
  const basePath = "/resources/Frontend/";

  // Header yükle
  fetch(basePath + "header/header.html")
    .then(res => res.text())
    .then(data => document.getElementById("header").innerHTML = data)
    .catch(err => console.error("Header yüklenemedi:", err));

  // Footer yükle
  fetch(basePath + "footer/footer.html")
    .then(res => res.text())
    .then(data => document.getElementById("footer").innerHTML = data)
    .catch(err => console.error("Footer yüklenemedi:", err));
});
