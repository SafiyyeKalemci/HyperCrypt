const finiteBtn = document.getElementById("finiteBtn");
const infiniteBtn = document.getElementById("infiniteBtn");

finiteBtn.addEventListener("click", function () {
  if (this.id === "finiteBtn") {
    document.getElementById("finite-set").style.display = "block";
    document.getElementById("infinite-set").style.display = "none";
    finiteBtn.classList.add("active");
    infiniteBtn.classList.remove("active");
  }
});

infiniteBtn.addEventListener("click", function () {
  if (this.id === "infiniteBtn") {
    document.getElementById("finite-set").style.display = "none";
    document.getElementById("infinite-set").style.display = "block";
    infiniteBtn.classList.add("active");
    finiteBtn.classList.remove("active");
  }
});

document.getElementById("finite-set").style.display = "block";
document.getElementById("infinite-set").style.display = "none";
finiteBtn.classList.add("active");

function showResults() {
  // Diyelim ki kullanıcı şu elemanları girdi
  const elements = ["a", "b", "c"];  

  // Tablo başlığı
  let tableHTML = `
    <h3>Sonuçlar</h3>
    <table>
      <thead>
        <tr>
          <th>*</th>
          ${elements.map(el => `<th>${el}</th>`).join("")}
        </tr>
      </thead>
      <tbody>
  `;

  // Satırlar
  elements.forEach(rowEl => {
    tableHTML += `<tr><th>${rowEl}</th>`;
    elements.forEach(colEl => {
      tableHTML += `<td>{${rowEl}*${colEl}}</td>`; // burada backend’den gelen gerçek sonuç yazılacak
    });
    tableHTML += `</tr>`;
  });

  tableHTML += `</tbody></table>`;

  document.getElementById("result").innerHTML = tableHTML;

  // Burada backend çağrısı yapmamız lazım, örnek için dummy veri kullanıyorum
  const backendResponse = {
    highestStructure: "Hypergroup",
    tests: {
      semihypergroup: true,
      hypergroupoid: true,
      quasihypergroup: false,
      hypergroup: true
    },
    isHypergroup: true // bu 6. adım için lazım
  };

  let testsHTML = `
    <h4>Yapı Testleri</h4>
    <p><b>Highest Structure:</b> ${backendResponse.highestStructure}</p>
    <ul>
      <li>Semihypergroup: ${backendResponse.tests.semihypergroup}</li>
      <li>Hypergroupoid: ${backendResponse.tests.hypergroupoid}</li>
      <li>Quasihypergroup: ${backendResponse.tests.quasihypergroup}</li>
      <li>Hypergroup: ${backendResponse.tests.hypergroup}</li>
    </ul>
  `;

  document.getElementById("structure-tests").innerHTML = testsHTML;

  document
    .getElementById("results-section")
    .scrollIntoView({ behavior: "smooth", block: "start" });

  // 6. kısım koşullu render burada olacak
  if (!backendResponse.isHypergroup) {
    showAIAssistant();
  }
}

function showAIAssistant() {
  // Backend’den gelen yapay zeka mesajını alacağız, örnek:
  const aiMessage = "Bu küme hypergroup değil çünkü kapalı değil. Ancak semihypergroup özellikleri gösteriyor.";
  
  document.getElementById("ai-message").innerText = aiMessage;
  document.getElementById("ai-assistant").style.display = "block";
}