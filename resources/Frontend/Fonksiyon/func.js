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
          ${elements.map((el) => `<th>${el}</th>`).join("")}
        </tr>
      </thead>
      <tbody>
  `;

  // Satırlar
  elements.forEach((rowEl) => {
    tableHTML += `<tr><th>${rowEl}</th>`;
    elements.forEach((colEl) => {
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
      hypergroup: true,
    },
    isHypergroup: true, // bu 6. adım için lazım
  };

  let testsHTML = `
    <p><b>Highest Structure:</b> ${backendResponse.highestStructure}</p>
    <ul>
      <li>
      <span>Semihypergroup</span>
      <span class="pill ${
        backendResponse.tests.semihypergroup ? "true" : "false"
      }">
        ${backendResponse.tests.semihypergroup}
      </span>
    </li>
    <li>
      <span>Hypergroupoid</span>
      <span class="pill ${
        backendResponse.tests.hypergroupoid ? "true" : "false"
      }">
        ${backendResponse.tests.hypergroupoid}
      </span>
    </li>
    <li>
      <span>Quasihypergroup</span>
      <span class="pill ${
        backendResponse.tests.quasihypergroup ? "true" : "false"
      }">
        ${backendResponse.tests.quasihypergroup}
      </span>
    </li>
    <li>
      <span>Hypergroup</span>
      <span class="pill ${backendResponse.tests.hypergroup ? "true" : "false"}">
        ${backendResponse.tests.hypergroup}
      </span>
    </li>
    </ul>
  `;

  document.getElementById("structure-tests").innerHTML = testsHTML;

  const resultsSection = document.getElementById("results-section");
  resultsSection.style.display = "block"; // görünür yap
  resultsSection.scrollIntoView({ behavior: "smooth", block: "start" });

  // 6. kısım koşullu render burada olacak
  if (!backendResponse.isHypergroup) {
    showAIAssistant();
  }
}

function showAIAssistant() {
  // Backend’den gelen yapay zeka mesajını alacağız, örnek:
  const aiMessage =
    "Bu küme hypergroup değil çünkü kapalı değil. Ancak semihypergroup özellikleri gösteriyor.";

  document.getElementById("ai-message").innerText = aiMessage;
  document.getElementById("ai-assistant").style.display = "block";
}

// Tab geçişleri (Bu kısmı yukarı koyabilirsin, sonra düzenle)
document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    document
      .querySelectorAll(".tab-btn")
      .forEach((b) => b.classList.remove("active"));
    document
      .querySelectorAll(".tab-content")
      .forEach((tc) => tc.classList.remove("active"));

    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.add("active");
  });
});

//Hiperhalka Örnekleri
const examples = {
  finite1: {
    set: ["1", "2", "3"],
    rules: "a*b=a; a*c=b",
    results: {
      "1*1": "1",
      "1*2": "1",
      "1*3": "2",
      "2*1": "1",
      "2*2": "2",
      "2*3": "3",
      "3*1": "2",
      "3*2": "3",
      "3*3": "1",
    },
  },

  finite2: {
    set: ["a", "b"],
    rules: "a*b=b; b*b=a",
    results: {
      "a*a": "a",
      "a*b": "b",
      "b*a": "b",
      "b*b": "a",
    },
  },

  finite3: {
    set: ["x", "y", "z"],
    rules: "x*y=z; y*z=x",
    results: {
      "x*x": "x",
      "x*y": "z",
      "x*z": "y",
      "y*x": "z",
      "y*y": "y",
      "y*z": "x",
      "z*x": "y",
      "z*y": "x",
      "z*z": "z",
    },
  },

  infinite1: {
    set: "ℤ (Tam sayılar)",
    rules: "a+b ∈ ℤ",
    examples: [
      { a: 2, b: 3, result: 5 },
      { a: -1, b: 4, result: 3 },
    ],
  },

  infinite2: {
    set: "ℚ (Rasyonel sayılar)",
    rules: "a*b ∈ ℚ",
    examples: [
      { a: "1/2", b: "2/3", result: "1/3" },
      { a: "1/3", b: "3", result: "1" },
    ],
  },
};

// Tek fonksiyon: finite mi infinite mi ayırıyor
function renderExample(example) {
  let html = "";

  if (Array.isArray(example.set)) {
    // Finite küme (Cayley tablosu)
    html += `<p><b>Küme:</b> { ${example.set.join(", ")} }</p>`;
    html += `<p><b>Kurallar:</b> ${example.rules}</p>`;
    html += `<table>
      <thead>
        <tr><th>*</th>${example.set.map((el) => `<th>${el}</th>`).join("")}</tr>
      </thead>
      <tbody>`;

    example.set.forEach((rowEl) => {
      html += `<tr><th>${rowEl}</th>`;
      example.set.forEach((colEl) => {
        const key = `${rowEl}*${colEl}`;
        html += `<td>${example.results[key] || "-"}</td>`;
      });
      html += `</tr>`;
    });

    html += `</tbody></table>`;
  } else {
    // Infinite küme (örnek işlemler)
    html += `<p><b>Küme:</b> ${example.set}</p>`;
    html += `<p><b>Kurallar:</b> ${example.rules}</p>`;
    html += `<table>
      <thead><tr><th>a</th><th>b</th><th>Sonuç</th></tr></thead>
      <tbody>`;

    example.examples.forEach((ex) => {
      html += `<tr><td>${ex.a}</td><td>${ex.b}</td><td>${ex.result}</td></tr>`;
    });

    html += `</tbody></table>`;
  }

  html += `</div>`;
  return html;
}

// Başlangıçta tüm tab-content'leri doldur
Object.keys(examples).forEach((key) => {
  document.getElementById(key).innerHTML = renderExample(examples[key]);
});

// Fonksiyon Kullanımı butonu için scroll fonksiyonu
document.getElementById("usageGuideBtn").addEventListener("click", function() {
  const usageGuide = document.getElementById("usage-guide");
  const yOffset = -80; // üstten boşluk
  const y = usageGuide.getBoundingClientRect().top + window.pageYOffset + yOffset;

  window.scrollTo({ top: y, behavior: "smooth" });
  
});