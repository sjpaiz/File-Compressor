document.getElementById("uploadBtn").addEventListener("click", async () => {
  const fileInput = document.getElementById("fileInput");
  const status = document.getElementById("status");

  if (!fileInput.files.length) {
    alert("Selecciona un archivo primero.");
    return;
  }

  const file = fileInput.files[0];
  const formData = new FormData();
  formData.append("file", file);

  status.innerText = "Procesando...";

  try {
    const res = await fetch("http://localhost:4567/upload", {
      method: "POST",
      body: formData
    });

    if (!res.ok) {
      status.innerText = "Error al procesar el archivo.";
      return;
    }

    // Obtener el nombre del archivo desde el header del servidor
    const disposition = res.headers.get("Content-Disposition") || res.headers.get("content-disposition");
    let filename = "resultado.txt";

    if (disposition) {
      // Intenta extraer el nombre correctamente (ej. attachment; filename="descomprimido.txt")
      const match = /filename\*?=(?:UTF-8'')?["']?([^;"']+)["']?/i.exec(disposition);
      if (match && match[1]) {
        filename = decodeURIComponent(match[1]);
      } else {
        const parts = disposition.split("filename=");
        if (parts.length > 1) filename = parts[1].replace(/["']/g, "").trim();
      }
    }

    // Descargar el archivo resultante
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);

    status.innerText = `Archivo procesado correctamente (${filename})`;
  } catch (err) {
    console.error(err);
    status.innerText = "Error en la conexión con el servidor.";
  }
});
