async function procesarArchivo(action) {
  const fileInput = document.getElementById("fileInput");
  const status = document.getElementById("status");

  if (!fileInput.files.length) {
    alert("Selecciona al menos un archivo o una carpeta.");
    return;
  }

  const formData = new FormData();

  // 🔹 Agregar TODOS los archivos seleccionados (carpeta o múltiples archivos)
  for (let i = 0; i < fileInput.files.length; i++) {
    const file = fileInput.files[i];
    // Si viene de una carpeta, conservar ruta relativa
    const relativePath = file.webkitRelativePath || file.name;
    formData.append("file", file, relativePath);
  }

  // Acción (compress, encrypt, etc.)
  formData.append("action", action);

  status.innerText = "Procesando...";

  try {
    const res = await fetch("http://localhost:4567/upload", {
      method: "POST",
      body: formData
    });

    if (!res.ok) {
      status.innerText = "Error al procesar los archivos.";
      console.error(await res.text());
      return;
    }

    // 🔹 En este caso el servidor devuelve texto, no descarga
    const text = await res.text();
    status.innerText = "✅ Proceso completado.\n" + text;

  } catch (err) {
    console.error(err);
    status.innerText = "Error en la conexión con el servidor.";
  }
}

// 🔹 Botones de acción (sin cambios)
document.getElementById("encryptButton").addEventListener("click", () =>
  procesarArchivo("encrypt")
);

document.getElementById("deEncryptButton").addEventListener("click", () =>
  procesarArchivo("deEncrypt")
);

document.getElementById("compressButton").addEventListener("click", () =>
  procesarArchivo("compress")
);

document.getElementById("deCompressButton").addEventListener("click", () =>
  procesarArchivo("deCompress")
);

document.getElementById("compressEncryptionButton").addEventListener("click", () =>
  procesarArchivo("compressEncryption")
);
