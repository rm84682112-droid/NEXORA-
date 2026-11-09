document.addEventListener('DOMContentLoaded', () => {
  const downloadBtn = document.getElementById('download-btn');
  const apkSizeSpan = document.getElementById('apk-size');

  // 1. Deteção básica de SO
  const isAndroid = /Android/i.test(navigator.userAgent);

  if (!isAndroid && downloadBtn) {
    const notice = document.createElement('p');
    notice.style.fontSize = '12px';
    notice.style.color = '#38bdf8';
    notice.style.marginTop = '8px';
    notice.textContent = 'ℹ️ A aceder pelo computador? Pode transferir o ficheiro para o seu telemóvel Android.';
    downloadBtn.parentElement.appendChild(notice);
  }

  // 2. Feedback visual imediato ao carregar no download
  if (downloadBtn) {
    downloadBtn.addEventListener('click', () => {
      const originalText = downloadBtn.innerHTML;
      
      downloadBtn.style.opacity = '0.8';
      const label = downloadBtn.querySelector('strong');
      if (label) {
        label.textContent = 'A iniciar transferência...';
      }

      // Restaura o estado após 3 segundos
      setTimeout(() => {
        downloadBtn.style.opacity = '1';
        downloadBtn.innerHTML = originalText;
      }, 3000);
    });
  }
});
