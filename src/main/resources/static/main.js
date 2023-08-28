const converter = new showdown.Converter()
const decoder = new TextDecoder()

const elMessages = document.getElementById("messages")
const elSystem = document.getElementById("system")
const elPrompt = document.getElementById("prompt")
const elGenerate = document.getElementById("generate")
const elModel = document.getElementById("model")
const messages = []

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
    addMessage('User: ' + converter.makeHtml(elPrompt.value))
    if (messages.length === 0 && elSystem.value.length > 0) {
        messages.push({ role: 'system', content: elSystem.value })
    }
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
            //console.log(string)
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
