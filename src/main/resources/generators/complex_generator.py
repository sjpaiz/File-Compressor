import random
import string

def random_word():
    # mezcla de palabras comunes y aleatorias
    words = ["sistema", "variable", "entorno", "usuario", "configuración",
             "proceso", "servicio", "módulo", "controlador", "paquete",
             "elemento", "transacción", "memoria", "instancia", "parámetro",
             "ejecución", "respuesta", "método", "objeto", "interfaz"]
    return random.choice(words) + ''.join(random.choices(string.ascii_lowercase, k=random.randint(0, 3)))

def random_sentence():
    sentence = ' '.join(random_word() for _ in range(random.randint(8, 16)))
    return sentence.capitalize() + random.choice(['.', '?', '!', '...']) + "\n"

def random_paragraph():
    return ''.join(random_sentence() for _ in range(random.randint(5, 10))) + "\n"

def random_code_block():
    lines = []
    for _ in range(random.randint(5, 15)):
        var = random.choice(['x', 'y', 'z', 'data', 'temp', 'value', 'index'])
        op = random.choice(['+', '-', '*', '/', '%', '^'])
        line = f"{var} = ({random.randint(1, 999)} {op} {random.randint(1, 999)}) // calc {random_word()}\n"
        lines.append(line)
    return ''.join(lines) + "\n"

def generate_text(size_mb=20, out_file="dificil_lz77.txt"):
    target_size = size_mb * 1024 * 1024  # bytes
    written = 0
    with open(out_file, "w", encoding="utf-8") as f:
        while written < target_size:
            block_type = random.choice(["text", "code", "log"])
            if block_type == "text":
                block = random_paragraph()
            elif block_type == "code":
                block = random_code_block()
            else:
                block = f"[{random.randint(0,23):02}:{random.randint(0,59):02}:{random.randint(0,59):02}] INFO: {random_sentence()}"
            f.write(block)
            written += len(block.encode("utf-8"))
    print(f"Archivo generado: {out_file} ({written / (1024*1024):.2f} MB)")

# Ejecutar
generate_text(20)
