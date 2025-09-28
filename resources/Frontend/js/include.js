document.addEventListener("DOMContentLoaded", () => {
  const basePath = "../"; // Anasayfa -> Frontend

  fetch(basePath + "header/header.html")
    .then(res => res.text())
    .then(data => document.getElementById("header").innerHTML = data)
    .catch(err => console.error("Header yüklenemedi:", err));

  fetch(basePath + "footer/footer.html")
    .then(res => res.text())
    .then(data => document.getElementById("footer").innerHTML = data)
    .catch(err => console.error("Footer yüklenemedi:", err));
});