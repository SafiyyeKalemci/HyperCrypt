document.addEventListener('DOMContentLoaded', () => {
  const modeSwitcher = document.getElementById('modeSwitcher');
  const customMapMode = document.getElementById('customMapMode');
  const binaryMode = document.getElementById('binaryMode');

  const customMessageInput = document.getElementById('customMessageInput');
  const qKeyboard = document.getElementById('qKeyboard');
  const customRuleInput = document.getElementById('customRuleInput');
  const customEncryptedPreview = document.getElementById('customEncryptedPreview');
  const sendCustomButton = document.getElementById('sendCustomButton');
  const customCommunicationLog = document.getElementById('customCommunicationLog');

  const binaryMessageInput = document.getElementById('binaryMessageInput');
  const binaryRuleInput = document.getElementById('binaryRuleInput');
  const binaryPreviewOutput = document.getElementById('binaryPreviewOutput');
  const sendBinaryButton = document.getElementById('sendBinaryButton');
  const binaryCommunicationLog = document.getElementById('binaryCommunicationLog');

  // Klavye tuşları (QWERTY düzeni)
  const qwertyLayout = [
    'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P',
    'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L',
    'Z', 'X', 'C', 'V', 'B', 'N', 'M',
    ' ' // Boşluk tuşu
  ];

  // Anahtar haritalamalarını tutacak obje
  const keyMappings = {}; // { 'Q': 12, 'W': 5, ... }

  // --- Yardımcı Fonksiyonlar ---

  // Custom Map Mod: Klavye oluşturma
  function createQKeyboard() {
    qKeyboard.innerHTML = ''; // Önceki klavyeyi temizle
    qwertyLayout.forEach(char => {
      const keyElement = document.createElement('div');
      keyElement.classList.add('key');
      keyElement.textContent = char;
      keyElement.dataset.char = char; // Hangi karaktere ait olduğunu tutar

      // Her tuş için sayı girişi input'u oluştur
      const input = document.createElement('input');
      input.type = 'number';
      input.classList.add('key-value-input');
      input.placeholder = '?';
      input.style.display = 'none'; // Başlangıçta gizli
      input.addEventListener('click', (e) => e.stopPropagation()); // Inputa tıklayınca tuşu tetiklememesi için
      input.addEventListener('change', (e) => {
        const val = parseInt(e.target.value);
        if (!isNaN(val)) {
          keyMappings[char] = val;
          keyElement.classList.add('active'); // Anahtar atandığında yeşil yap
        } else {
          delete keyMappings[char]; // Değer boşsa veya sayı değilse anahtarı kaldır
          keyElement.classList.remove('active');
        }
      });
      keyElement.appendChild(input);

      keyElement.addEventListener('click', () => {
        // Diğer tuşların inputlarını gizle
        qKeyboard.querySelectorAll('.key-value-input').forEach(otherInput => {
          if (otherInput !== input) {
            otherInput.style.display = 'none';
          }
        });

        // Tıklanan tuşun input'unu göster/gizle
        input.style.display = input.style.display === 'none' ? 'block' : 'none';
        if (input.style.display === 'block') {
          input.focus(); // Açıldığında odaklan
        }
      });

      qKeyboard.appendChild(keyElement);
    });
  }

  // Custom Map Mod: Önizlemeyi güncelleme
  function updateCustomPreview() {
    const message = customMessageInput.value.toUpperCase();
    const rule = customRuleInput.value;
    let encryptedPreview = '';

    for (const char of message) {
      if (keyMappings[char] !== undefined) {
        let val = keyMappings[char];
        try {
          // Rule'ı güvenli bir şekilde değerlendirmek için geçici bir fonksiyon oluştur
          // NOT: eval() kullanmak güvenlik riski taşır. Gerçek uygulamalarda daha güvenli bir parser/interpreter kullanılmalı.
          const func = new Function('x', 'return ' + rule);
          val = func(val);
        } catch (e) {
          console.error("Kural hatası:", e);
          encryptedPreview += `[HATA]`;
          continue;
        }
        encryptedPreview += `${val} `;
      } else {
        encryptedPreview += `${char} `; // Eşleşmeyen karakteri olduğu gibi bırak
      }
    }
    customEncryptedPreview.textContent = encryptedPreview.trim();
  }

  // Binary Mod: Önizlemeyi güncelleme
  function updateBinaryPreview() {
    const message = binaryMessageInput.value;
    const rule = binaryRuleInput.value;
    let binaryMessage = '';
    let processedBinary = '';

    if (!message) {
        binaryPreviewOutput.innerHTML = '';
        return;
    }

    // Mesajı binary'ye çevir
    for (let i = 0; i < message.length; i++) {
        const charCode = message.charCodeAt(i);
        binaryMessage += charCode.toString(2).padStart(8, '0') + ' ';
    }
    binaryMessage = binaryMessage.trim();

    // Binary string'i bit bit işleyelim
    let currentBitString = '';
    for(let i=0; i<binaryMessage.length; i++){
        const char = binaryMessage[i];
        if(char === ' '){
            processedBinary += ' ';
            continue;
        }
        const bit = parseInt(char); // '0' veya '1'
        let processedBit = bit;
        try {
            // Güvenli eval yerine daha kontrollü bir yaklaşım
            const func = new Function('bit', 'return ' + rule);
            processedBit = func(bit);
            // Sadece 0 veya 1 olduğundan emin olalım
            processedBit = (processedBit === 0 || processedBit === 1) ? processedBit : (bit ^ 1); // Geçersizse XOR 1 yap

        } catch (e) {
            console.error("Binary kural hatası:", e);
            processedBit = (bit ^ 1); // Hata durumunda varsayılan XOR 1
        }
        processedBinary += processedBit;
    }


    binaryPreviewOutput.innerHTML = `
      <p><span class="step-label">Orijinal Binary:</span> <span class="message-content">${binaryMessage}</span></p>
      <p><span class="step-label">Kural Uygulanmış Binary:</span> <span class="message-content">${processedBinary.trim()}</span></p>
    `;
  }


  // --- Event Dinleyicileri ---

  // Mod değiştirme
  modeSwitcher.addEventListener('change', () => {
    if (modeSwitcher.checked) {
      customMapMode.classList.add('hidden');
      binaryMode.classList.remove('hidden');
      updateBinaryPreview(); // Binary moda geçince önizlemeyi güncelle
    } else {
      binaryMode.classList.add('hidden');
      customMapMode.classList.remove('hidden');
      updateCustomPreview(); // Custom Map moda geçince önizlemeyi güncelle
    }
  });

  // Custom Map Mod input ve kural değişikliklerini dinle
  customMessageInput.addEventListener('input', updateCustomPreview);
  customRuleInput.addEventListener('input', updateCustomPreview);

  // Custom Map Mod: Şifrele ve Gönder
  sendCustomButton.addEventListener('click', () => {
    const originalMessage = customMessageInput.value;
    const encryptedMessage = customEncryptedPreview.textContent; // Önizlemedeki hali al
    const rule = customRuleInput.value;

    if (!originalMessage || Object.keys(keyMappings).length === 0) {
      alert("Lütfen bir mesaj girin ve en az bir tuşa anahtar atayın.");
      return;
    }
     if (!rule.trim()) {
      alert("Lütfen bir kural girin.");
      return;
    }

    customCommunicationLog.innerHTML = `
      <div>
        <span class="step-label">Gönderilen Mesaj (Orijinal):</span>
        <span class="message-content">${originalMessage}</span>
      </div>
      <div>
        <span class="step-label">Şifrelenmiş Mesaj (Yolda):</span>
        <span class="message-content">${encryptedMessage}</span>
      </div>
      <div>
        <span class="step-label">Alıcı Tarafında Çözümleme:</span>
        <span class="message-content">Mesaj alıcıya ulaştı ve '${rule}' kuralı ve bilinen anahtar haritalarıyla çözümlenecek.</span>
        <span class="message-content">Çözülen Mesaj: <strong>${originalMessage}</strong> (Basit simülasyon)</span>
      </div>
    `;
    alert("Mesaj başarıyla şifrelendi ve gönderildi!");
  });


  // Binary Mod input ve kural değişikliklerini dinle
  binaryMessageInput.addEventListener('input', updateBinaryPreview);
  binaryRuleInput.addEventListener('input', updateBinaryPreview);

  // Binary Mod: Şifrele ve Gönder
  sendBinaryButton.addEventListener('click', () => {
    const originalMessage = binaryMessageInput.value;
    const rule = binaryRuleInput.value;

    if (!originalMessage) {
      alert("Lütfen bir mesaj girin.");
      return;
    }
    if (!rule.trim()) {
      alert("Lütfen bir kural girin.");
      return;
    }

    // Binary preview'den işlenmiş binary mesajı alalım
    const previewContent = binaryPreviewOutput.querySelector('.message-content:last-of-type');
    const processedBinaryMessage = previewContent ? previewContent.textContent : '';

    // İşlenmiş binary'yi tekrar metne çevirme (basit bir simülasyon için)
    let decryptedMessage = '';
    const binaryBlocks = processedBinaryMessage.split(' ').filter(block => block.length === 8);
    for(const block of binaryBlocks){
        const charCode = parseInt(block, 2);
        decryptedMessage += String.fromCharCode(charCode);
    }

    binaryCommunicationLog.innerHTML = `
      <div>
        <span class="step-label">Gönderilen Mesaj (Orijinal Metin):</span>
        <span class="message-content">${originalMessage}</span>
      </div>
      <div>
        <span class="step-label">Şifrelenmiş Mesaj (Binary):</span>
        <span class="message-content">${processedBinaryMessage}</span>
      </div>
      <div>
        <span class="step-label">Alıcı Tarafında Çözümleme:</span>
        <span class="message-content">Mesaj alıcıya ulaştı ve kuralın tersi veya bilinen anahtarlarla çözümlenecek.</span>
        <span class="message-content">Çözülen Mesaj (Metin): <strong>${decryptedMessage}</strong></span>
      </div>
    `;
    alert("Mesaj başarıyla şifrelendi ve gönderildi!");
  });


  // --- Başlangıç Durumu ---
  createQKeyboard(); // Custom Map modu için klavyeyi oluştur
  updateCustomPreview(); // İlk yüklemede önizlemeyi güncelle
});