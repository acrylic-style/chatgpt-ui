const elImages = document.getElementById('images')
const elPrompt = document.getElementById("prompt")
const elCount = document.getElementById('count')
const elSize = document.getElementById('size')
const elGenerate = document.getElementById("generate")
const messages = []

const capitalize = s => s.substring(0, 1).toUpperCase() + s.substring(1)

// hacks for Chrome
ReadableStream.prototype[Symbol.asyncIterator] = async function* () {
    const reader = this.getReader()
    try {
        while (true) {
            const {done, value} = await reader.read()
            if (done) return
            yield value
        }
    }
    finally {
        reader.releaseLock()
    }
}

const generate = () => {
    if (elPrompt.value.length === 0) {
        return
    }
    elGenerate.disabled = true
    elPrompt.readOnly = true
    fetch("/generate_image", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            prompt: elPrompt.value,
            count: Math.max(1, Math.min(10, elCount.value)),
            size: elSize.value,
        }),
    })
        .then(res => res.json())
        .then(res => {
            for (const data of res.data) {
                data.b64_json = data.b64_json || ''
                const img = document.createElement('img')
                img.src = `data:image/png;base64,${data.b64_json}`
                elImages.appendChild(img)
            }
        })
        .catch(console.error)
        .finally(() => {
            elGenerate.disabled = false
            elPrompt.readOnly = false
        })
}

elGenerate.onclick = generate

document.getElementById('clear').onclick = () => {
    while (elImages.firstChild) {
        elImages.removeChild(elImages.firstChild)
    }
    elPrompt.value = ''
    messages.length = 0
}

document.getElementById('clearGenerate').onclick = () => {
    while (elImages.firstChild) {
        elImages.removeChild(elImages.firstChild)
    }
    messages.length = 0
    generate()
}
