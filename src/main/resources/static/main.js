const converter = new showdown.Converter()
const decoder = new TextDecoder()

const elMessages = document.getElementById("messages")
const elSystem = document.getElementById("system")
const elPrompt = document.getElementById("prompt")
const elGenerate = document.getElementById("generate")
const elModel = document.getElementById("model")
const messages = []

const capitalize = s => s.substring(0, 1).toUpperCase() + s.substring(1)

// TODO
const escapeHtml = (unsafe) => {
    return unsafe.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;')
}

const addMessage = htmlContent => {
    const element = document.createElement("div")
    element.classList.add("message")
    element.innerHTML = htmlContent
    return elMessages.appendChild(element)
}

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
    const model = elModel.value
    if (messages.length === 0 && elSystem.value.length > 0) {
        addMessage('System: ' + converter.makeHtml(elSystem.value))
        messages.push({ role: 'system', content: elSystem.value })
    }
    addMessage('User: ' + converter.makeHtml(elPrompt.value))
    messages.push({ role: 'user', content: elPrompt.value })
    elPrompt.value = ''
    fetch("/generate", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            model,
            content: messages,
        }),
    }).then(async (res) => {
        let currentContent = ''
        const element = addMessage(currentContent)
        for await (const chunk of res.body) {
            const string = decoder.decode(chunk)
            currentContent += string
            element.innerHTML = 'Assistant: ' + converter.makeHtml(currentContent)
            element.querySelectorAll('pre code').forEach((el) => {
                if (!el.classList.contains('hljs')) {
                    hljs.highlightElement(el)
                }
            })
        }
        messages.push({ role: 'assistant', content: currentContent })
    }).catch(console.error).finally(() => {
        elGenerate.disabled = false
        elPrompt.readOnly = false
    })
}

elGenerate.onclick = generate

document.getElementById('clear').onclick = () => {
    while (elMessages.firstChild) {
        elMessages.removeChild(elMessages.firstChild)
    }
    elPrompt.value = ''
    messages.length = 0
}

document.getElementById('clearGenerate').onclick = () => {
    while (elMessages.firstChild) {
        elMessages.removeChild(elMessages.firstChild)
    }
    messages.length = 0
    generate()
}

document.getElementById('save').onclick = () => {
    let save = localStorage.getItem('save')
    if (!save) save = '{}'
    save = JSON.parse(save)
    const name = document.getElementById('save-id').value
    save[name] = messages
    localStorage.setItem('save', JSON.stringify(save))
}

document.getElementById('load').onclick = () => {
    let save = localStorage.getItem('save')
    if (!save) return
    save = JSON.parse(save)
    const name = document.getElementById('save-id').value
    while (elMessages.firstChild) {
        elMessages.removeChild(elMessages.firstChild)
    }
    messages.length = 0
    if (!save[name]) return
    messages.push(...save[name])
    messages.forEach(e => {
        addMessage(`${capitalize(e.role)}: ` + converter.makeHtml(e.content))
    })
}
