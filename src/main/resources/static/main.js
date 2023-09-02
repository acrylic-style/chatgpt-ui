const SUMMARIZE_PROMPT = 'Summarize the prompt in around 40 characters for English, and 15 characters for Japanese. You only have to output the result in the appropriate language (If English was provided, then output in English, and do NOT output Japanese). Provide only one summary, and do not provide more than one summary.'

const converter = new showdown.Converter()
const decoder = new TextDecoder()

const elHistory = document.getElementById('history')
const elMessages = document.getElementById('messages')
const elSystem = document.getElementById('system')
const elPrompt = document.getElementById('prompt')
const elGenerate = document.getElementById('generate')
const elModel = document.getElementById('model')
const elDelete = document.getElementById('delete')
const current = {id: '', title: '', messages: []}

let screenTransitionBlocked = false

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
    } finally {
        reader.releaseLock()
    }
}

const saveHistory = () => {
    let save = localStorage.getItem('save') || '{}'
    save = JSON.parse(save)
    save[current.id] = current
    localStorage.setItem('save', JSON.stringify(save))
}

const loadHistory = id => {
    if (screenTransitionBlocked) return
    let save = localStorage.getItem('save')
    if (!save) return
    save = JSON.parse(save)
    while (elMessages.firstChild) {
        elMessages.removeChild(elMessages.firstChild)
    }
    messages.length = 0
    if (!save[id] || !save[id].id || !save[id].messages) return
    current.id = save[id].id
    current.messages = save[id].messages
    current.messages.forEach(e => {
        addMessage(`${capitalize(e.role)}: ` + converter.makeHtml(e.content))
    })
    document.querySelectorAll('#history>a').forEach(e => e.classList.remove('selected'))
    document.querySelector(`a[data-id="${id}"]`)?.classList?.add('selected')
    elDelete.disabled = false
}

const loadAndShowHistory = () => {
    document.querySelectorAll('#history>a[data-id]').forEach(e => e.remove())
    let save = localStorage.getItem('save')
    if (!save) return
    save = JSON.parse(save)
    for (const history of Object.values(save)) {
        let content = history.title || history.messages.find(e => e.role === 'user').content
        content = content.substring(0, Math.min(content.length, 250))
        const aElement = document.createElement('a')
        aElement.setAttribute('data-id', history.id)
        aElement.classList.add('waves-effect', 'waves-light', 'btn-flat')
        if (history.id === current.id) {
            aElement.classList.add('selected')
        }
        aElement.onclick = () => loadHistory(history.id)
        aElement.style.textOverflow = 'ellipse'
        aElement.title = content
        const iElement = document.createElement('i')
        iElement.classList.add('material-icons', 'left')
        iElement.textContent = 'chat_bubble'
        const spanElement = document.createElement('span')
        spanElement.textContent = content
        aElement.appendChild(iElement)
        aElement.appendChild(spanElement)
        elHistory.appendChild(aElement)
    }
}

const generate = () => {
    if (screenTransitionBlocked || elPrompt.value.length === 0) {
        return
    }
    screenTransitionBlocked = true
    elDelete.disabled = true
    elGenerate.disabled = true
    elPrompt.readOnly = true
    const model = elModel.value
    if (current.messages.length === 0) {
        current.id = Math.floor(Math.random() * 10000000000000).toString(16)
        current.title = '' // reset title
        if (elSystem.value.length > 0) {
            addMessage('System: ' + converter.makeHtml(elSystem.value))
            current.messages.push({role: 'system', content: elSystem.value})
        }
    }
    const prompt = elPrompt.value
    addMessage('User: ' + converter.makeHtml(prompt))
    current.messages.push({role: 'user', content: prompt})
    elPrompt.value = ''
    fetch("/generate", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            model,
            content: current.messages,
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
        current.messages.push({role: 'assistant', content: currentContent})
        if (!current.title || current.title.length === 0) {
            // summarize the prompt
            await fetch('/generate', {
                method: 'POST',
                body: JSON.stringify({
                    model: 'gpt-4',
                    content:
                        [
                            {
                                role: 'system',
                                content: SUMMARIZE_PROMPT,
                            },
                            {
                                role: 'user',
                                content: current.messages.find(e => e.role === 'user').content,
                            },
                        ]
                })
            })
                .then(res => res.text())
                .then(summary => {
                    if ((summary.startsWith('"') && summary.endsWith('"')) || (summary.startsWith('「') && summary.endsWith('」'))) {
                        summary = summary.substring(1, summary.length - 1)
                    }
                    current.title = summary
                    saveHistory()
                })
                .finally(() => loadAndShowHistory())
        } else {
            saveHistory()
        }
    }).catch(console.error).finally(() => {
        elGenerate.disabled = false
        elPrompt.readOnly = false
        elDelete.disabled = false
        screenTransitionBlocked = false
    })
}

elGenerate.onclick = generate

const clear = () => {
    if (screenTransitionBlocked) return
    while (elMessages.firstChild) {
        elMessages.removeChild(elMessages.firstChild)
    }
    elPrompt.value = ''
    current.id = ''
    current.messages.length = 0
    document.querySelectorAll('#history>a').forEach(e => e.classList.remove('selected'))
    elDelete.disabled = true
}

document.getElementById('clear').onclick = clear

elDelete.onclick = () => {
    if (screenTransitionBlocked) return
    let save = localStorage.getItem('save')
    if (save) {
        save = JSON.parse(save)
        delete save[current.id]
        localStorage.setItem('save', JSON.stringify(save))
    }
    clear()
    loadAndShowHistory()
}

elModel.onchange = () => {
    localStorage.setItem('model', elModel.value)
}

document.addEventListener('DOMContentLoaded', () => {
    if (localStorage.getItem('model')) {
        elModel.value = localStorage.getItem('model')
        document.querySelectorAll('option').forEach(e => e.selected = false)
        document.querySelector(`option[value="${localStorage.getItem('model')}"]`).selected = true
    }

    // initialize elements
    const elems = document.querySelectorAll('select');
    M.FormSelect.init(elems);

    loadAndShowHistory()
})
