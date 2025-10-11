// Cyber Security Module
const cyberModule = document.querySelector(".cyber-security-module");
const cyberVideo = cyberModule.querySelector(".hover-video");

cyberModule.addEventListener("mouseenter", () => {
  cyberVideo.style.display = "block";
  cyberModule.classList.add("hovering");
  cyberVideo.play();
});

cyberModule.addEventListener("mouseleave", () => {
  cyberVideo.pause();
  cyberVideo.currentTime = 0;
  cyberVideo.style.display = "none";
  cyberModule.classList.remove("hovering");
});

// Encryption Module
const encryptionModule = document.querySelector(".encryption-module");
const encryptionVideo = encryptionModule.querySelector(".hover-video");

encryptionModule.addEventListener("mouseenter", () => {
  encryptionVideo.style.display = "block";
  encryptionModule.classList.add("hovering");
  encryptionVideo.play();
});

encryptionModule.addEventListener("mouseleave", () => {
  encryptionVideo.pause();
  encryptionVideo.currentTime = 0;
  encryptionVideo.style.display = "none";
  encryptionModule.classList.remove("hovering");
});
