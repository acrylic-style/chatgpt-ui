const SUMMARIZE_PROMPT = 'Summarize the prompt in around 40 characters for English, and 15 characters for Japanese. You only have to output the result in the appropriate language (If English was provided, then output in English, and do NOT output Japanese). Provide only one summary, and do not provide more than one summary.'

const { encode: encodeGPT } = GPTTokenizer_cl100k_base
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

const addMessage = (messageIndex, htmlContent) => {
    const parentDiv = document.createElement('div')
    parentDiv.classList.add("message")
    parentDiv.setAttribute('data-message-index', messageIndex)
    const element = document.createElement("div")
    element.innerHTML = htmlContent
    parentDiv.appendChild(element)
    const copyButton = document.createElement('a')
    copyButton.classList.add("btn-floating", "waves-effect", "white")
    const copyIcon = document.createElement('i')
    copyIcon.classList.add('material-icons')
    copyIcon.textContent = 'content_paste'
    copyButton.appendChild(copyIcon)
    copyButton.onclick = () => {
        const content = current.messages[parseInt(parentDiv.getAttribute('data-message-index'))].content
        if (content) {
            navigator.clipboard.writeText(content).then(() => {
                copyIcon.textContent = 'done'
            }).catch(() => {
                copyIcon.textContent = 'close'
            }).finally(() => {
                setTimeout(() => copyIcon.textContent = 'content_paste', 1000)
            })
        } else {
            copyIcon.textContent = 'close'
            setTimeout(() => copyIcon.textContent = 'content_paste', 1000)
        }
    }
    const deleteButton = document.createElement('a')
    deleteButton.classList.add("btn-floating", "waves-effect", "white")
    const deleteIcon = document.createElement('i')
    deleteIcon.classList.add('material-icons')
    deleteIcon.textContent = 'delete'
    deleteButton.appendChild(deleteIcon)
    deleteButton.onclick = () => {
        const index = parseInt(parentDiv.getAttribute('data-message-index'))
        current.messages.splice(index, 1)
        for (const child of elMessages.children) {
            const curr = parseInt(child.getAttribute('data-message-index'))
            if (curr > index) {
                child.setAttribute('data-message-index', (curr - 1).toString())
            }
        }
        parentDiv.remove()
        saveHistory()
    }
    const spanToken = document.createElement('span')
    spanToken.classList.add('token-field')
    parentDiv.appendChild(copyButton)
    parentDiv.appendChild(deleteButton)
    parentDiv.appendChild(spanToken)
    elMessages.appendChild(parentDiv)
    return element
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
    let totalTokens = 0
    current.messages.forEach((e, i) => {
        addMessage(i, `${capitalize(e.role)}: ` + converter.makeHtml(e.content))
        const tokens = encodeGPT(e.content).length
        const tokenField = document.querySelector(`div.message[data-message-index="${i}"]>span.token-field`)
        tokenField.textContent = `(${totalTokens} + ${tokens} = ${totalTokens + tokens} tokens)`
        totalTokens += tokens
    })
    document.querySelectorAll('#history>a').forEach(e => e.classList.remove('selected'))
    document.querySelector(`a[data-id="${id}"]`)?.classList?.add('selected')
    elDelete.disabled = false
    elMessages.querySelectorAll('pre code').forEach((el) => {
        if (!el.classList.contains('hljs')) {
            hljs.highlightElement(el)
        }
    })
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
            addMessage(0, 'System: ' + converter.makeHtml(elSystem.value))
            current.messages.push({role: 'system', content: elSystem.value})
        }
    }
    const prompt = elPrompt.value
    addMessage(current.messages.length, 'User: ' + converter.makeHtml(prompt))
    const userTotalTokenCount = current.messages.map((e) => encodeGPT(e.content).length).reduce((a, b) => a + b)
    const userTokenCount = encodeGPT(prompt).length
    current.messages.push({role: 'user', content: prompt})
    const userTokenField = document.querySelector(`div.message[data-message-index="${current.messages.length - 1}"]>span.token-field`)
    userTokenField.textContent = `(${userTotalTokenCount} + ${userTokenCount} = ${userTotalTokenCount + userTokenCount} tokens)`
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
        const element = addMessage(current.messages.length, currentContent)
        for await (const chunk of res.body) {
            const string = decoder.decode(chunk)
            currentContent += string
            if ((currentContent.match(/```/g) || []).length % 2 === 1) {
                element.innerHTML = 'Assistant: ' + converter.makeHtml(currentContent + '\n```')
            } else {
                element.innerHTML = 'Assistant: ' + converter.makeHtml(currentContent)
            }
            element.querySelectorAll('pre code').forEach((el) => {
                if (!el.classList.contains('hljs')) {
                    hljs.highlightElement(el)
                }
            })
        }
        const assistantTotalTokenCount = current.messages.map((e) => encodeGPT(e.content).length).reduce((a, b) => a + b)
        const assistantTokenCount = encodeGPT(currentContent).length
        current.messages.push({role: 'assistant', content: currentContent})
        const assistantTokenField = document.querySelector(`div.message[data-message-index="${current.messages.length - 1}"]>span.token-field`)
        assistantTokenField.textContent = `(${assistantTotalTokenCount} + ${assistantTokenCount} = ${assistantTotalTokenCount + assistantTokenCount} tokens)`
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
